package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.team.TeamId;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class ScorePanelStoreTest {

    @Test
    void saveAndLoadRoundTrip() throws Exception {
        Path dir = Files.createTempDirectory("panel-store-test");
        File storeFile = dir.resolve("panels.yml").toFile();
        storeFile.deleteOnExit();

        ScorePanelStore store = new ScorePanelStore(storeFile);

        UUID worldId = UUID.randomUUID();
        DynamicBoardLayout layout = new DynamicBoardLayout(
                worldId,
                BoardFacing.NORTH,
                new Vector3d(1.0, 65.0, 2.0),
                4.0,
                3.0,
                2.0,
                1.5,
                0.1,
                0.2,
                0.05,
                0.06,
                0.08,
                new Vector3d(0.0, 64.0, 2.0),
                new Vector3d(3.0, 66.0, 2.0));

        store.savePanel("demo:red", TeamId.RED, layout);

        Map<String, StoredScorePanel> loaded = store.loadPanels();
        assertFalse(loaded.isEmpty());
        StoredScorePanel stored = loaded.get("demo:red");
        assertNotNull(stored);
        assertEquals(TeamId.RED, stored.team());
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
