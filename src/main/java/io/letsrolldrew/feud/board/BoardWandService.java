package io.letsrolldrew.feud.board;

import io.letsrolldrew.feud.board.layout.TilePos;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

// Gives the board wand and captures top-left + bottom-right item frames to bind the 10x6 wall
public final class BoardWandService implements Listener {
    private static final int EXPECTED_WIDTH = 10;
    private static final int EXPECTED_HEIGHT = 6;

    private final Plugin plugin;
    private final NamespacedKey wandKey;
    private final BoardBindingStore store;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public BoardWandService(Plugin plugin, NamespacedKey wandKey, BoardBindingStore store) {
        this.plugin = plugin;
        this.wandKey = wandKey;
        this.store = store;
    }

    public ItemStack giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(
                    "Feud Board Wand", net.kyori.adventure.text.format.NamedTextColor.GOLD));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(wandKey, PersistentDataType.INTEGER, 1);
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        return wand;
    }

    public Optional<BoardBinding> loadBinding() {
        return store.load();
    }

    @EventHandler
    public void onItemFrameClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame frame)) {
            return;
        }
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (!isHoldingWand(event.getPlayer())) {
            return;
        }
        event.setCancelled(true);
        handleSelection(event.getPlayer(), frame);
    }

    private boolean isHoldingWand(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null || held.getType() != Material.STICK) {
            return false;
        }
        ItemMeta meta = held.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(wandKey, PersistentDataType.INTEGER);
    }

    private void handleSelection(Player player, ItemFrame frame) {
        Selection first = selections.get(player.getUniqueId());
        if (first == null) {
            selections.put(player.getUniqueId(), Selection.from(frame));
            player.sendMessage("Top-left set. Now click the bottom-right frame.");
            return;
        }

        BoardBinding binding = buildBinding(first, Selection.from(frame));
        if (binding == null) {
            player.sendMessage("Selection invalid. Ensure a 10x6 rectangle on the same wall (consistent facing).");
            selections.remove(player.getUniqueId());
            return;
        }

        try {
            store.save(binding);
            player.sendMessage("Board binding saved (" + EXPECTED_WIDTH + "x" + EXPECTED_HEIGHT + ").");
            selections.remove(player.getUniqueId());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save board binding: " + e.getMessage());
            player.sendMessage("Error saving board binding. See console.");
        }
    }

    private BoardBinding buildBinding(Selection a, Selection b) {
        if (!a.worldId.equals(b.worldId)) {
            return null;
        }
        if (a.facing != b.facing) {
            return null;
        }

        // determine plane: either X constant or Z constant
        boolean xConst = a.blockX == b.blockX;
        boolean zConst = a.blockZ == b.blockZ;
        if (xConst == zConst) {
            return null; // both changed or both same --> invalid
        }

        int widthDelta = xConst ? Math.abs(a.blockZ - b.blockZ) + 1 : Math.abs(a.blockX - b.blockX) + 1;
        int heightDelta = Math.abs(a.blockY - b.blockY) + 1;
        if (widthDelta != EXPECTED_WIDTH || heightDelta != EXPECTED_HEIGHT) {
            return null;
        }

        HorizontalAxis axis = xConst ? HorizontalAxis.Z : HorizontalAxis.X;
        int widthSign = xConst ? Integer.signum(b.blockZ - a.blockZ) : Integer.signum(b.blockX - a.blockX);
        int topY = Math.max(a.blockY, b.blockY);
        int originX = axis == HorizontalAxis.X
                ? (widthSign > 0 ? Math.min(a.blockX, b.blockX) : Math.max(a.blockX, b.blockX))
                : a.blockX; // x is constant when axis is Z
        int originZ = axis == HorizontalAxis.Z
                ? (widthSign > 0 ? Math.min(a.blockZ, b.blockZ) : Math.max(a.blockZ, b.blockZ))
                : a.blockZ; // z is constant when axis is X

        // validate frames exist at all positions
        World world = Bukkit.getWorld(a.worldId);
        if (world == null) {
            return null;
        }
        Map<String, ItemFrame> frames = collectFrames(world, a.facing, originX, originZ, topY, axis, widthSign);
        for (int y = 0; y < EXPECTED_HEIGHT; y++) {
            for (int x = 0; x < EXPECTED_WIDTH; x++) {
                TilePos pos = new TilePos(x, y);
                var vec = tileToWorld(originX, originZ, topY, axis, widthSign, pos);
                String key = key(vec);
                if (!frames.containsKey(key)) {
                    plugin.getLogger().warning("Missing frame at " + key + " during binding validation.");
                    return null;
                }
            }
        }

        return new BoardBinding(a.worldId, a.facing, axis, widthSign, originX, topY, originZ);
    }

    private Map<String, ItemFrame> collectFrames(
            World world, BlockFace facing, int originX, int originZ, int topY, HorizontalAxis axis, int widthSign) {
        int minX = originX + (axis == HorizontalAxis.X && widthSign < 0 ? -(EXPECTED_WIDTH - 1) : 0);
        int maxX = originX + (axis == HorizontalAxis.X && widthSign > 0 ? (EXPECTED_WIDTH - 1) : 0);
        int minZ = originZ + (axis == HorizontalAxis.Z && widthSign < 0 ? -(EXPECTED_WIDTH - 1) : 0);
        int maxZ = originZ + (axis == HorizontalAxis.Z && widthSign > 0 ? (EXPECTED_WIDTH - 1) : 0);
        int minY = topY - (EXPECTED_HEIGHT - 1);
        int maxY = topY;

        BoundingBox box = BoundingBox.of(new Vector(minX, minY, minZ), new Vector(maxX + 1, maxY + 1, maxZ + 1));
        Map<String, ItemFrame> frames = new HashMap<>();
        for (var entity : world.getNearbyEntities(box, e -> e instanceof ItemFrame)) {
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getFacing() != facing) {
                continue;
            }
            String key = key(frame.getLocation().toVector());
            frames.putIfAbsent(key, frame);
        }
        return frames;
    }

    private Vector tileToWorld(int originX, int originZ, int topY, HorizontalAxis axis, int widthSign, TilePos pos) {
        int x = originX;
        int z = originZ;
        if (axis == HorizontalAxis.X) {
            x += widthSign * pos.x();
        } else {
            z += widthSign * pos.x();
        }
        int y = topY - pos.y();
        return new Vector(x, y, z);
    }

    private String key(Vector v) {
        return v.getBlockX() + "," + v.getBlockY() + "," + v.getBlockZ();
    }

    private record Selection(UUID worldId, int blockX, int blockY, int blockZ, BlockFace facing) {
        static Selection from(ItemFrame frame) {
            var loc = frame.getLocation();
            return new Selection(
                    Objects.requireNonNull(loc.getWorld()).getUID(),
                    loc.getBlockX(),
                    loc.getBlockY(),
                    loc.getBlockZ(),
                    frame.getFacing());
        }
    }
}
