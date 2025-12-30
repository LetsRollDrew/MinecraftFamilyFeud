package io.letsrolldrew.feud.effects.board.selection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.letsrolldrew.feud.board.display.BoardFacing;
import java.util.UUID;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

final class DisplayBoardSelectionStoreTest {

    private static DisplayBoardSelection selection(
            UUID world, double ax, double ay, double az, double bx, double by, double bz) {
        return new DisplayBoardSelection(
                world, new Vector3d(ax, ay, az), new Vector3d(bx, by, bz), BoardFacing.NORTH, new Vector3d(0, 0, -1));
    }

    @Test
    void setGetClear() {
        DisplayBoardSelectionStore store = new DisplayBoardSelectionStore();
        UUID player = UUID.randomUUID();
        UUID world = UUID.randomUUID();
        DisplayBoardSelection sel = selection(world, 0, 64, 0, 3, 64, 2);

        assertNull(store.get(player));

        store.set(player, sel);
        assertEquals(sel, store.get(player));

        store.clear(player);
        assertNull(store.get(player));
    }

    @Test
    void clearAllRemovesAllSelections() {
        DisplayBoardSelectionStore store = new DisplayBoardSelectionStore();
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        DisplayBoardSelection s1 = selection(UUID.randomUUID(), 0, 64, 0, 2, 64, 2);
        DisplayBoardSelection s2 = selection(UUID.randomUUID(), 1, 65, 1, 3, 65, 3);

        store.set(p1, s1);
        store.set(p2, s2);

        store.clearAll();

        assertNull(store.get(p1));
        assertNull(store.get(p2));
    }
}
