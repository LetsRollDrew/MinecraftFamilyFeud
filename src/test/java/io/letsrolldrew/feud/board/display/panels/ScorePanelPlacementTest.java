package io.letsrolldrew.feud.board.display.panels;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.team.TeamId;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class ScorePanelPlacementTest {

    @Test
    void computesCentersFacingEast_whenPanelMatchesSelectionWidth_shiftIsZero() {
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

        // panel covers the full selection width, maxShift becomes 0, BLUE and RED centers match
        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();

        Vector3d blue = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.BLUE);
        Vector3d red = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.RED);

        // facing EAST: forward = +X, right = +Z
        // right = totalW/2 = 2.0, centerUp = -(totalH/2) = -3.0, forward = 0.5 + 0.05 = 0.55
        assertEquals(10.55, blue.x, 1e-6);
        assertEquals(17.0, blue.y, 1e-6);
        assertEquals(32.0, blue.z, 1e-6);

        assertEquals(10.55, red.x, 1e-6);
        assertEquals(17.0, red.y, 1e-6);
        assertEquals(32.0, red.z, 1e-6);
    }

    @Test
    void computesCentersFacingEast_whenPanelIsNarrower_blueAndRedSplitLeftRight() {
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

        // make the panel narrower than the selection so shift > 0
        double panelWidth = 1.0;
        double panelHeight = layout.totalHeight();

        Vector3d blue = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.BLUE);
        Vector3d red = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.RED);

        // shared vertical center for both
        assertEquals(17.0, blue.y, 1e-6);
        assertEquals(17.0, red.y, 1e-6);

        // forward still +X, both have same X
        assertEquals(10.55, blue.x, 1e-6);
        assertEquals(10.55, red.x, 1e-6);

        // EAST right axis is +Z, so "right" shows up in Z.
        // totalW=4, centerRight=2
        // halfW=2, halfPanel=0.5, maxShift=1.5
        // shift = 1.5 * 0.55 = 0.825
        // rightBlue = 2 - 0.825 = 1.175, z = 30 + 1.175 = 31.175
        // rightRed  = 2 + 0.825 = 2.825, z = 30 + 2.825 = 32.825
        assertEquals(31.175, blue.z, 1e-6);
        assertEquals(32.825, red.z, 1e-6);
    }

    @Test
    void computesCentersFacingSouth_whenPanelMatchesSelectionWidth_shiftIsZero() {
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

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();

        Vector3d blue = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.BLUE);
        Vector3d red = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, 0.05, TeamId.RED);

        // facing SOUTH: forward = +Z, right = -X
        // right = totalW/2 = 2.0, centerUp = -(totalH/2) = -3.0, forward = 0.25 + 0.05 = 0.30
        assertEquals(-2.0, blue.x, 1e-6);
        assertEquals(61.0, blue.y, 1e-6);
        assertEquals(0.3, blue.z, 1e-6);

        assertEquals(-2.0, red.x, 1e-6);
        assertEquals(61.0, red.y, 1e-6);
        assertEquals(0.3, red.z, 1e-6);
    }
}
