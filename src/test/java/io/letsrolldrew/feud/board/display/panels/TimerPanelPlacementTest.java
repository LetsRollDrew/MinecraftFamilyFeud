package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class TimerPanelPlacementTest {

    @Test
    void centersAboveBoardFacingEast() {
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

        Vector3d center = TimerPanelPlacement.computeCenter(layout, 1.0, 2.0, 0.05);

        // facing EAST: right axis is +Z, forward should be +X
        assertEquals(10.55, center.x, 1e-6);
        assertEquals(22.0, center.y, 1e-6);
        assertEquals(32.0, center.z, 1e-6);
    }

    @Test
    void centersAboveBoardFacingNorth() {
        DynamicBoardLayout layout = new DynamicBoardLayout(
                UUID.randomUUID(),
                BoardFacing.NORTH,
                new Vector3d(5.0, 70.0, 5.0),
                4.0,
                6.0,
                2.0,
                1.0,
                0,
                0,
                0,
                0,
                0.3,
                new Vector3d(),
                new Vector3d());

        Vector3d center = TimerPanelPlacement.computeCenter(layout, 1.0, 2.0, 0.1);

        // facing NORTH: right axis is +X, forward should be -Z
        assertEquals(7.0, center.x, 1e-6);
        assertEquals(72.0, center.y, 1e-6);
        assertEquals(4.6, center.z, 1e-6);
    }
}
