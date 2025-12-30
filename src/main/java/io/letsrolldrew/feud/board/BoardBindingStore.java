package io.letsrolldrew.feud.board;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

// Persists a single board binding to a YAML file
public final class BoardBindingStore {
    private final Plugin plugin;
    private final File file;

    public BoardBindingStore(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "board-binding.yml");
    }

    public Optional<BoardBinding> load() {
        if (!file.exists()) {
            return Optional.empty();
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String worldStr = cfg.getString("world");
        String facingStr = cfg.getString("facing");
        String axisStr = cfg.getString("width-axis");
        int sign = cfg.getInt("width-sign", 0);
        Integer originX = getInt(cfg, "origin.x");
        Integer originY = getInt(cfg, "origin.y");
        Integer originZ = getInt(cfg, "origin.z");
        if (worldStr == null
                || facingStr == null
                || axisStr == null
                || sign == 0
                || originX == null
                || originY == null
                || originZ == null) {
            plugin.getLogger().warning("board-binding.yml is incomplete; ignoring.");
            return Optional.empty();
        }
        UUID worldId = UUID.fromString(worldStr);
        BlockFace facing = BlockFace.valueOf(facingStr);
        HorizontalAxis axis = HorizontalAxis.valueOf(axisStr);
        return Optional.of(new BoardBinding(worldId, facing, axis, sign, originX, originY, originZ));
    }

    public void save(BoardBinding binding) throws IOException {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("world", binding.worldId().toString());
        cfg.set("facing", binding.facing().name());
        cfg.set("width-axis", binding.widthAxis().name());
        cfg.set("width-sign", binding.widthSign());
        cfg.set("origin.x", binding.origin().getBlockX());
        cfg.set("origin.y", binding.origin().getBlockY());
        cfg.set("origin.z", binding.origin().getBlockZ());
        cfg.save(file);
    }

    private Integer getInt(FileConfiguration cfg, String path) {
        if (!cfg.isInt(path)) {
            return null;
        }
        return cfg.getInt(path);
    }
}
