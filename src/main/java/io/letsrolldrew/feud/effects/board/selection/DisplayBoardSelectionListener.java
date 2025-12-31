package io.letsrolldrew.feud.effects.board.selection;

import io.letsrolldrew.feud.board.display.BoardFacing;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.joml.Vector3d;

// Captures two corners on a wall to build a DisplayBoardSelection.

public final class DisplayBoardSelectionListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey wandKey;
    private final DisplayBoardSelectionStore store;
    private final Consumer<Player> selectionRefresher;
    private final Map<UUID, Partial> partials = new ConcurrentHashMap<>();

    public DisplayBoardSelectionListener(
            Plugin plugin,
            NamespacedKey wandKey,
            DisplayBoardSelectionStore store,
            Consumer<Player> selectionRefresher) {
        this.plugin = plugin;
        this.wandKey = wandKey;
        this.store = store;
        this.selectionRefresher = selectionRefresher;
    }

    public ItemStack giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.STICK);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(
                    "Display Selector", net.kyori.adventure.text.format.NamedTextColor.AQUA));
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(wandKey, PersistentDataType.INTEGER, 1);
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        return wand;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!isHoldingWand(event.getPlayer())) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        BlockFace face = event.getBlockFace();
        BoardFacing facing = toBoardFacing(face);
        if (facing == null) {
            player.sendMessage("Select a vertical face (north/east/south/west)");
            return;
        }

        UUID worldId = event.getClickedBlock().getWorld().getUID();
        Vector3d corner = new Vector3d(
                event.getClickedBlock().getX(),
                event.getClickedBlock().getY(),
                event.getClickedBlock().getZ());
        Vector3d normal = new Vector3d(face.getModX(), face.getModY(), face.getModZ());

        Partial first = partials.get(player.getUniqueId());
        if (first == null) {
            partials.put(player.getUniqueId(), new Partial(worldId, corner, facing, normal));
            player.sendMessage("Corner 1 set. Click opposite corner on the same face.");
            return;
        }

        if (!first.worldId.equals(worldId)) {
            player.sendMessage("Selection failed: corners must be in the same world.");
            partials.remove(player.getUniqueId());
            return;
        }
        if (first.facing != facing) {
            player.sendMessage("Selection failed: corners must be on the same face.");
            partials.remove(player.getUniqueId());
            return;
        }

        // verify plane: for north/south, z should match, for east/west, x should match
        if ((facing == BoardFacing.NORTH || facing == BoardFacing.SOUTH) && first.corner.z != corner.z) {
            player.sendMessage("Selection failed: not on the same wall (z mismatch).");
            partials.remove(player.getUniqueId());
            return;
        }
        if ((facing == BoardFacing.EAST || facing == BoardFacing.WEST) && first.corner.x != corner.x) {
            player.sendMessage("Selection failed: not on the same wall (x mismatch).");
            partials.remove(player.getUniqueId());
            return;
        }

        DisplayBoardSelection selection = new DisplayBoardSelection(worldId, first.corner, corner, facing, normal);
        if (selection.isInvalid()) {
            player.sendMessage("Selection failed: " + selection.invalidReason());
            partials.remove(player.getUniqueId());
            return;
        }

        store.set(player.getUniqueId(), selection);
        partials.remove(player.getUniqueId());

        double width = Math.abs(first.corner.x - corner.x) + 1;
        double height = Math.abs(first.corner.y - corner.y) + 1;
        player.sendMessage(
                "Display selection saved: width=" + width + " height=" + height + " facing=" + facing.name());
        if (selectionRefresher != null) {
            selectionRefresher.accept(player);
        }
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

    private BoardFacing toBoardFacing(BlockFace face) {
        return switch (face) {
            case NORTH -> BoardFacing.NORTH;
            case SOUTH -> BoardFacing.SOUTH;
            case EAST -> BoardFacing.EAST;
            case WEST -> BoardFacing.WEST;
            default -> null;
        };
    }

    private record Partial(UUID worldId, Vector3d corner, BoardFacing facing, Vector3d normal) {}
}
