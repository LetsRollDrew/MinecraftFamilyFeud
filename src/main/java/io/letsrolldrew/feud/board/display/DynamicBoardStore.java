package io.letsrolldrew.feud.board.display;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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
            StoredLayout stored = LayoutStoreSupport.readStoredLayout(config, path, id);
            if (stored != null && stored.layout() != null) {
                layouts.put(stored.id(), stored.layout());
            }
        }
        return layouts;
    }

    void saveLayout(String boardId, DynamicBoardLayout layout) {
        if (file == null || layout == null || boardId == null || boardId.isBlank()) {
            return;
        }
        var path = "boards." + boardId + ".";
        LayoutStoreSupport.writeLayout(config, path, layout);
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
