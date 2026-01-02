package io.letsrolldrew.feud.board.display;

import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;
import org.joml.Vector3d;

// Shared serialization helpers for DynamicBoardLayout so
// multiple selection stores can read/write Layouts without
// having to rewrite all this YAML config everytime (Panels/Boards/more later?)

public final class LayoutStoreSupport {
    private LayoutStoreSupport() {}

    public static StoredLayout readStoredLayout(YamlConfiguration config, String pathPrefix, String id) {
        DynamicBoardLayout layout = readLayout(config, pathPrefix);
        if (layout == null || id == null) {
            return null;
        }
        return new StoredLayout(id, layout);
    }

    public static DynamicBoardLayout readLayout(YamlConfiguration config, String pathPrefix) {
        if (config == null || pathPrefix == null) {
            return null;
        }
        try {
            String worldRaw = config.getString(pathPrefix + "world");
            String facingRaw = config.getString(pathPrefix + "facing");
            if (worldRaw == null || facingRaw == null) {
                return null;
            }

            UUID worldId = UUID.fromString(worldRaw);
            BoardFacing facing = BoardFacing.valueOf(facingRaw);

            Vector3d anchor = new Vector3d(
                    config.getDouble(pathPrefix + "anchor.x"),
                    config.getDouble(pathPrefix + "anchor.y"),
                    config.getDouble(pathPrefix + "anchor.z"));

            Vector3d minCorner = new Vector3d(
                    config.getDouble(pathPrefix + "minCorner.x"),
                    config.getDouble(pathPrefix + "minCorner.y"),
                    config.getDouble(pathPrefix + "minCorner.z"));

            Vector3d maxCorner = new Vector3d(
                    config.getDouble(pathPrefix + "maxCorner.x"),
                    config.getDouble(pathPrefix + "maxCorner.y"),
                    config.getDouble(pathPrefix + "maxCorner.z"));

            double totalWidth = config.getDouble(pathPrefix + "totalWidth");
            double totalHeight = config.getDouble(pathPrefix + "totalHeight");
            double cellWidth = config.getDouble(pathPrefix + "cellWidth");
            double cellHeight = config.getDouble(pathPrefix + "cellHeight");
            double padX = config.getDouble(pathPrefix + "padX");
            double padY = config.getDouble(pathPrefix + "padY");
            double gapX = config.getDouble(pathPrefix + "gapX");
            double gapY = config.getDouble(pathPrefix + "gapY");
            double forwardOffset = config.getDouble(pathPrefix + "forwardOffset");

            return new DynamicBoardLayout(
                    worldId,
                    facing,
                    anchor,
                    totalWidth,
                    totalHeight,
                    cellWidth,
                    cellHeight,
                    padX,
                    padY,
                    gapX,
                    gapY,
                    forwardOffset,
                    minCorner,
                    maxCorner);
        } catch (Exception ex) {
            return null;
        }
    }

    public static void writeLayout(YamlConfiguration config, String pathPrefix, DynamicBoardLayout layout) {
        if (config == null || pathPrefix == null || layout == null) {
            return;
        }
        config.set(pathPrefix + "world", layout.worldId().toString());
        config.set(pathPrefix + "facing", layout.facing().name());
        config.set(pathPrefix + "anchor.x", layout.anchor().x);
        config.set(pathPrefix + "anchor.y", layout.anchor().y);
        config.set(pathPrefix + "anchor.z", layout.anchor().z);
        config.set(pathPrefix + "totalWidth", layout.totalWidth());
        config.set(pathPrefix + "totalHeight", layout.totalHeight());
        config.set(pathPrefix + "cellWidth", layout.cellWidth());
        config.set(pathPrefix + "cellHeight", layout.cellHeight());
        config.set(pathPrefix + "padX", layout.padX());
        config.set(pathPrefix + "padY", layout.padY());
        config.set(pathPrefix + "gapX", layout.gapX());
        config.set(pathPrefix + "gapY", layout.gapY());
        config.set(pathPrefix + "forwardOffset", layout.forwardOffset());
        config.set(pathPrefix + "minCorner.x", layout.minCorner().x);
        config.set(pathPrefix + "minCorner.y", layout.minCorner().y);
        config.set(pathPrefix + "minCorner.z", layout.minCorner().z);
        config.set(pathPrefix + "maxCorner.x", layout.maxCorner().x);
        config.set(pathPrefix + "maxCorner.y", layout.maxCorner().y);
        config.set(pathPrefix + "maxCorner.z", layout.maxCorner().z);
    }
}
