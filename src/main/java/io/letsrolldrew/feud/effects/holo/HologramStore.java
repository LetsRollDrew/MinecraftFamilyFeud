package io.letsrolldrew.feud.effects.holo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

// persist holo ids so /feud holo list/remove works after restart
final class HologramStore {
    private final File file;
    private final YamlConfiguration config;

    HologramStore(File file) {
        this.file = file;
        this.config = new YamlConfiguration();
        if (file != null && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        reload();
    }

    Map<String, StoredHologram> loadAll() {
        Map<String, StoredHologram> out = new HashMap<>();
        if (file == null) {
            return out;
        }
        reload();
        var section = config.getConfigurationSection("holograms");
        if (section == null) {
            return out;
        }
        for (String id : section.getKeys(false)) {
            var path = "holograms." + id + ".";
            try {
                UUID worldId = UUID.fromString(config.getString(path + "world"));
                UUID entityId = UUID.fromString(config.getString(path + "uuid"));
                HologramType type = HologramType.valueOf(config.getString(path + "type"));
                out.put(id, new StoredHologram(type, worldId, entityId));
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    void save(String id, HologramType type, UUID worldId, UUID entityId) {
        if (file == null || id == null || id.isBlank() || type == null || worldId == null || entityId == null) {
            return;
        }
        var path = "holograms." + id + ".";
        config.set(path + "type", type.name());
        config.set(path + "world", worldId.toString());
        config.set(path + "uuid", entityId.toString());
        save();
    }

    void remove(String id) {
        if (file == null || id == null || id.isBlank()) {
            return;
        }
        config.set("holograms." + id, null);
        save();
    }

    void clear() {
        if (file == null) {
            return;
        }
        config.set("holograms", null);
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

    record StoredHologram(HologramType type, UUID worldId, UUID entityId) {}
}
