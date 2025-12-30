package io.letsrolldrew.feud.board.display;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

// Persists dynamic board metadata so boards remain addressable after restart
// without this data, boards would lose their layout and players would not be able to
// interact with them properly with the book commands

final class DynamicBoardStore {
    private final File file;
    private final YamlConfiguration config;

    DynamicBoardStore(File file) {
        this.file = file;
        this.config = new YamlConfiguration();
        if (file != null && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        reload();
    }

    Map<String, DynamicBoardLayout> loadLayouts() {
        Map<String, DynamicBoardLayout> layouts = new java.util.HashMap<>();
        if (file == null) {
            return layouts;
        }
        reload();
        var section = config.getConfigurationSection("boards");
        if (section == null) {
            return layouts;
        }
        for (String id : section.getKeys(false)) {
            var path = "boards." + id + ".";
            try {
                UUID worldId = UUID.fromString(config.getString(path + "world"));
                BoardFacing facing = BoardFacing.valueOf(config.getString(path + "facing"));
                var anchor = new org.joml.Vector3d(
                        config.getDouble(path + "anchor.x"),
                        config.getDouble(path + "anchor.y"),
                        config.getDouble(path + "anchor.z"));
                DynamicBoardLayout layout = new DynamicBoardLayout(
                        worldId,
                        facing,
                        anchor,
                        config.getDouble(path + "totalWidth"),
                        config.getDouble(path + "totalHeight"),
                        config.getDouble(path + "cellWidth"),
                        config.getDouble(path + "cellHeight"),
                        config.getDouble(path + "padX"),
                        config.getDouble(path + "padY"),
                        config.getDouble(path + "gapX"),
                        config.getDouble(path + "gapY"),
                        config.getDouble(path + "forwardOffset"),
                        new org.joml.Vector3d(
                                config.getDouble(path + "minCorner.x"),
                                config.getDouble(path + "minCorner.y"),
                                config.getDouble(path + "minCorner.z")),
                        new org.joml.Vector3d(
                                config.getDouble(path + "maxCorner.x"),
                                config.getDouble(path + "maxCorner.y"),
                                config.getDouble(path + "maxCorner.z")));
                layouts.put(id, layout);
            } catch (Exception ignored) {
                // malformed entry
            }
        }
        return layouts;
    }

    void saveLayout(String boardId, DynamicBoardLayout layout) {
        if (file == null || layout == null || boardId == null || boardId.isBlank()) {
            return;
        }
        var path = "boards." + boardId + ".";
        config.set(path + "world", layout.worldId().toString());
        config.set(path + "facing", layout.facing().name());
        config.set(path + "anchor.x", layout.anchor().x);
        config.set(path + "anchor.y", layout.anchor().y);
        config.set(path + "anchor.z", layout.anchor().z);
        config.set(path + "totalWidth", layout.totalWidth());
        config.set(path + "totalHeight", layout.totalHeight());
        config.set(path + "cellWidth", layout.cellWidth());
        config.set(path + "cellHeight", layout.cellHeight());
        config.set(path + "padX", layout.padX());
        config.set(path + "padY", layout.padY());
        config.set(path + "gapX", layout.gapX());
        config.set(path + "gapY", layout.gapY());
        config.set(path + "forwardOffset", layout.forwardOffset());
        config.set(path + "minCorner.x", layout.minCorner().x);
        config.set(path + "minCorner.y", layout.minCorner().y);
        config.set(path + "minCorner.z", layout.minCorner().z);
        config.set(path + "maxCorner.x", layout.maxCorner().x);
        config.set(path + "maxCorner.y", layout.maxCorner().y);
        config.set(path + "maxCorner.z", layout.maxCorner().z);
        save();
    }

    void removeLayout(String boardId) {
        if (file == null || boardId == null || boardId.isBlank()) {
            return;
        }
        config.set("boards." + boardId, null);
        save();
    }

    void clear() {
        if (file == null) {
            return;
        }
        config.set("boards", null);
        save();
    }

    private void reload() {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            config.load(file);
        } catch (Exception ignored) {
        }
    }

    private void save() {
        if (file == null) {
            return;
        }
        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }
}
