package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

public final class BuzzerBindingStore {
    private final File file;
    private final YamlConfiguration config;

    public BuzzerBindingStore(File file) {
        this.file = file;
        this.config = new YamlConfiguration();
        if (file != null && file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        reload();
    }

    public Map<TeamId, BlockRef> loadAll() {
        Map<TeamId, BlockRef> bindings = new EnumMap<>(TeamId.class);
        reload();
        for (TeamId team : TeamId.values()) {
            String path = "buzzers." + team.name().toLowerCase();
            String worldId = config.getString(path + ".world");
            if (worldId == null || worldId.isBlank()) {
                continue;
            }
            try {
                bindings.put(
                        team,
                        new BlockRef(
                                UUID.fromString(worldId),
                                config.getInt(path + ".x"),
                                config.getInt(path + ".y"),
                                config.getInt(path + ".z")));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return bindings;
    }

    public void save(TeamId team, BlockRef ref) {
        if (team == null || ref == null) {
            return;
        }
        String path = "buzzers." + team.name().toLowerCase();
        config.set(path + ".world", ref.worldId().toString());
        config.set(path + ".x", ref.x());
        config.set(path + ".y", ref.y());
        config.set(path + ".z", ref.z());
        save();
    }

    public void clear(TeamId team) {
        if (team == null) {
            return;
        }
        config.set("buzzers." + team.name().toLowerCase(), null);
        save();
    }

    public void clearAll() {
        config.set("buzzers", null);
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
