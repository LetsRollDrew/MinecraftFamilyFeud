package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class TimerPanelPlacementTest {

    @Test
    void centersOnSelectionFacingEast() {
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

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();
        double forwardNudge = 0.05;

        Vector3d center = TimerPanelPlacement.computeCenterOnSelection(layout, panelWidth, panelHeight, forwardNudge);

        // Facing EAST: right = +Z, forward = +X
        assertEquals(10.0 + 0.55, center.x, 1e-6);
        assertEquals(20.0 - 3.0, center.y, 1e-6);
        assertEquals(30.0 + 2.0, center.z, 1e-6);
    }

    @Test
    void centersOnSelectionFacingNorth() {
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

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();
        double forwardNudge = 0.1;

        Vector3d center = TimerPanelPlacement.computeCenterOnSelection(layout, panelWidth, panelHeight, forwardNudge);

        // Facing NORTH: right = +X, forward = -Z
        assertEquals(5.0 + 2.0, center.x, 1e-6);
        assertEquals(70.0 - 3.0, center.y, 1e-6);
        assertEquals(5.0 - 0.4, center.z, 1e-6);
    }
}
