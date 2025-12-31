package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class ScorePanelPlacementTest {

    @Test
    void computesCentersFacingEast() {
        DynamicBoardLayout layout = new DynamicBoardLayout(
                UUID.randomUUID(),
                BoardFacing.EAST,
                new Vector3d(10.0, 20.0, 30.0),
                4.0,
                6.0,
                2.0,
                1.0,
                0,
                0,
                0,
                0,
                0.5,
                new Vector3d(),
                new Vector3d());

        ScorePanelPlacement.Panels panels = ScorePanelPlacement.compute(layout, 2.0, 1.0, 0.5, 0.05);

        // facing EAST: right axis is +Z, forward should be +X
        assertEquals(10.55, panels.leftCenter().x, 1e-6);
        assertEquals(17.0, panels.leftCenter().y, 1e-6);
        assertEquals(28.5, panels.leftCenter().z, 1e-6);

        assertEquals(10.55, panels.rightCenter().x, 1e-6);
        assertEquals(17.0, panels.rightCenter().y, 1e-6);
        assertEquals(35.5, panels.rightCenter().z, 1e-6);
    }

    @Test
    void computesCentersFacingSouth() {
        DynamicBoardLayout layout = new DynamicBoardLayout(
                UUID.randomUUID(),
                BoardFacing.SOUTH,
                new Vector3d(0.0, 64.0, 0.0),
                4.0,
                6.0,
                2.0,
                1.0,
                0,
                0,
                0,
                0,
                0.25,
                new Vector3d(),
                new Vector3d());

        ScorePanelPlacement.Panels panels = ScorePanelPlacement.compute(layout, 1.0, 1.0, 0.25, 0.05);

        // facing SOUTH: right axis is -X, forward should be +Z
        assertEquals(0.75, panels.leftCenter().x, 1e-6);
        assertEquals(61.0, panels.leftCenter().y, 1e-6);
        assertEquals(0.3, panels.leftCenter().z, 1e-6);

        assertEquals(-4.75, panels.rightCenter().x, 1e-6);
        assertEquals(61.0, panels.rightCenter().y, 1e-6);
        assertEquals(0.3, panels.rightCenter().z, 1e-6);
    }
}
