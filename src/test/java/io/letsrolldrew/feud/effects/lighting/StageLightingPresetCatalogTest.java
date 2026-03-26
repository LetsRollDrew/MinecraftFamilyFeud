package io.letsrolldrew.feud.effects.lighting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

final class StageLightingPresetCatalogTest {

    @Test
    void applyOverwritesManagedIdsAndPreservesCustomEntries() {
        StageLightingConfig base = new StageLightingConfig(
                StageLightingConfig.Arena.unbound(),
                Map.of(),
                Map.of(
                        "custom_palette",
                        new StageLightingConfig.Palette("custom_palette", "BEACON", "RED_STAINED_GLASS"),
                        StageLightingPresetCatalog.PALETTE_DEEP_BLUE,
                        new StageLightingConfig.Palette(
                                StageLightingPresetCatalog.PALETTE_DEEP_BLUE, "BEACON", "WHITE_STAINED_GLASS")),
                Map.of(
                        "custom_mode",
                        new StageLightingConfig.Mode("custom_mode", Map.of("all", "custom_palette"), "custom_jingle"),
                        StageLightingPresetCatalog.MODE_DEFAULT,
                        new StageLightingConfig.Mode(StageLightingPresetCatalog.MODE_DEFAULT, Map.of("all", "bad"), "")),
                Map.of(
                        "custom_animation",
                        new StageLightingConfig.Animation(
                                "custom_animation",
                                StageLightingConfig.AnimationKind.PULSE,
                                9L,
                                List.of(),
                                "custom_palette",
                                StageLightingPresetCatalog.PALETTE_COOL_BLUE,
                                "",
                                1,
                                1,
                                "")),
                Map.of("custom_jingle", new StageLightingConfig.Jingle("custom_jingle", List.of("say custom"))));

        StageLightingConfig applied = new StageLightingPresetCatalog().apply(base);

        assertTrue(applied.palettes().containsKey("custom_palette"));
        assertTrue(applied.modes().containsKey("custom_mode"));
        assertTrue(applied.animations().containsKey("custom_animation"));
        assertTrue(applied.jingles().containsKey("custom_jingle"));

        assertEquals("__original__", applied.modes().get(StageLightingPresetCatalog.MODE_DEFAULT).columnSpecs().get("all"));
        assertEquals(
                "BLUE_STAINED_GLASS",
                applied.palettes().get(StageLightingPresetCatalog.PALETTE_DEEP_BLUE).filterSpec());
        assertTrue(applied.jingles().containsKey(StageLightingPresetCatalog.JINGLE_INTRO_DEMO));
    }
}
