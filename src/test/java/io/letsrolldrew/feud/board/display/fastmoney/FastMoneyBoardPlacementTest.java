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
    void computesSixRowsWithDescendingY() {
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

        List<FastMoneyBoardPlacement.RowAnchors> rows = placement.compute(layout);

        assertEquals(6, rows.size());
        // y decreases per row
        for (int i = 1; i < rows.size(); i++) {
            assertTrue(rows.get(i).textCell().y() < rows.get(i - 1).textCell().y());
        }
        FastMoneyBoardPlacement.RowAnchors first = rows.get(0);
        double rightX = BoardFacing.SOUTH.rightX();
        double rightZ = BoardFacing.SOUTH.rightZ();
        double textDot = (first.textCell().x() * rightX) + (first.textCell().z() * rightZ);
        double pointsDot =
                (first.pointsCell().x() * rightX) + (first.pointsCell().z() * rightZ);
        assertTrue(pointsDot > textDot);
    }
}
