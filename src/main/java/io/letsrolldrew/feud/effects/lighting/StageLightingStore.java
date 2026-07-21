package io.letsrolldrew.feud.effects.lighting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class StageLightingStore {
    private final File file;

    public StageLightingStore(File file) {
        this.file = file;
    }

    public StageLightingConfig load() {
        if (file == null) {
            return new StageLightingConfig(
                    StageLightingConfig.Arena.unbound(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = config.getConfigurationSection("lighting");
        if (root == null) {
            return new StageLightingConfig(
                    StageLightingConfig.Arena.unbound(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of());
        }

        return new StageLightingConfig(
                loadArena(root),
                loadColumns(root),
                loadZones(root),
                loadPalettes(root),
                loadModes(root),
                loadAnimations(root),
                loadJingles(root));
    }

    public void save(StageLightingConfig config) {
        if (file == null || config == null) {
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();
        saveArena(yaml, config.arena());
        saveColumns(yaml, config.columns());
        saveZones(yaml, config.zones());
        savePalettes(yaml, config.palettes());
        saveModes(yaml, config.modes());
        saveAnimations(yaml, config.animations());
        saveJingles(yaml, config.jingles());

        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            yaml.save(file);
        } catch (IOException ignored) {
        }
    }

    private static StageLightingConfig.Arena loadArena(ConfigurationSection root) {
        ConfigurationSection arena = root.getConfigurationSection("arena");
        if (arena == null) {
            return StageLightingConfig.Arena.unbound();
        }
        List<Integer> center = parseTriple(arena.getString("center", ""));
        String world = arena.getString("world", "");
        StageLightingConfig.ScanAxis axis = StageLightingConfig.ScanAxis.fromString(arena.getString("axis", "x"));
        return new StageLightingConfig.Arena(
                world,
                center.size() > 0 ? center.get(0) : 0,
                center.size() > 1 ? center.get(1) : 0,
                center.size() > 2 ? center.get(2) : 0,
                axis,
                arena.getInt("radiusX", 25),
                arena.getInt("radiusZ", 25),
                arena.getInt("yDown", 16),
                arena.getInt("yUp", 15),
                arena.getStringList("palette"));
    }

    private static Map<String, StageLightingConfig.Column> loadColumns(ConfigurationSection root) {
        Map<String, StageLightingConfig.Column> columns = new LinkedHashMap<>();
        ConfigurationSection columnsSection = root.getConfigurationSection("columns");
        if (columnsSection == null) {
            return columns;
        }

        for (String columnId : columnsSection.getKeys(false)) {
            ConfigurationSection columnSection = columnsSection.getConfigurationSection(columnId);
            if (columnSection == null) {
                continue;
            }
            List<StageLightingConfig.Cell> cells = new ArrayList<>();
            List<Map<?, ?>> rawCells = columnSection.getMapList("cells");
            if (!rawCells.isEmpty()) {
                for (Map<?, ?> rawCell : rawCells) {
                    CellParts parts = parseCellMap(rawCell);
                    if (parts != null) {
                        cells.add(new StageLightingConfig.Cell(
                                parts.dx, parts.dy, parts.dz, parts.role, parts.originalSpec));
                    }
                }
            } else {
                for (String rawCell : columnSection.getStringList("cells")) {
                    CellParts parts = parseCellString(rawCell);
                    if (parts != null) {
                        cells.add(new StageLightingConfig.Cell(
                                parts.dx, parts.dy, parts.dz, parts.role, parts.originalSpec));
                    }
                }
            }
            columns.put(columnId, new StageLightingConfig.Column(columnId, cells));
        }
        return columns;
    }

    private static Map<String, StageLightingConfig.Palette> loadPalettes(ConfigurationSection root) {
        Map<String, StageLightingConfig.Palette> palettes = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection("palettes");
        if (section == null) {
            return palettes;
        }

        for (String paletteId : section.getKeys(false)) {
            ConfigurationSection paletteSection = section.getConfigurationSection(paletteId);
            if (paletteSection == null) {
                continue;
            }
            String emitter = paletteSection.getString("emitter", "");
            String filter = paletteSection.getString("filter", paletteSection.getString("glass", ""));
            palettes.put(paletteId, new StageLightingConfig.Palette(paletteId, emitter, filter));
        }
        return palettes;
    }

    private static Map<String, List<String>> loadZones(ConfigurationSection root) {
        Map<String, List<String>> zones = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection("zones");
        if (section == null) {
            return zones;
        }
        for (String zoneId : section.getKeys(false)) {
            List<String> columns = section.getStringList(zoneId);
            if (columns == null) {
                columns = List.of();
            }
            List<String> cleaned = new ArrayList<>();
            for (String columnId : columns) {
                if (columnId == null || columnId.isBlank()) {
                    continue;
                }
                String trimmed = columnId.trim();
                if (!cleaned.contains(trimmed)) {
                    cleaned.add(trimmed);
                }
            }
            zones.put(zoneId, cleaned);
        }
        return zones;
    }

    private static Map<String, StageLightingConfig.Mode> loadModes(ConfigurationSection root) {
        Map<String, StageLightingConfig.Mode> modes = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection("modes");
        if (section == null) {
            return modes;
        }

        for (String modeId : section.getKeys(false)) {
            ConfigurationSection modeSection = section.getConfigurationSection(modeId);
            if (modeSection == null) {
                continue;
            }
            ConfigurationSection specSection = modeSection.getConfigurationSection("columns");
            if (specSection == null) {
                specSection = modeSection.getConfigurationSection("zones");
            }
            modes.put(
                    modeId,
                    new StageLightingConfig.Mode(modeId, loadSpecMap(specSection), linkedJingleId(modeSection)));
        }
        return modes;
    }

    private static Map<String, StageLightingConfig.Animation> loadAnimations(ConfigurationSection root) {
        Map<String, StageLightingConfig.Animation> animations = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection("animations");
        if (section == null) {
            return animations;
        }

        for (String animationId : section.getKeys(false)) {
            ConfigurationSection animationSection = section.getConfigurationSection(animationId);
            if (animationSection == null) {
                continue;
            }

            List<StageLightingConfig.Frame> frames = new ArrayList<>();
            List<Map<?, ?>> rawFrames = animationSection.getMapList("frames");
            for (Map<?, ?> rawFrame : rawFrames) {
                Object columnsObj = rawFrame.get("columns");
                Object zonesObj = rawFrame.get("zones");
                Map<String, String> specMap = new LinkedHashMap<>();
                if (columnsObj instanceof Map<?, ?> rawColumns) {
                    readSpecMap(rawColumns, specMap);
                } else if (zonesObj instanceof Map<?, ?> rawZones) {
                    readSpecMap(rawZones, specMap);
                }
                frames.add(new StageLightingConfig.Frame(specMap));
            }

            StageLightingConfig.AnimationKind kind = StageLightingConfig.AnimationKind.fromString(
                    animationSection.getString("kind", frames.isEmpty() ? "pulse" : "frames"));
            animations.put(
                    animationId,
                    new StageLightingConfig.Animation(
                            animationId,
                            kind,
                            animationSection.getLong("periodTicks", 6L),
                            frames,
                            animationSection.getString("primary", animationSection.getString("active", "")),
                            animationSection.getString("secondary", ""),
                            animationSection.getString("background", ""),
                            animationSection.getInt("width", 1),
                            animationSection.getInt("step", 1),
                            linkedJingleId(animationSection)));
        }
        return animations;
    }

    private static Map<String, StageLightingConfig.Jingle> loadJingles(ConfigurationSection root) {
        Map<String, StageLightingConfig.Jingle> jingles = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection("jingles");
        if (section == null) {
            section = root.getConfigurationSection("cues");
        }
        if (section == null) {
            return jingles;
        }

        for (String jingleId : section.getKeys(false)) {
            ConfigurationSection jingleSection = section.getConfigurationSection(jingleId);
            if (jingleSection == null) {
                continue;
            }

            List<String> commands = new ArrayList<>();
            for (String command : jingleSection.getStringList("commands")) {
                if (command != null && !command.isBlank()) {
                    commands.add(command.trim());
                }
            }
            String singleCommand = jingleSection.getString("command", "");
            if (!singleCommand.isBlank()) {
                commands.add(singleCommand.trim());
            }
            jingles.put(jingleId, new StageLightingConfig.Jingle(jingleId, commands));
        }
        return jingles;
    }

    private static void saveArena(YamlConfiguration yaml, StageLightingConfig.Arena arena) {
        String base = "lighting.arena";
        yaml.set(base + ".world", arena.world());
        yaml.set(base + ".center", arena.centerX() + "," + arena.centerY() + "," + arena.centerZ());
        yaml.set(base + ".axis", arena.axis().name().toLowerCase());
        yaml.set(base + ".radiusX", arena.radiusX());
        yaml.set(base + ".radiusZ", arena.radiusZ());
        yaml.set(base + ".yDown", arena.yDown());
        yaml.set(base + ".yUp", arena.yUp());
        yaml.set(base + ".palette", arena.palette());
    }

    private static void saveColumns(YamlConfiguration yaml, Map<String, StageLightingConfig.Column> columns) {
        String base = "lighting.columns";
        yaml.set(base, null);
        for (StageLightingConfig.Column column : columns.values()) {
            List<Map<String, Object>> rawCells = new ArrayList<>();
            for (StageLightingConfig.Cell cell : column.cells()) {
                Map<String, Object> rawCell = new LinkedHashMap<>();
                rawCell.put("dx", cell.dx());
                rawCell.put("dy", cell.dy());
                rawCell.put("dz", cell.dz());
                rawCell.put("role", cell.role().name().toLowerCase());
                rawCell.put("original", cell.originalSpec());
                rawCells.add(rawCell);
            }
            yaml.set(base + "." + column.id() + ".cells", rawCells);
        }
    }

    private static void savePalettes(YamlConfiguration yaml, Map<String, StageLightingConfig.Palette> palettes) {
        String base = "lighting.palettes";
        yaml.set(base, null);
        for (StageLightingConfig.Palette palette : palettes.values()) {
            String paletteBase = base + "." + palette.id();
            yaml.set(paletteBase + ".emitter", palette.emitterSpec().isBlank() ? null : palette.emitterSpec());
            yaml.set(paletteBase + ".filter", palette.filterSpec().isBlank() ? null : palette.filterSpec());
        }
    }

    private static void saveZones(YamlConfiguration yaml, Map<String, List<String>> zones) {
        String base = "lighting.zones";
        yaml.set(base, null);
        for (Map.Entry<String, List<String>> entry : zones.entrySet()) {
            yaml.set(base + "." + entry.getKey(), entry.getValue());
        }
    }

    private static void saveModes(YamlConfiguration yaml, Map<String, StageLightingConfig.Mode> modes) {
        String base = "lighting.modes";
        yaml.set(base, null);
        for (StageLightingConfig.Mode mode : modes.values()) {
            String modeBase = base + "." + mode.id();
            yaml.set(modeBase + ".jingle", mode.jingleId().isBlank() ? null : mode.jingleId());
            for (Map.Entry<String, String> entry : mode.columnSpecs().entrySet()) {
                yaml.set(modeBase + ".columns." + entry.getKey(), entry.getValue());
            }
        }
    }

    private static void saveAnimations(YamlConfiguration yaml, Map<String, StageLightingConfig.Animation> animations) {
        String base = "lighting.animations";
        yaml.set(base, null);
        for (StageLightingConfig.Animation animation : animations.values()) {
            String animationBase = base + "." + animation.id();
            yaml.set(animationBase + ".kind", animation.kind().name().toLowerCase());
            yaml.set(animationBase + ".periodTicks", animation.periodTicks());
            yaml.set(animationBase + ".jingle", animation.jingleId().isBlank() ? null : animation.jingleId());
            if (!animation.primaryPaletteId().isBlank()) {
                yaml.set(animationBase + ".primary", animation.primaryPaletteId());
            }
            if (!animation.secondaryPaletteId().isBlank()) {
                yaml.set(animationBase + ".secondary", animation.secondaryPaletteId());
            }
            if (!animation.backgroundPaletteId().isBlank()) {
                yaml.set(animationBase + ".background", animation.backgroundPaletteId());
            }
            if (animation.windowWidth() > 1) {
                yaml.set(animationBase + ".width", animation.windowWidth());
            }
            if (animation.step() > 1) {
                yaml.set(animationBase + ".step", animation.step());
            }
            if (animation.kind() == StageLightingConfig.AnimationKind.FRAMES) {
                List<Map<String, Object>> frames = new ArrayList<>();
                for (StageLightingConfig.Frame frame : animation.frames()) {
                    Map<String, Object> rawFrame = new LinkedHashMap<>();
                    rawFrame.put("columns", new LinkedHashMap<>(frame.columnSpecs()));
                    frames.add(rawFrame);
                }
                yaml.set(animationBase + ".frames", frames);
            }
        }
    }

    private static void saveJingles(YamlConfiguration yaml, Map<String, StageLightingConfig.Jingle> jingles) {
        String base = "lighting.jingles";
        yaml.set(base, null);
        for (StageLightingConfig.Jingle jingle : jingles.values()) {
            yaml.set(base + "." + jingle.id() + ".commands", jingle.commands());
        }
    }

    private static String linkedJingleId(ConfigurationSection section) {
        if (section == null) {
            return "";
        }
        return section.getString("jingle", section.getString("cue", "")).trim();
    }

    private static Map<String, String> loadSpecMap(ConfigurationSection section) {
        Map<String, String> specMap = new LinkedHashMap<>();
        if (section == null) {
            return specMap;
        }
        for (String id : section.getKeys(false)) {
            String spec = section.getString(id);
            if (spec == null || spec.isBlank()) {
                continue;
            }
            specMap.put(id, spec.trim());
        }
        return specMap;
    }

    private static void readSpecMap(Map<?, ?> raw, Map<String, String> out) {
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            out.put(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    private static List<Integer> parseTriple(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String[] parts = raw.split(",");
        if (parts.length != 3) {
            return List.of();
        }
        try {
            return List.of(
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException ignored) {
            return List.of();
        }
    }

    private static CellParts parseCellMap(Map<?, ?> rawCell) {
        if (rawCell == null) {
            return null;
        }
        Integer dx = asInt(rawCell.get("dx"));
        Integer dy = asInt(rawCell.get("dy"));
        Integer dz = asInt(rawCell.get("dz"));
        if (dx == null || dy == null || dz == null) {
            return null;
        }
        StageLightingConfig.CellRole role = StageLightingConfig.CellRole.fromString(asString(rawCell.get("role")));
        String originalSpec = asString(rawCell.get("original"));
        return new CellParts(dx, dy, dz, role, originalSpec);
    }

    private static CellParts parseCellString(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String[] parts = raw.split(",");
        if (parts.length < 3) {
            return null;
        }
        try {
            int dx = Integer.parseInt(parts[0].trim());
            int dy = Integer.parseInt(parts[1].trim());
            int dz = Integer.parseInt(parts[2].trim());
            StageLightingConfig.CellRole role = parts.length > 3
                    ? StageLightingConfig.CellRole.fromString(parts[3])
                    : StageLightingConfig.CellRole.EMITTER;
            return new CellParts(dx, dy, dz, role, "");
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Integer asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private static String asString(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private record CellParts(int dx, int dy, int dz, StageLightingConfig.CellRole role, String originalSpec) {}
}
