package io.letsrolldrew.feud.effects.lighting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.bukkit.scheduler.BukkitTask;

public final class StageLightingService {
    public static final String ORIGINAL_SPEC = "__original__";

    private final Plugin plugin;
    private final StageLightingStore store;
    private final StageLightingPresetCatalog presetCatalog;
    private final Map<String, BlockData> blockDataCache = new LinkedHashMap<>();

    private StageLightingConfig config;
    private BukkitTask animationTask;
    private String activeModeId = "";
    private String activeAnimationId = "";

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

    public Optional<StageLightingConfig.Animation> findAnimation(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(config.animations().get(id.trim()));
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
        config =
                presetCatalog.apply(config.withArena(next).withColumns(Map.of()).withZones(Map.of()));
        store.save(config);
        return true;
    }

    public void clearCenter() {
        stopAnimation();
        config = presetCatalog.apply(config.withArena(StageLightingConfig.Arena.unbound())
                .withColumns(Map.of())
                .withZones(Map.of()));
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
        Map<String, List<String>> nextZones = pruneZones(config.zones(), columns.keySet());
        if (nextZones.isEmpty()) {
            nextZones = inferFallbackZones(columns);
        }
        config = presetCatalog.apply(
                config.withArena(nextArena).withColumns(columns).withZones(nextZones));
        store.save(config);
        return columns.size();
    }

    public boolean applyMode(String id) {
        StageLightingConfig.Mode mode = config.modes().get(id);
        if (mode == null || !config.arena().isBound()) {
            return false;
        }
        stopAnimation();
        applyColumnSpecs(mode.columnSpecs());
        activeModeId = mode.id();
        activeAnimationId = "";
        triggerJingle(mode.jingleId());
        return true;
    }

    public boolean restoreOriginal() {
        if (!config.arena().isBound()) {
            return false;
        }
        stopAnimation();
        applyColumnSpecs(Map.of("all", ORIGINAL_SPEC));
        activeModeId = "";
        activeAnimationId = "";
        return true;
    }

    public boolean playAnimation(String id) {
        StageLightingConfig.Animation animation = config.animations().get(id);
        if (animation == null || !config.arena().isBound()) {
            return false;
        }

        List<StageLightingConfig.Frame> frames = framesFor(animation);
        if (frames.isEmpty()) {
            return false;
        }

        stopAnimation();
        activeModeId = "";
        activeAnimationId = animation.id();

        applyColumnSpecs(frames.getFirst().columnSpecs());
        triggerJingle(animation.jingleId());

        animationTask = Bukkit.getScheduler()
                .runTaskTimer(
                        plugin,
                        new Runnable() {
                            private int frameIndex = 1;

                            @Override
                            public void run() {
                                applyColumnSpecs(frames.get(frameIndex).columnSpecs());
                                frameIndex = (frameIndex + 1) % frames.size();
                            }
                        },
                        animation.periodTicks(),
                        animation.periodTicks());
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

    public void stopAnimation() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        activeAnimationId = "";
    }

