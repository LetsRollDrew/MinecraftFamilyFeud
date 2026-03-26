package io.letsrolldrew.feud.effects.lighting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;

public final class StageLightingService {
    public static final String ORIGINAL_SPEC = "__original__";

    private final Plugin plugin;
    private final StageLightingStore store;
    private final StageLightingPresetCatalog presetCatalog;
    private final Map<String, BlockData> blockDataCache = new LinkedHashMap<>();

    private StageLightingConfig config;

    public StageLightingService(Plugin plugin, StageLightingStore store) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.store = Objects.requireNonNull(store, "store");
        this.presetCatalog = new StageLightingPresetCatalog();
        this.config = presetCatalog.apply(store.load());
        store.save(config);
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

    public Optional<StageLightingConfig.Mode> findMode(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.modes().get(id.trim()));
    }

    public Optional<StageLightingConfig.Jingle> findJingle(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.jingles().get(id.trim()));
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
        config = presetCatalog.apply(config.withArena(next).withColumns(Map.of()));
        store.save(config);
        return true;
    }

    public void clearCenter() {
        config = presetCatalog.apply(config.withArena(StageLightingConfig.Arena.unbound()).withColumns(Map.of()));
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
        config = presetCatalog.apply(config.withArena(nextArena).withColumns(columns));
        store.save(config);
        return columns.size();
    }

    public boolean applyMode(String id) {
        StageLightingConfig.Mode mode = config.modes().get(id);
        if (mode == null || !config.arena().isBound()) {
            return false;
        }
        applyColumnSpecs(mode.columnSpecs());
        triggerJingle(mode.jingleId());
        return true;
    }

    public boolean triggerJingle(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        StageLightingConfig.Jingle jingle = config.jingles().get(id);
        if (jingle == null) {
            return false;
        }

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (String rawCommand : jingle.commands()) {
            if (rawCommand == null || rawCommand.isBlank()) {
                continue;
            }
            String command = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
            Bukkit.dispatchCommand(console, command);
        }
        return true;
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

    private void applyColumnSpecs(Map<String, String> columnSpecs) {
        if (columnSpecs == null || columnSpecs.isEmpty() || !config.arena().isBound()) {
            return;
        }
        World world = Bukkit.getWorld(config.arena().world());
        if (world == null) {
            return;
        }

        Map<String, String> resolvedSpecs = resolveColumnSpecs(columnSpecs);
        List<StageLightingConfig.Column> columns = new ArrayList<>(config.columns().values());
        for (StageLightingConfig.Column column : columns) {
            String spec = resolvedSpecs.get(column.id());
            if (spec == null || spec.isBlank()) {
                continue;
            }
            applyColumnSpec(world, column, spec);
        }
    }

    private Map<String, String> resolveColumnSpecs(Map<String, String> columnSpecs) {
        Map<String, String> resolvedSpecs = new LinkedHashMap<>();
        List<StageLightingConfig.Column> columns = new ArrayList<>(config.columns().values());

        String allSpec = columnSpecs.get("all");
        if (allSpec != null && !allSpec.isBlank()) {
            for (StageLightingConfig.Column column : columns) {
                resolvedSpecs.put(column.id(), allSpec);
            }
        }
        String oddSpec = columnSpecs.get("odd");
        if (oddSpec != null && !oddSpec.isBlank()) {
            for (int i = 0; i < columns.size(); i += 2) {
                resolvedSpecs.put(columns.get(i).id(), oddSpec);
            }
        }
        String evenSpec = columnSpecs.get("even");
        if (evenSpec != null && !evenSpec.isBlank()) {
            for (int i = 1; i < columns.size(); i += 2) {
                resolvedSpecs.put(columns.get(i).id(), evenSpec);
            }
        }
        for (Map.Entry<String, String> entry : columnSpecs.entrySet()) {
            String key = entry.getKey();
            if ("all".equalsIgnoreCase(key) || "odd".equalsIgnoreCase(key) || "even".equalsIgnoreCase(key)) {
                continue;
            }
            resolvedSpecs.put(key, entry.getValue());
        }
        return resolvedSpecs;
    }

    private void applyColumnSpec(World world, StageLightingConfig.Column column, String spec) {
        if (ORIGINAL_SPEC.equalsIgnoreCase(spec)
                || "default".equalsIgnoreCase(spec)
                || "restore".equalsIgnoreCase(spec)) {
            restoreColumn(world, column);
            return;
        }

        StageLightingConfig.Palette palette = config.palettes().get(spec);
        if (palette != null) {
            applyPalette(world, column, palette);
            return;
        }

        BlockData blockData = blockDataFor(spec);
        if (blockData == null) {
            return;
        }
        for (StageLightingConfig.Cell cell : column.cells()) {
            blockAt(world, cell).setBlockData(blockData.clone());
        }
    }

    private void restoreColumn(World world, StageLightingConfig.Column column) {
        for (StageLightingConfig.Cell cell : column.cells()) {
            BlockData original = blockDataFor(cell.originalSpec());
            if (original == null) {
                continue;
            }
            blockAt(world, cell).setBlockData(original.clone());
        }
    }

    private void applyPalette(World world, StageLightingConfig.Column column, StageLightingConfig.Palette palette) {
        BlockData emitterData = palette.emitterSpec().isBlank() ? null : blockDataFor(palette.emitterSpec());
        BlockData filterData = palette.filterSpec().isBlank() ? null : blockDataFor(palette.filterSpec());
        if (emitterData == null && filterData == null) {
            return;
        }

        for (StageLightingConfig.Cell cell : column.cells()) {
            BlockData target = cell.role() == StageLightingConfig.CellRole.FILTER ? filterData : emitterData;
            if (target == null) {
                continue;
            }
            blockAt(world, cell).setBlockData(target.clone());
        }
    }

    private Block blockAt(World world, StageLightingConfig.Cell cell) {
        return world.getBlockAt(
                config.arena().centerX() + cell.dx(),
                config.arena().centerY() + cell.dy(),
                config.arena().centerZ() + cell.dz());
    }

    private BlockData blockDataFor(String spec) {
        if (spec == null) {
            return null;
        }
        String trimmed = spec.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        BlockData cached = blockDataCache.get(trimmed);
        if (cached != null) {
            return cached;
        }

        BlockData parsed = null;
        try {
            parsed = Bukkit.createBlockData(trimmed);
        } catch (IllegalArgumentException ignored) {
        }
        if (parsed == null) {
            try {
                Material material = Material.valueOf(trimmed.toUpperCase(Locale.ROOT));
                parsed = material.createBlockData();
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (parsed != null) {
            blockDataCache.put(trimmed, parsed);
        }
        return parsed;
    }

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
