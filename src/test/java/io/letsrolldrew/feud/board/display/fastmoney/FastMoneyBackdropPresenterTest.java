package io.letsrolldrew.feud.board.display.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class FastMoneyBackdropPresenterTest {

    @Test
    void computesCenterFromBounds() {
        DynamicBoardLayout layout = new DynamicBoardLayout(
                java.util.UUID.randomUUID(),
                BoardFacing.SOUTH,
                new Vector3d(0, 0, 0),
                10.0,
                5.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                new Vector3d(-1, 2, -3),
                new Vector3d(3, 6, 1));

        double centerX = (layout.minCorner().x + layout.maxCorner().x) / 2.0;
        double centerY = (layout.minCorner().y + layout.maxCorner().y) / 2.0;
        double centerZ = (layout.minCorner().z + layout.maxCorner().z) / 2.0;

        assertEquals(1.0, centerX, 0.0001);
        assertEquals(4.0, centerY, 0.0001);
        assertEquals(-1.0, centerZ, 0.0001);
    }
}
