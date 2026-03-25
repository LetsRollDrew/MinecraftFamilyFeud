package io.letsrolldrew.feud.effects.lighting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

public final class StageLightingService {
    private final Plugin plugin;
    private final StageLightingStore store;

    private StageLightingConfig config;

    public StageLightingService(Plugin plugin, StageLightingStore store) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.store = Objects.requireNonNull(store, "store");
        this.config = store.load();
    }

    public List<String> columnIds() {
        return List.copyOf(config.columns().keySet());
    }

    public List<String> paletteIds() {
        return List.copyOf(config.palettes().keySet());
    }

    public List<String> modeIds() {
        return List.copyOf(config.modes().keySet());
    }

    public List<String> animationIds() {
        return List.copyOf(config.animations().keySet());
    }

    public List<String> jingleIds() {
        return List.copyOf(config.jingles().keySet());
    }

    public StageLightingConfig.Arena arena() {
        return config.arena();
    }

    public boolean hasBoundCenter() {
        return config.arena().isBound();
    }

    public boolean bindCenter(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        StageLightingConfig.Arena current = config.arena();
        StageLightingConfig.Arena next = new StageLightingConfig.Arena(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                current.axis(),
                current.radiusX(),
                current.radiusZ(),
                current.yDown(),
                current.yUp(),
                current.palette());
        config = config.withArena(next).withColumns(Map.of());
        store.save(config);
        return true;
    }

    public void clearCenter() {
        config = config.withArena(StageLightingConfig.Arena.unbound()).withColumns(Map.of());
        store.save(config);
    }

    public int scanColumns(StageLightingConfig.ScanAxis axis, int radiusX, int radiusZ, int yDown, int yUp) {
        StageLightingConfig.Arena current = config.arena();
        if (!current.isBound()) {
            return -1;
        }
        World world = Bukkit.getWorld(current.world());
        if (world == null) {
            return -1;
        }

        Set<Material> palette = paletteMaterials(current.palette());
        Map<String, List<StageLightingConfig.Cell>> grouped = new LinkedHashMap<>();

        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                for (int dy = -yDown; dy <= yUp; dy++) {
                    Block block =
                            world.getBlockAt(current.centerX() + dx, current.centerY() + dy, current.centerZ() + dz);
                    if (!palette.contains(block.getType())) {
                        continue;
                    }
                    StageLightingConfig.CellRole role = classifyRole(block.getType());
                    String originalSpec = block.getBlockData().getAsString();
                    String key = dx + ":" + dz;
                    grouped.computeIfAbsent(key, ignored -> new ArrayList<>())
                            .add(new StageLightingConfig.Cell(dx, dy, dz, role, originalSpec));
                }
            }
        }

        Comparator<Map.Entry<String, List<StageLightingConfig.Cell>>> ordering = axis == StageLightingConfig.ScanAxis.Z
                ? Comparator.<Map.Entry<String, List<StageLightingConfig.Cell>>>comparingInt(
                                entry -> firstCell(entry).dz())
                        .thenComparingInt(entry -> firstCell(entry).dx())
                : Comparator.<Map.Entry<String, List<StageLightingConfig.Cell>>>comparingInt(
                                entry -> firstCell(entry).dx())
                        .thenComparingInt(entry -> firstCell(entry).dz());

        List<Map.Entry<String, List<StageLightingConfig.Cell>>> ordered = new ArrayList<>(grouped.entrySet());
        ordered.sort(ordering);

        Map<String, StageLightingConfig.Column> columns = new LinkedHashMap<>();
        int index = 1;
        for (Map.Entry<String, List<StageLightingConfig.Cell>> entry : ordered) {
            List<StageLightingConfig.Cell> cells = new ArrayList<>(entry.getValue());
            cells.sort(Comparator.comparingInt(StageLightingConfig.Cell::dy));
            if (cells.isEmpty()) {
                continue;
            }
            String columnId = "c" + String.format(Locale.ROOT, "%03d", index++);
            columns.put(columnId, new StageLightingConfig.Column(columnId, cells));
        }

        StageLightingConfig.Arena nextArena = new StageLightingConfig.Arena(
                current.world(),
                current.centerX(),
                current.centerY(),
                current.centerZ(),
                axis,
                radiusX,
                radiusZ,
                yDown,
                yUp,
                current.palette());
        config = config.withArena(nextArena).withColumns(columns);
        store.save(config);
        return columns.size();
    }

    public LightingStatus status() {
        StageLightingConfig.Arena arena = config.arena();
        return new LightingStatus(
                arena.isBound(),
                arena.isBound()
                        ? arena.world() + " " + arena.centerX() + "," + arena.centerY() + "," + arena.centerZ()
                        : "none",
                arena.axis().name().toLowerCase(Locale.ROOT),
                arena.radiusX(),
                arena.radiusZ(),
                arena.yDown(),
                arena.yUp(),
                config.columns().size(),
                config.palettes().size(),
                config.modes().size(),
                config.animations().size(),
                config.jingles().size());
    }

    public void shutdown() {}

    private Set<Material> paletteMaterials(List<String> paletteSpecs) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        List<String> palette =
                paletteSpecs == null || paletteSpecs.isEmpty() ? StageLightingConfig.defaultPalette() : paletteSpecs;
        for (String spec : palette) {
            if (spec == null || spec.isBlank()) {
                continue;
            }
            try {
                materials.add(Material.valueOf(spec.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return materials;
    }

    private static StageLightingConfig.CellRole classifyRole(Material material) {
        String name = material.name();
        if (name.endsWith("_STAINED_GLASS") || "GLASS".equals(name) || "TINTED_GLASS".equals(name)) {
            return StageLightingConfig.CellRole.FILTER;
        }
        return StageLightingConfig.CellRole.EMITTER;
    }

    private static StageLightingConfig.Cell firstCell(Map.Entry<String, List<StageLightingConfig.Cell>> entry) {
        return entry.getValue().getFirst();
    }

    public record LightingStatus(
            boolean centerBound,
            String center,
            String axis,
            int radiusX,
            int radiusZ,
            int yDown,
            int yUp,
            int columns,
            int palettes,
            int modes,
            int animations,
            int jingles) {
        public String centerOrNone() {
            return centerBound ? center : "none";
        }
    }
}
