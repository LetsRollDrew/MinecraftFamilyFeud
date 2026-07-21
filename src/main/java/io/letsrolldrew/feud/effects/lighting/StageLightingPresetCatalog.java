package io.letsrolldrew.feud.effects.lighting;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class StageLightingPresetCatalog {
    public static final String PALETTE_COOL_BLUE = "cool_blue";
    public static final String PALETTE_DEEP_BLUE = "deep_blue";
    public static final String PALETTE_WARM_ORANGE = "warm_orange";
    public static final String PALETTE_OFF_DIM = "off_dim";
    public static final String PALETTE_ORANGE = "orange";
    public static final String PALETTE_LIGHT_BLUE = "light_blue";
    public static final String PALETTE_RED = "red";
    public static final String PALETTE_YELLOW = "yellow";
    public static final String PALETTE_BLUE = "blue";
    public static final String PALETTE_WHITE = "white";
    public static final String PALETTE_DARK_BLUE = "dark_blue";
    public static final String PALETTE_GREEN = "green";
    public static final String PALETTE_PURPLE = "purple";
    public static final String PALETTE_PINK = "pink";
    public static final String PALETTE_SLIT_DIM = "slit_dim";
    public static final String PALETTE_BOARD_OCHRE = "board_ochre";
    public static final String PALETTE_BOARD_BEACON = "board_beacon";
    public static final String PALETTE_BOARD_SHROOMLIGHT = "board_shroomlight";
    public static final String PALETTE_BOARD_PEARLESCENT = "board_pearlescent";
    public static final String PALETTE_BOARD_VERDANT = "board_verdant";

    public static final String MODE_DEFAULT = "default";
    public static final String MODE_OFF = "off";
    public static final String MODE_DEEP_BLUE = "deep_blue";
    public static final String MODE_WARM = "warm";
    public static final String MODE_INTRO_BASE = "intro_base";
    public static final String MODE_FAST_MONEY_BASE = "fast_money_base";
    public static final String MODE_ROUND_WIN_BASE = "round_win_base";
    public static final String MODE_GAME_WON_BASE = "game_won_base";
    public static final String MODE_ZONE_DEBUG = "zone_debug";

    public static final String ANIMATION_PULSE = "pulse";
    public static final String ANIMATION_CHASE = "chase";
    public static final String ANIMATION_INTRO_CYCLE = "intro_cycle";
    public static final String ANIMATION_FAST_MONEY_CYCLE = "fast_money_cycle";
    public static final String ANIMATION_ROUND_WIN_CYCLE = "round_win_cycle";
    public static final String ANIMATION_GAME_WON_CYCLE = "game_won_cycle";
    public static final String ANIMATION_GAME_WON_SLITS = "game_won_slits";

    public static final String JINGLE_INTRO_DEMO = "intro_demo";

    public StageLightingConfig apply(StageLightingConfig config) {
        Map<String, StageLightingConfig.Palette> palettes = new LinkedHashMap<>(managedPalettes());
        palettes.putAll(config.palettes());

        Map<String, StageLightingConfig.Mode> modes = new LinkedHashMap<>(managedModes());
        modes.putAll(config.modes());

        Map<String, StageLightingConfig.Animation> animations = new LinkedHashMap<>(managedAnimations());
        animations.putAll(config.animations());

        Map<String, StageLightingConfig.Jingle> jingles = new LinkedHashMap<>(managedJingles());
        jingles.putAll(config.jingles());

        return new StageLightingConfig(
                config.arena(), config.columns(), config.zones(), palettes, modes, animations, jingles);
    }

    private static Map<String, StageLightingConfig.Palette> managedPalettes() {
        Map<String, StageLightingConfig.Palette> palettes = new LinkedHashMap<>();
        palettes.put(
                PALETTE_COOL_BLUE,
                new StageLightingConfig.Palette(PALETTE_COOL_BLUE, "SEA_LANTERN", "LIGHT_BLUE_STAINED_GLASS"));
        palettes.put(
                PALETTE_DEEP_BLUE,
                new StageLightingConfig.Palette(PALETTE_DEEP_BLUE, "SEA_LANTERN", "BLUE_STAINED_GLASS"));
        palettes.put(
                PALETTE_WARM_ORANGE,
                new StageLightingConfig.Palette(PALETTE_WARM_ORANGE, "SHROOMLIGHT", "ORANGE_STAINED_GLASS"));
        palettes.put(
                PALETTE_OFF_DIM,
                new StageLightingConfig.Palette(PALETTE_OFF_DIM, "BLACK_CONCRETE", "BLUE_STAINED_GLASS"));
        palettes.put(
                PALETTE_ORANGE,
                new StageLightingConfig.Palette(PALETTE_ORANGE, "OCHRE_FROGLIGHT", "ORANGE_STAINED_GLASS"));
        palettes.put(
                PALETTE_LIGHT_BLUE,
                new StageLightingConfig.Palette(PALETTE_LIGHT_BLUE, "BEACON", "BLUE_STAINED_GLASS"));
        palettes.put(PALETTE_RED, new StageLightingConfig.Palette(PALETTE_RED, "SHROOMLIGHT", "RED_STAINED_GLASS"));
        palettes.put(
                PALETTE_YELLOW,
                new StageLightingConfig.Palette(PALETTE_YELLOW, "OCHRE_FROGLIGHT", "YELLOW_STAINED_GLASS"));
        palettes.put(PALETTE_BLUE, new StageLightingConfig.Palette(PALETTE_BLUE, "BEACON", "BLUE_STAINED_GLASS"));
        palettes.put(
                PALETTE_WHITE, new StageLightingConfig.Palette(PALETTE_WHITE, "SEA_LANTERN", "WHITE_STAINED_GLASS"));
        palettes.put(
                PALETTE_DARK_BLUE,
                new StageLightingConfig.Palette(PALETTE_DARK_BLUE, "PEARLESCENT_FROGLIGHT", "BLUE_STAINED_GLASS"));
        palettes.put(
                PALETTE_GREEN,
                new StageLightingConfig.Palette(PALETTE_GREEN, "VERDANT_FROGLIGHT", "GREEN_STAINED_GLASS"));
        palettes.put(
                PALETTE_PURPLE,
                new StageLightingConfig.Palette(PALETTE_PURPLE, "PEARLESCENT_FROGLIGHT", "PURPLE_STAINED_GLASS"));
        palettes.put(
                PALETTE_PINK,
                new StageLightingConfig.Palette(PALETTE_PINK, "PEARLESCENT_FROGLIGHT", "PINK_STAINED_GLASS"));
        palettes.put(
                PALETTE_SLIT_DIM,
                new StageLightingConfig.Palette(PALETTE_SLIT_DIM, "LIGHT_GRAY_CONCRETE", "LIGHT_GRAY_STAINED_GLASS"));
        palettes.put(PALETTE_BOARD_OCHRE, new StageLightingConfig.Palette(PALETTE_BOARD_OCHRE, "OCHRE_FROGLIGHT", ""));
        palettes.put(PALETTE_BOARD_BEACON, new StageLightingConfig.Palette(PALETTE_BOARD_BEACON, "BEACON", ""));
        palettes.put(
                PALETTE_BOARD_SHROOMLIGHT,
                new StageLightingConfig.Palette(PALETTE_BOARD_SHROOMLIGHT, "SHROOMLIGHT", ""));
        palettes.put(
                PALETTE_BOARD_PEARLESCENT,
                new StageLightingConfig.Palette(PALETTE_BOARD_PEARLESCENT, "PEARLESCENT_FROGLIGHT", ""));
        palettes.put(
                PALETTE_BOARD_VERDANT, new StageLightingConfig.Palette(PALETTE_BOARD_VERDANT, "VERDANT_FROGLIGHT", ""));
        return palettes;
    }

    private static Map<String, StageLightingConfig.Mode> managedModes() {
        Map<String, StageLightingConfig.Mode> modes = new LinkedHashMap<>();
        modes.put(
                MODE_DEFAULT,
                new StageLightingConfig.Mode(
                        MODE_DEFAULT,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_LIGHT_BLUE,
                                "inside", PALETTE_RED,
                                "windows", PALETTE_ORANGE,
                                "slits", PALETTE_LIGHT_BLUE,
                                "roof", PALETTE_ORANGE,
                                "pillar", PALETTE_ORANGE,
                                "back_wall", PALETTE_LIGHT_BLUE,
                                "board", PALETTE_BOARD_OCHRE),
                        ""));
        modes.put(MODE_OFF, new StageLightingConfig.Mode(MODE_OFF, Map.of("all", PALETTE_OFF_DIM), ""));
        modes.put(MODE_DEEP_BLUE, new StageLightingConfig.Mode(MODE_DEEP_BLUE, Map.of("all", PALETTE_DEEP_BLUE), ""));
        modes.put(
                MODE_WARM,
                new StageLightingConfig.Mode(MODE_WARM, Map.of("all", PALETTE_WARM_ORANGE), JINGLE_INTRO_DEMO));
        modes.put(
                MODE_INTRO_BASE,
                new StageLightingConfig.Mode(
                        MODE_INTRO_BASE,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_RED,
                                "inside", PALETTE_RED,
                                "windows", PALETTE_RED,
                                "slits", PALETTE_WHITE,
                                "roof", PALETTE_ORANGE,
                                "pillar", PALETTE_RED,
                                "back_wall", PALETTE_RED,
                                "board", PALETTE_BOARD_SHROOMLIGHT),
                        ""));
        modes.put(
                MODE_FAST_MONEY_BASE,
                new StageLightingConfig.Mode(
                        MODE_FAST_MONEY_BASE,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_LIGHT_BLUE,
                                "inside", PALETTE_LIGHT_BLUE,
                                "windows", PALETTE_DARK_BLUE,
                                "slits", PALETTE_DARK_BLUE,
                                "roof", PALETTE_LIGHT_BLUE,
                                "pillar", PALETTE_LIGHT_BLUE,
                                "back_wall", PALETTE_LIGHT_BLUE,
                                "board", PALETTE_BOARD_BEACON),
                        ""));
        modes.put(
                MODE_ROUND_WIN_BASE,
                new StageLightingConfig.Mode(
                        MODE_ROUND_WIN_BASE,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_RED,
                                "inside", PALETTE_RED,
                                "windows", PALETTE_ORANGE,
                                "slits", PALETTE_LIGHT_BLUE,
                                "roof", PALETTE_ORANGE,
                                "pillar", PALETTE_ORANGE,
                                "back_wall", PALETTE_LIGHT_BLUE,
                                "board", PALETTE_BOARD_OCHRE),
                        ""));
        modes.put(
                MODE_GAME_WON_BASE,
                new StageLightingConfig.Mode(
                        MODE_GAME_WON_BASE,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_LIGHT_BLUE,
                                "inside", PALETTE_LIGHT_BLUE,
                                "windows", PALETTE_RED,
                                "slits", PALETTE_WHITE,
                                "roof", PALETTE_PINK,
                                "pillar", PALETTE_BLUE,
                                "back_wall", PALETTE_RED,
                                "board", PALETTE_BOARD_BEACON),
                        ""));
        modes.put(
                MODE_ZONE_DEBUG,
                new StageLightingConfig.Mode(
                        MODE_ZONE_DEBUG,
                        specs(
                                "all", StageLightingService.ORIGINAL_SPEC,
                                "arch", PALETTE_LIGHT_BLUE,
                                "inside", PALETTE_RED,
                                "windows", PALETTE_ORANGE,
                                "slits", PALETTE_LIGHT_BLUE,
                                "roof", PALETTE_ORANGE,
                                "pillar", PALETTE_ORANGE,
                                "back_wall", PALETTE_LIGHT_BLUE,
                                "board", PALETTE_BOARD_OCHRE),
                        ""));
        return modes;
    }

    private static Map<String, StageLightingConfig.Animation> managedAnimations() {
        Map<String, StageLightingConfig.Animation> animations = new LinkedHashMap<>();
        animations.put(
                ANIMATION_PULSE,
                new StageLightingConfig.Animation(
                        ANIMATION_PULSE,
                        StageLightingConfig.AnimationKind.COLUMN_ALTERNATE,
                        6L,
                        List.of(),
                        PALETTE_LIGHT_BLUE,
                        PALETTE_DARK_BLUE,
                        "",
                        1,
                        2,
                        ""));
        animations.put(
                ANIMATION_CHASE,
                new StageLightingConfig.Animation(
                        ANIMATION_CHASE,
                        StageLightingConfig.AnimationKind.SLIDING_WINDOW,
                        4L,
                        List.of(),
                        PALETTE_ORANGE,
                        "",
                        PALETTE_LIGHT_BLUE,
                        2,
                        2,
                        JINGLE_INTRO_DEMO));
        animations.put(ANIMATION_INTRO_CYCLE, framesAnimation(ANIMATION_INTRO_CYCLE, 6L, introFrames()));
        animations.put(ANIMATION_FAST_MONEY_CYCLE, framesAnimation(ANIMATION_FAST_MONEY_CYCLE, 5L, fastMoneyFrames()));
        animations.put(ANIMATION_ROUND_WIN_CYCLE, framesAnimation(ANIMATION_ROUND_WIN_CYCLE, 5L, roundWinFrames()));
        animations.put(ANIMATION_GAME_WON_CYCLE, framesAnimation(ANIMATION_GAME_WON_CYCLE, 4L, gameWonFrames()));
        animations.put(ANIMATION_GAME_WON_SLITS, framesAnimation(ANIMATION_GAME_WON_SLITS, 4L, gameWonSlitsFrames()));
        return animations;
    }

    private static Map<String, StageLightingConfig.Jingle> managedJingles() {
        Map<String, StageLightingConfig.Jingle> jingles = new LinkedHashMap<>();
        jingles.put(
                JINGLE_INTRO_DEMO,
                new StageLightingConfig.Jingle(JINGLE_INTRO_DEMO, List.of("say [FamilyFeud] intro_demo")));
        return jingles;
    }

    private static StageLightingConfig.Animation framesAnimation(
            String id, long periodTicks, List<StageLightingConfig.Frame> frames) {
        return new StageLightingConfig.Animation(
                id, StageLightingConfig.AnimationKind.FRAMES, periodTicks, frames, "", "", "", 1, 1, "");
    }

    private static List<StageLightingConfig.Frame> introFrames() {
        return List.of(
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "arch", PALETTE_RED,
                        "inside", PALETTE_RED,
                        "windows", PALETTE_RED,
                        "slits", PALETTE_WHITE,
                        "roof", PALETTE_ORANGE,
                        "pillar", PALETTE_RED,
                        "back_wall", PALETTE_RED,
                        "board", PALETTE_BOARD_SHROOMLIGHT),
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "arch", PALETTE_YELLOW,
                        "inside", PALETTE_YELLOW,
                        "windows", PALETTE_YELLOW,
                        "slits", PALETTE_WHITE,
                        "roof", PALETTE_PINK,
                        "pillar", PALETTE_YELLOW,
                        "back_wall", PALETTE_YELLOW,
                        "board", PALETTE_BOARD_SHROOMLIGHT),
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "arch", PALETTE_BLUE,
                        "inside", PALETTE_BLUE,
                        "windows", PALETTE_BLUE,
                        "slits", PALETTE_WHITE,
                        "roof", PALETTE_BLUE,
                        "pillar", PALETTE_BLUE,
                        "back_wall", PALETTE_BLUE,
                        "board", PALETTE_BOARD_SHROOMLIGHT));
    }

    private static List<StageLightingConfig.Frame> fastMoneyFrames() {
        return List.of(
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "arch", PALETTE_LIGHT_BLUE,
                        "inside", PALETTE_LIGHT_BLUE,
                        "windows", PALETTE_DARK_BLUE,
                        "slits:odd", PALETTE_DARK_BLUE,
                        "slits:even", PALETTE_LIGHT_BLUE,
                        "roof", PALETTE_LIGHT_BLUE,
                        "pillar", PALETTE_LIGHT_BLUE,
                        "back_wall", PALETTE_LIGHT_BLUE,
                        "board", PALETTE_BOARD_BEACON),
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "arch", PALETTE_DARK_BLUE,
                        "inside", PALETTE_DARK_BLUE,
                        "windows", PALETTE_LIGHT_BLUE,
                        "slits:odd", PALETTE_LIGHT_BLUE,
                        "slits:even", PALETTE_DARK_BLUE,
                        "roof", PALETTE_LIGHT_BLUE,
                        "pillar", PALETTE_DARK_BLUE,
                        "back_wall", PALETTE_DARK_BLUE,
                        "board", PALETTE_BOARD_BEACON));
    }

    private static List<StageLightingConfig.Frame> roundWinFrames() {
        List<String> rainbow = List.of(
                PALETTE_RED, PALETTE_ORANGE, PALETTE_YELLOW, PALETTE_GREEN, PALETTE_BLUE, PALETTE_PURPLE, PALETTE_PINK);
        List<StageLightingConfig.Frame> frames = new LinkedList<>();
        for (String color : rainbow) {
            frames.add(frame(
                    "all", StageLightingService.ORIGINAL_SPEC,
                    "arch", color,
                    "inside", color,
                    "windows", PALETTE_ORANGE,
                    "slits", PALETTE_LIGHT_BLUE,
                    "roof", PALETTE_ORANGE,
                    "pillar", PALETTE_ORANGE,
                    "back_wall", PALETTE_LIGHT_BLUE,
                    "board", PALETTE_BOARD_OCHRE));
        }
        return frames;
    }

    private static List<StageLightingConfig.Frame> gameWonFrames() {
        List<String> archCycle = List.of(PALETTE_LIGHT_BLUE, PALETTE_PINK, PALETTE_PURPLE, PALETTE_PINK);
        List<String> rainbow = List.of(
                PALETTE_RED, PALETTE_ORANGE, PALETTE_YELLOW, PALETTE_GREEN, PALETTE_BLUE, PALETTE_PURPLE, PALETTE_PINK);
        List<String> roofCycle = List.of(PALETTE_PINK, PALETTE_BLUE, PALETTE_ORANGE);
        List<String> boardCycle =
                List.of(PALETTE_BOARD_BEACON, PALETTE_BOARD_PEARLESCENT, PALETTE_BOARD_VERDANT, PALETTE_BOARD_OCHRE);

        List<StageLightingConfig.Frame> frames = new LinkedList<>();
        int frameCount = 14;
        for (int i = 0; i < frameCount; i++) {
            boolean even = (i % 2) == 0;
            frames.add(frame(
                    "all", StageLightingService.ORIGINAL_SPEC,
                    "arch", archCycle.get(i % archCycle.size()),
                    "inside", archCycle.get(i % archCycle.size()),
                    "windows", rainbow.get(i % rainbow.size()),
                    "slits:odd", even ? PALETTE_WHITE : PALETTE_SLIT_DIM,
                    "slits:even", even ? PALETTE_SLIT_DIM : PALETTE_WHITE,
                    "roof", roofCycle.get(i % roofCycle.size()),
                    "pillar", PALETTE_BLUE,
                    "back_wall", rainbow.get(i % rainbow.size()),
                    "board", boardCycle.get(i % boardCycle.size())));
        }
        return frames;
    }

    private static List<StageLightingConfig.Frame> gameWonSlitsFrames() {
        return List.of(
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "slits:odd", PALETTE_WHITE,
                        "slits:even", PALETTE_SLIT_DIM),
                frame(
                        "all", StageLightingService.ORIGINAL_SPEC,
                        "slits:odd", PALETTE_SLIT_DIM,
                        "slits:even", PALETTE_WHITE));
    }

    private static StageLightingConfig.Frame frame(String... entries) {
        return new StageLightingConfig.Frame(specs(entries));
    }

    private static Map<String, String> specs(String... entries) {
        Map<String, String> out = new LinkedHashMap<>();
        if (entries == null || entries.length == 0) {
            return out;
        }
        if ((entries.length % 2) != 0) {
            throw new IllegalArgumentException("entries must be key/value pairs");
        }
        for (int i = 0; i < entries.length; i += 2) {
            String key = entries[i];
            String value = entries[i + 1];
            if (key == null || key.isBlank() || value == null || value.isBlank()) {
                continue;
            }
            out.put(key, value);
        }
        return out;
    }
}
