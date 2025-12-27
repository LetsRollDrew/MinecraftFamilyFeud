package io.letsrolldrew.feud.board.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class DynamicBoardLayoutBuilderTest {

    private static DisplayBoardSelection sel(BoardFacing facing, double ax, double ay, double az, double bx, double by, double bz) {
        return new DisplayBoardSelection(
            UUID.randomUUID(),
            new Vector3d(ax, ay, az),
            new Vector3d(bx, by, bz),
            facing,
            new Vector3d(0, 0, 1)
        );
    }

    @Test
    void buildsLayoutForSouthFacingSelection() {
        DisplayBoardSelection selection = sel(BoardFacing.SOUTH, 0, 64, 0, 4, 68, 0);

        DynamicBoardLayoutBuilder.Result result = DynamicBoardLayoutBuilder.build(selection);

        assertTrue(result.success());
        DynamicBoardLayout layout = result.layout();
        assertNotNull(layout);
        assertEquals(5.0, layout.totalWidth(), 0.0001);
        assertEquals(5.0, layout.totalHeight(), 0.0001);
        assertEquals(2.5, layout.cellWidth(), 0.0001);
        assertEquals(1.25, layout.cellHeight(), 0.0001);
        assertEquals(68.0, layout.anchor().y, 0.0001);
        assertEquals(BoardFacing.SOUTH, layout.facing());
    }

    @Test
    void tooNarrowSelectionFails() {
        DisplayBoardSelection selection = sel(BoardFacing.NORTH, 0, 64, 0, 0.5, 66, 0);

        DynamicBoardLayoutBuilder.Result result = DynamicBoardLayoutBuilder.build(selection);

        assertFalse(result.success());
        assertNull(result.layout());
        assertNotNull(result.error());
    }
}
