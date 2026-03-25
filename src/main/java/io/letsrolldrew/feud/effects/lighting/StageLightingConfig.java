package io.letsrolldrew.feud.effects.lighting;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StageLightingConfig(
        Arena arena,
        Map<String, Column> columns,
        Map<String, Palette> palettes,
        Map<String, Mode> modes,
        Map<String, Animation> animations,
        Map<String, Jingle> jingles) {
    public StageLightingConfig {
        arena = arena == null ? Arena.unbound() : arena;
        columns = columns == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(columns));
        palettes = palettes == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(palettes));
        modes = modes == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(modes));
        animations = animations == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(animations));
        jingles = jingles == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(jingles));
    }

    public StageLightingConfig withArena(Arena nextArena) {
        return new StageLightingConfig(nextArena, columns, palettes, modes, animations, jingles);
    }

    public StageLightingConfig withColumns(Map<String, Column> nextColumns) {
        return new StageLightingConfig(arena, nextColumns, palettes, modes, animations, jingles);
    }

    public static List<String> defaultPalette() {
        return List.of(
                "SEA_LANTERN",
                "BEACON",
                "SHROOMLIGHT",
                "OCHRE_FROGLIGHT",
                "VERDANT_FROGLIGHT",
                "PEARLESCENT_FROGLIGHT",
                "WHITE_STAINED_GLASS",
                "LIGHT_BLUE_STAINED_GLASS",
                "BLUE_STAINED_GLASS",
                "RED_STAINED_GLASS",
                "ORANGE_STAINED_GLASS",
                "YELLOW_STAINED_GLASS");
    }

    public enum ScanAxis {
        X,
        Z;

        public static ScanAxis fromString(String raw) {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    public enum CellRole {
        EMITTER,
        FILTER;

        public static CellRole fromString(String raw) {
            if (raw == null || raw.isBlank()) {
                return EMITTER;
            }
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return EMITTER;
            }
        }
    }

    public enum AnimationKind {
        FRAMES,
        PULSE,
        COLUMN_ALTERNATE,
        SLIDING_WINDOW;

        public static AnimationKind fromString(String raw) {
            if (raw == null || raw.isBlank()) {
                return FRAMES;
            }
            try {
                return valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                return FRAMES;
            }
        }
    }

    public record Arena(
            String world,
            int centerX,
            int centerY,
            int centerZ,
            ScanAxis axis,
            int radiusX,
            int radiusZ,
            int yDown,
            int yUp,
            List<String> palette) {
        public Arena {
            world = world == null ? "" : world.trim();
            axis = axis == null ? ScanAxis.X : axis;
            radiusX = Math.max(0, radiusX);
            radiusZ = Math.max(0, radiusZ);
            yDown = Math.max(0, yDown);
            yUp = Math.max(0, yUp);
            palette = palette == null || palette.isEmpty() ? defaultPalette() : List.copyOf(palette);
        }

        public static Arena unbound() {
            return new Arena("", 0, 0, 0, ScanAxis.X, 25, 25, 16, 15, defaultPalette());
        }

        public boolean isBound() {
            return !world.isBlank();
        }
    }

    public record Column(String id, List<Cell> cells) {
        public Column {
            id = Objects.requireNonNull(id, "id").trim();
            cells = cells == null ? List.of() : List.copyOf(cells);
        }
    }

    public record Cell(int dx, int dy, int dz, CellRole role, String originalSpec) {
        public Cell {
            role = role == null ? CellRole.EMITTER : role;
            originalSpec = originalSpec == null ? "" : originalSpec.trim();
        }
    }

    public record Palette(String id, String emitterSpec, String filterSpec) {
        public Palette {
            id = Objects.requireNonNull(id, "id").trim();
            emitterSpec = emitterSpec == null ? "" : emitterSpec.trim();
            filterSpec = filterSpec == null ? "" : filterSpec.trim();
        }
    }

    public record Mode(String id, Map<String, String> columnSpecs, String jingleId) {
        public Mode {
            id = Objects.requireNonNull(id, "id").trim();
            columnSpecs =
                    columnSpecs == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(columnSpecs));
            jingleId = jingleId == null ? "" : jingleId.trim();
        }
    }

    public record Animation(
            String id,
            AnimationKind kind,
            long periodTicks,
            List<Frame> frames,
            String primaryPaletteId,
            String secondaryPaletteId,
            String backgroundPaletteId,
            int windowWidth,
            int step,
            String jingleId) {
        public Animation {
            id = Objects.requireNonNull(id, "id").trim();
            kind = kind == null ? AnimationKind.FRAMES : kind;
            periodTicks = Math.max(1L, periodTicks);
            frames = frames == null ? List.of() : List.copyOf(frames);
            primaryPaletteId = primaryPaletteId == null ? "" : primaryPaletteId.trim();
            secondaryPaletteId = secondaryPaletteId == null ? "" : secondaryPaletteId.trim();
            backgroundPaletteId = backgroundPaletteId == null ? "" : backgroundPaletteId.trim();
            windowWidth = Math.max(1, windowWidth);
            step = Math.max(1, step);
            jingleId = jingleId == null ? "" : jingleId.trim();
        }
    }

    public record Frame(Map<String, String> columnSpecs) {
        public Frame {
            columnSpecs =
                    columnSpecs == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(columnSpecs));
        }
    }

    public record Jingle(String id, List<String> commands) {
        public Jingle {
            id = Objects.requireNonNull(id, "id").trim();
            commands = commands == null ? List.of() : List.copyOf(commands);
        }
    }
}
