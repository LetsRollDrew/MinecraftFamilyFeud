package io.letsrolldrew.feud.board.display.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.List;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class FastMoneyBoardPlacementTest {

    @Test
    void computesQuestionRowsWithDescendingY() {
        DynamicBoardLayout layout = new DynamicBoardLayout(
                UUID.randomUUID(),
                BoardFacing.SOUTH,
                new Vector3d(0, 5, 0),
                6.0,
                6.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.05,
                new Vector3d(0, 0, 0),
                new Vector3d(0, 0, 0));

        FastMoneyBoardPlacement placement = new FastMoneyBoardPlacement();

        FastMoneyBoardPlacement.LayoutAnchors anchors = placement.compute(layout);
        List<FastMoneyBoardPlacement.QuestionRowAnchors> rows = anchors.questions();

        assertEquals(5, rows.size());

        for (int i = 1; i < rows.size(); i++) {
            assertTrue(rows.get(i).p1TextCell().y() < rows.get(i - 1).p1TextCell().y());
        }

        FastMoneyBoardPlacement.QuestionRowAnchors first = rows.get(0);
        double screenRightX = -BoardFacing.SOUTH.rightX();
        double screenRightZ = -BoardFacing.SOUTH.rightZ();

        double p1TextDot =
                (first.p1TextCell().x() * screenRightX) + (first.p1TextCell().z() * screenRightZ);
        double p1PointsDot =
                (first.p1PointsCell().x() * screenRightX) + (first.p1PointsCell().z() * screenRightZ);
        double p2TextDot =
                (first.p2TextCell().x() * screenRightX) + (first.p2TextCell().z() * screenRightZ);
        double p2PointsDot =
                (first.p2PointsCell().x() * screenRightX) + (first.p2PointsCell().z() * screenRightZ);

        assertTrue(p1PointsDot > p1TextDot);
        assertTrue(p2TextDot > p1PointsDot);
        assertTrue(p2PointsDot > p2TextDot);
        assertTrue(first.rowHeight() > 0.0);
        assertTrue(anchors.totals().totalZoneHeight() > 0.0);
    }
}
