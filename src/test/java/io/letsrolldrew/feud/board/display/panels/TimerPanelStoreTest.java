package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class TimerPanelStoreTest {

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        Path dir = Files.createTempDirectory("timer-panel-store-test");
        File storeFile = dir.resolve("timer-panels.yml").toFile();
        storeFile.deleteOnExit();

        TimerPanelStore store = new TimerPanelStore(storeFile);

        UUID worldId = UUID.randomUUID();
        DynamicBoardLayout layout = new DynamicBoardLayout(
                worldId,
                BoardFacing.EAST,
                new Vector3d(5.0, 70.0, -2.0),
                4.5,
                3.5,
                2.0,
                1.0,
                0.1,
                0.2,
                0.03,
                0.04,
                0.09,
                new Vector3d(4.0, 69.0, -3.0),
                new Vector3d(6.0, 71.0, -1.0));

        store.savePanel("demo:timer", layout);

        Map<String, StoredTimerPanel> loaded = store.loadPanels();
        assertFalse(loaded.isEmpty());
        StoredTimerPanel stored = loaded.get("demo:timer");
        assertNotNull(stored);
        assertEquals(layout.worldId(), stored.layout().worldId());
        assertEquals(layout.facing(), stored.layout().facing());
        assertEquals(layout.anchor(), stored.layout().anchor());
        assertEquals(layout.totalWidth(), stored.layout().totalWidth());
        assertEquals(layout.totalHeight(), stored.layout().totalHeight());
        assertEquals(layout.cellWidth(), stored.layout().cellWidth());
        assertEquals(layout.cellHeight(), stored.layout().cellHeight());
        assertEquals(layout.padX(), stored.layout().padX());
        assertEquals(layout.padY(), stored.layout().padY());
        assertEquals(layout.gapX(), stored.layout().gapX());
        assertEquals(layout.gapY(), stored.layout().gapY());
        assertEquals(layout.forwardOffset(), stored.layout().forwardOffset());
        assertEquals(layout.minCorner(), stored.layout().minCorner());
        assertEquals(layout.maxCorner(), stored.layout().maxCorner());
    }
}
