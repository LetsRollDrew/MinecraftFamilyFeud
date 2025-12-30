package io.letsrolldrew.feud.board;

import io.letsrolldrew.feud.board.layout.BoardLayout10x6;
import io.letsrolldrew.feud.board.layout.TilePos;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.board.render.TileMapRenderer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.util.*;

// Assigns unique maps to each tile and attaches renderers
public final class MapWallBinder {
    private final BoardBinding binding;
    private final MapIdStore mapIdStore;
    private final TileFramebufferStore framebufferStore;

    public MapWallBinder(BoardBinding binding, MapIdStore mapIdStore, TileFramebufferStore framebufferStore) {
        this.binding = Objects.requireNonNull(binding, "binding");
        this.mapIdStore = Objects.requireNonNull(mapIdStore, "mapIdStore");
        this.framebufferStore = Objects.requireNonNull(framebufferStore, "framebufferStore");
    }

    public boolean bind() {
        World world = Bukkit.getWorld(binding.worldId());
        if (world == null) {
            return false;
        }
        Map<String, ItemFrame> frames = collectFrames(world, binding);
        List<FrameLocation> locs = toLocations(frames.values());
        Map<TilePos, FrameLocation> mapped = BoardGridValidator.validate(binding, locs);

        Map<TilePos, Integer> ids = new HashMap<>(mapIdStore.load());
        boolean updated = false;
        for (TilePos pos : mapped.keySet()) {
            if (!ids.containsKey(pos)) {
                MapView view = Bukkit.createMap(world);
                ids.put(pos, view.getId());
                updated = true;
            }
        }
        if (updated) {
            try {
                mapIdStore.save(ids);
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to save map ids: " + e.getMessage());
            }
        }

        for (Map.Entry<TilePos, FrameLocation> entry : mapped.entrySet()) {
            TilePos pos = entry.getKey();
            FrameLocation loc = entry.getValue();
            Integer mapId = ids.get(pos);
            if (mapId == null) {
                continue;
            }
            MapView view = Bukkit.getMap(mapId.shortValue());
            if (view == null) {
                view = Bukkit.createMap(world);
            }
            view.getRenderers().clear();
            view.addRenderer(new TileMapRenderer(pos, framebufferStore));

            ItemStack mapItem = new ItemStack(Material.FILLED_MAP);
            MapMeta meta = (MapMeta) mapItem.getItemMeta();
            meta.setMapView(view);
            mapItem.setItemMeta(meta);
            ItemFrame frame = frames.get(key(loc.x(), loc.y(), loc.z()));
            if (frame != null) {
                frame.setItem(mapItem);
            }
        }
        return true;
    }

    private Map<String, ItemFrame> collectFrames(World world, BoardBinding binding) {

        int minX = binding.widthAxis() == io.letsrolldrew.feud.board.HorizontalAxis.X && binding.widthSign() < 0 ?
            binding.origin().getBlockX() - (BoardLayout10x6.WIDTH - 1) : binding.origin().getBlockX();
        int maxX = binding.widthAxis() == io.letsrolldrew.feud.board.HorizontalAxis.X && binding.widthSign() > 0 ?
            binding.origin().getBlockX() + (BoardLayout10x6.WIDTH - 1) : binding.origin().getBlockX();
        int minZ = binding.widthAxis() == io.letsrolldrew.feud.board.HorizontalAxis.Z && binding.widthSign() < 0 ?
            binding.origin().getBlockZ() - (BoardLayout10x6.WIDTH - 1) : binding.origin().getBlockZ();
        int maxZ = binding.widthAxis() == io.letsrolldrew.feud.board.HorizontalAxis.Z && binding.widthSign() > 0 ?
            binding.origin().getBlockZ() + (BoardLayout10x6.WIDTH - 1) : binding.origin().getBlockZ();

        int minY = binding.origin().getBlockY() - (BoardLayout10x6.HEIGHT - 1);
        int maxY = binding.origin().getBlockY();

        BoundingBox box = BoundingBox.of(new Vector(minX, minY, minZ), new Vector(maxX + 1, maxY + 1, maxZ + 1));
        Map<String, ItemFrame> frames = new HashMap<>();
        for (var entity : world.getNearbyEntities(box, e -> e instanceof ItemFrame)) {
            ItemFrame frame = (ItemFrame) entity;
            if (frame.getFacing() != binding.facing()) {
                continue;
            }
            frames.putIfAbsent(key(frame.getLocation().getBlockX(), frame.getLocation().getBlockY(), frame.getLocation().getBlockZ()), frame);
        }
        return frames;
    }

    private List<FrameLocation> toLocations(Collection<ItemFrame> frames) {
        List<FrameLocation> list = new ArrayList<>();
        for (ItemFrame frame : frames) {
            list.add(new FrameLocation(
                frame.getUniqueId(),
                frame.getLocation().getBlockX(),
                frame.getLocation().getBlockY(),
                frame.getLocation().getBlockZ(),
                frame.getFacing()
            ));
        }
        return list;
    }

    private String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
