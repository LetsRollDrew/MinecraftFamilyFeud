package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// Persists the TilePos to map id mapping so board maps survive server restarts
public final class MapIdStore {
    private final File file;

    public MapIdStore(File file) {
        this.file = file;
    }

    public Map<TilePos, Integer> load() {
        Map<TilePos, Integer> result = new HashMap<>();
        if (!file.exists()) {
            return result;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (String key : cfg.getKeys(false)) {
            String[] parts = key.split(",");
            if (parts.length != 2) {
                continue;
            }
            try {
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                int mapId = cfg.getInt(key);
                result.put(new TilePos(x, y), mapId);
            } catch (NumberFormatException ignored) {
                // skip bad entries
            }
        }
        return result;
    }

    public void save(Map<TilePos, Integer> mapping) throws IOException {
        FileConfiguration cfg = new YamlConfiguration();
        for (Map.Entry<TilePos, Integer> entry : mapping.entrySet()) {
            TilePos pos = entry.getKey();
            cfg.set(pos.x() + "," + pos.y(), entry.getValue());
        }
        cfg.save(file);
    }
}
