package io.letsrolldrew.feud.effects.lighting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StageLightingPresetCatalog {
    public static final String PALETTE_COOL_BLUE = "cool_blue";
    public static final String PALETTE_DEEP_BLUE = "deep_blue";
    public static final String PALETTE_WARM_ORANGE = "warm_orange";
    public static final String PALETTE_OFF_DIM = "off_dim";

    public static final String MODE_DEFAULT = "default";
    public static final String MODE_OFF = "off";
    public static final String MODE_DEEP_BLUE = "deep_blue";
    public static final String MODE_WARM = "warm";

    public static final String ANIMATION_PULSE = "pulse";
    public static final String ANIMATION_CHASE = "chase";

    public static final String JINGLE_INTRO_DEMO = "intro_demo";

    public StageLightingConfig apply(StageLightingConfig config) {
        Map<String, StageLightingConfig.Palette> palettes = new LinkedHashMap<>(config.palettes());
        palettes.putAll(managedPalettes());

        Map<String, StageLightingConfig.Mode> modes = new LinkedHashMap<>(config.modes());
        modes.putAll(managedModes());

        Map<String, StageLightingConfig.Animation> animations = new LinkedHashMap<>(config.animations());
        animations.putAll(managedAnimations());

        Map<String, StageLightingConfig.Jingle> jingles = new LinkedHashMap<>(config.jingles());
        jingles.putAll(managedJingles());

        return new StageLightingConfig(config.arena(), config.columns(), palettes, modes, animations, jingles);
    }

    private static Map<String, StageLightingConfig.Palette> managedPalettes() {
        Map<String, StageLightingConfig.Palette> palettes = new LinkedHashMap<>();
        palettes.put(PALETTE_COOL_BLUE,
                new StageLightingConfig.Palette(PALETTE_COOL_BLUE, "SEA_LANTERN", "LIGHT_BLUE_STAINED_GLASS"));
        palettes.put(PALETTE_DEEP_BLUE,
                new StageLightingConfig.Palette(PALETTE_DEEP_BLUE, "SEA_LANTERN", "BLUE_STAINED_GLASS"));
        palettes.put(PALETTE_WARM_ORANGE,
                new StageLightingConfig.Palette(PALETTE_WARM_ORANGE, "SHROOMLIGHT", "ORANGE_STAINED_GLASS"));
        palettes.put(PALETTE_OFF_DIM,
                new StageLightingConfig.Palette(PALETTE_OFF_DIM, "BLACK_CONCRETE", "BLUE_STAINED_GLASS"));
        return palettes;
    }

    private static Map<String, StageLightingConfig.Mode> managedModes() {
        Map<String, StageLightingConfig.Mode> modes = new LinkedHashMap<>();
        modes.put(MODE_DEFAULT,
                new StageLightingConfig.Mode(MODE_DEFAULT, Map.of("all", StageLightingService.ORIGINAL_SPEC), ""));
        modes.put(MODE_OFF, new StageLightingConfig.Mode(MODE_OFF, Map.of("all", PALETTE_OFF_DIM), ""));
        modes.put(MODE_DEEP_BLUE,
                new StageLightingConfig.Mode(MODE_DEEP_BLUE, Map.of("all", PALETTE_DEEP_BLUE), ""));
        modes.put(MODE_WARM,
                new StageLightingConfig.Mode(MODE_WARM, Map.of("all", PALETTE_WARM_ORANGE), JINGLE_INTRO_DEMO));
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
                        PALETTE_COOL_BLUE,
                        PALETTE_DEEP_BLUE,
                        "",
                        1,
                        1,
                        ""));
        animations.put(
                ANIMATION_CHASE,
                new StageLightingConfig.Animation(
                        ANIMATION_CHASE,
                        StageLightingConfig.AnimationKind.SLIDING_WINDOW,
                        4L,
                        List.of(),
                        PALETTE_WARM_ORANGE,
                        "",
                        PALETTE_DEEP_BLUE,
                        2,
                        1,
                        JINGLE_INTRO_DEMO));
        return animations;
    }

    private static Map<String, StageLightingConfig.Jingle> managedJingles() {
        Map<String, StageLightingConfig.Jingle> jingles = new LinkedHashMap<>();
        jingles.put(JINGLE_INTRO_DEMO,
                new StageLightingConfig.Jingle(JINGLE_INTRO_DEMO, List.of("say [FamilyFeud] intro_demo")));
        return jingles;
    }
}