    public LightingStatus status() {
        StageLightingConfig.Arena arena = config.arena();
        return new LightingStatus(
                activeModeId,
                activeAnimationId,
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

    public void shutdown() {
        stopAnimation();
    }

    private List<StageLightingConfig.Frame> framesFor(StageLightingConfig.Animation animation) {
        return switch (animation.kind()) {
            case FRAMES -> animation.frames();
            case PULSE -> buildPulseFrames(animation);
            case COLUMN_ALTERNATE -> buildColumnAlternateFrames(animation);
            case SLIDING_WINDOW -> buildSlidingWindowFrames(animation);
        };
    }

    private List<StageLightingConfig.Frame> buildPulseFrames(StageLightingConfig.Animation animation) {
        if (animation.primaryPaletteId().isBlank()
                || animation.secondaryPaletteId().isBlank()) {
            return List.of();
        }
        return List.of(
                new StageLightingConfig.Frame(Map.of("all", animation.primaryPaletteId())),
                new StageLightingConfig.Frame(Map.of("all", animation.secondaryPaletteId())));
    }

    private List<StageLightingConfig.Frame> buildColumnAlternateFrames(StageLightingConfig.Animation animation) {
        if (animation.primaryPaletteId().isBlank()
                || animation.secondaryPaletteId().isBlank()) {
            return List.of();
        }
        List<StageLightingConfig.Column> columns =
                new ArrayList<>(config.columns().values());
        if (columns.isEmpty()) {
            return List.of();
        }

        int groupSize = Math.max(1, animation.step());
        Map<String, String> first = new LinkedHashMap<>();
        Map<String, String> second = new LinkedHashMap<>();
        for (int i = 0; i < columns.size(); i++) {
            String columnId = columns.get(i).id();
            boolean primaryBand = ((i / groupSize) % 2) == 0;
            first.put(columnId, primaryBand ? animation.primaryPaletteId() : animation.secondaryPaletteId());
            second.put(columnId, primaryBand ? animation.secondaryPaletteId() : animation.primaryPaletteId());
        }
        return List.of(new StageLightingConfig.Frame(first), new StageLightingConfig.Frame(second));
    }

    private List<StageLightingConfig.Frame> buildSlidingWindowFrames(StageLightingConfig.Animation animation) {
        List<StageLightingConfig.Column> columns =
                new ArrayList<>(config.columns().values());
        if (columns.isEmpty()
                || animation.primaryPaletteId().isBlank()
                || animation.backgroundPaletteId().isBlank()) {
            return List.of();
        }

        List<StageLightingConfig.Frame> frames = new ArrayList<>();
        for (int start = 0; start < columns.size(); start += animation.step()) {
            Map<String, String> specMap = new LinkedHashMap<>();
            specMap.put("all", animation.backgroundPaletteId());
            for (int offset = 0; offset < animation.windowWidth(); offset++) {
                int index = (start + offset) % columns.size();
                specMap.put(columns.get(index).id(), animation.primaryPaletteId());
            }
            frames.add(new StageLightingConfig.Frame(specMap));
        }
        return frames;
    }

    private void applyColumnSpecs(Map<String, String> columnSpecs) {
        if (columnSpecs == null || columnSpecs.isEmpty() || !config.arena().isBound()) {
            return;
        }
        World world = Bukkit.getWorld(config.arena().world());
        if (world == null) {
            return;
        }

        Map<String, String> resolvedSpecs = resolveColumnSpecs(columnSpecs);
        List<StageLightingConfig.Column> columns =
                new ArrayList<>(config.columns().values());
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
        List<StageLightingConfig.Column> columns =
                new ArrayList<>(config.columns().values());

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
            String spec = entry.getValue();
            if (spec == null || spec.isBlank() || key == null || key.isBlank()) {
                continue;
            }
            if (config.columns().containsKey(key)) {
                resolvedSpecs.put(key, spec);
                continue;
            }

            String zoneKey = key.trim();
            boolean zoneOdd = false;
            boolean zoneEven = false;
            int separator = zoneKey.lastIndexOf(':');
            if (separator > 0 && separator < zoneKey.length() - 1) {
                String selector = zoneKey.substring(separator + 1).trim().toLowerCase(Locale.ROOT);
                if ("odd".equals(selector)) {
                    zoneOdd = true;
                    zoneKey = zoneKey.substring(0, separator).trim();
                } else if ("even".equals(selector)) {
                    zoneEven = true;
                    zoneKey = zoneKey.substring(0, separator).trim();
                }
            }

            List<String> zoneColumns = config.zones().get(zoneKey);
            if (zoneColumns == null || zoneColumns.isEmpty()) {
                continue;
            }
            for (int i = 0; i < zoneColumns.size(); i++) {
                if (zoneOdd && (i % 2) != 0) {
                    continue;
                }
                if (zoneEven && (i % 2) == 0) {
                    continue;
                }
                String columnId = zoneColumns.get(i);
                if (columnId == null || columnId.isBlank()) {
                    continue;
                }
                if (config.columns().containsKey(columnId)) {
                    resolvedSpecs.put(columnId, spec);
                }
            }
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
            BlockData target = adaptFilterShape(cell, blockData);
            blockAt(world, cell).setBlockData(target.clone());
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
            target = adaptFilterShape(cell, target);
            blockAt(world, cell).setBlockData(target.clone());
        }
    }

    private BlockData adaptFilterShape(StageLightingConfig.Cell cell, BlockData desired) {
        if (cell == null || desired == null || cell.role() != StageLightingConfig.CellRole.FILTER) {
            return desired;
        }
        String original = cell.originalSpec();
        if (original == null || !original.toLowerCase(Locale.ROOT).contains("glass_pane")) {
            return desired;
        }
        Material paneMaterial = paneVariant(desired.getMaterial());
        if (paneMaterial == null || paneMaterial == desired.getMaterial()) {
            return desired;
        }
        return paneMaterial.createBlockData();
    }

    private static Material paneVariant(Material material) {
        if (material == null) {
            return null;
        }
        String name = material.name();
        if (name.endsWith("_STAINED_GLASS_PANE") || "GLASS_PANE".equals(name)) {
            return material;
        }
        if ("GLASS".equals(name)) {
            return Material.GLASS_PANE;
        }
        if (name.endsWith("_STAINED_GLASS")) {
            try {
                return Material.valueOf(name + "_PANE");
            } catch (IllegalArgumentException ignored) {
                return material;
            }
        }
        return material;
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
        if (name.endsWith("_STAINED_GLASS")
                || name.endsWith("_STAINED_GLASS_PANE")
                || "GLASS".equals(name)
                || "GLASS_PANE".equals(name)
                || "TINTED_GLASS".equals(name)) {
            return StageLightingConfig.CellRole.FILTER;
        }
        return StageLightingConfig.CellRole.EMITTER;
    }

    private static StageLightingConfig.Cell firstCell(Map.Entry<String, List<StageLightingConfig.Cell>> entry) {
        return entry.getValue().getFirst();
    }

    private static Map<String, List<String>> pruneZones(Map<String, List<String>> zones, Set<String> validColumns) {
        if (zones == null || zones.isEmpty() || validColumns == null || validColumns.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> pruned = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : zones.entrySet()) {
            String zoneId = entry.getKey();
            if (zoneId == null || zoneId.isBlank()) {
                continue;
            }
            List<String> members = entry.getValue();
            if (members == null || members.isEmpty()) {
                continue;
            }
            LinkedHashSet<String> kept = new LinkedHashSet<>();
            for (String columnId : members) {
                if (columnId == null || columnId.isBlank()) {
                    continue;
                }
                String trimmed = columnId.trim();
                if (validColumns.contains(trimmed)) {
                    kept.add(trimmed);
                }
            }
            if (!kept.isEmpty()) {
                pruned.put(zoneId.trim(), List.copyOf(kept));
            }
        }
        return pruned.isEmpty() ? Map.of() : pruned;
    }

    private static Map<String, List<String>> inferFallbackZones(Map<String, StageLightingConfig.Column> columns) {
        if (columns == null || columns.isEmpty()) {
            return Map.of();
        }

        record ColumnMeta(
                String id, int dx, int dz, int minDy, int maxDy, int emitterCount, int filterCount, int paneCount) {}

        Map<String, ColumnMeta> byId = new LinkedHashMap<>();
        Map<String, String> byCoord = new LinkedHashMap<>();
        for (StageLightingConfig.Column column : columns.values()) {
            if (column == null
                    || column.id() == null
                    || column.id().isBlank()
                    || column.cells().isEmpty()) {
                continue;
            }
            StageLightingConfig.Cell first = column.cells().getFirst();
            int minDy = Integer.MAX_VALUE;
            int maxDy = Integer.MIN_VALUE;
            int emitter = 0;
            int filter = 0;
            int panes = 0;
            for (StageLightingConfig.Cell cell : column.cells()) {
                minDy = Math.min(minDy, cell.dy());
                maxDy = Math.max(maxDy, cell.dy());
                if (cell.role() == StageLightingConfig.CellRole.FILTER) {
                    filter++;
                } else {
                    emitter++;
                }
                String original =
                        cell.originalSpec() == null ? "" : cell.originalSpec().toLowerCase(Locale.ROOT);
                if (original.contains("glass_pane")) {
                    panes++;
                }
            }
            ColumnMeta meta = new ColumnMeta(column.id(), first.dx(), first.dz(), minDy, maxDy, emitter, filter, panes);
            byId.put(column.id(), meta);
            byCoord.put(first.dx() + ":" + first.dz(), column.id());
        }
        if (byId.isEmpty()) {
            return Map.of();
        }

        List<ColumnMeta> stage = new ArrayList<>();
        for (ColumnMeta meta : byId.values()) {
            if (byCoord.containsKey((-meta.dx()) + ":" + meta.dz())) {
                stage.add(meta);
            }
        }
        if (stage.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> zones = new LinkedHashMap<>();
        zones.put("arch", new ArrayList<>());
        zones.put("inside", new ArrayList<>());
        zones.put("windows", new ArrayList<>());
        zones.put("slits", new ArrayList<>());
        zones.put("roof", new ArrayList<>());
        zones.put("pillar", new ArrayList<>());
        zones.put("back_wall", new ArrayList<>());
        zones.put("board", new ArrayList<>());

        for (ColumnMeta meta : stage) {
            if (meta.dx() >= 10
                    && meta.dx() <= 17
                    && meta.dz() >= 2
                    && meta.dz() <= 18
                    && meta.emitterCount() > 0
                    && meta.filterCount() == 0) {
                zones.get("board").add(meta.id());
                continue;
            }
            if (meta.dx() >= 12
                    && meta.dx() <= 16
                    && meta.dz() >= 2
                    && meta.dz() <= 12
                    && meta.filterCount() > 0
                    && meta.paneCount() == 0) {
                zones.get("back_wall").add(meta.id());
                continue;
            }
            if (Math.abs(meta.dx()) >= 23) {
                zones.get("pillar").add(meta.id());
                continue;
            }
            if (Math.abs(meta.dx()) == 21 && meta.dz() >= -10 && meta.dz() <= -9) {
                zones.get("slits").add(meta.id());
                continue;
            }
            if (meta.dz() <= -8) {
                zones.get("arch").add(meta.id());
                continue;
            }
            if (meta.maxDy() >= 14 || (meta.dz() <= -3 && meta.emitterCount() > 0 && meta.filterCount() == 0)) {
                zones.get("roof").add(meta.id());
                continue;
            }
            if (meta.filterCount() >= 6) {
                zones.get("windows").add(meta.id());
                continue;
            }
            zones.get("inside").add(meta.id());
        }

        Map<String, List<String>> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : zones.entrySet()) {
            LinkedHashSet<String> unique = new LinkedHashSet<>(entry.getValue());
            if (!unique.isEmpty()) {
                normalized.put(entry.getKey(), List.copyOf(unique));
            }
        }
        return normalized;
    }

    public record LightingStatus(
            String activeModeId,
            String activeAnimationId,
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
        public String activeModeOrNone() {
            return activeModeId == null || activeModeId.isBlank() ? "none" : activeModeId;
        }

        public String activeAnimationOrNone() {
            return activeAnimationId == null || activeAnimationId.isBlank() ? "none" : activeAnimationId;
        }

        public String centerOrNone() {
            return centerBound ? center : "none";
        }
    }
}
