package io.letsrolldrew.feud.board.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;

class BoardLayout10x6Test {

    @Test
    void slotMappingLeftColumn() {
        var slot1 = BoardLayout10x6.tilesForSlot(1);
        assertEquals(4, slot1.size());
        assertEquals(new TilePos(1, 1), slot1.get(0));
        assertEquals(new TilePos(4, 1), slot1.get(3));

        var slot4 = BoardLayout10x6.tilesForSlot(4);
        assertEquals(new TilePos(1, 4), slot4.get(0));
        assertEquals(new TilePos(4, 4), slot4.get(3));
    }

    @Test
    void slotMappingRightColumn() {
        var slot5 = BoardLayout10x6.tilesForSlot(5);
        assertEquals(new TilePos(5, 1), slot5.get(0));
        assertEquals(new TilePos(8, 1), slot5.get(3));

        var slot8 = BoardLayout10x6.tilesForSlot(8);
        assertEquals(new TilePos(5, 4), slot8.get(0));
        assertEquals(new TilePos(8, 4), slot8.get(3));
    }

    @Test
    void borderAndInteriorSizes() {
        Set<TilePos> border = BoardLayout10x6.borderTiles();
        Set<TilePos> interior = BoardLayout10x6.interiorTiles();
        assertEquals(28, border.size()); // top(10)+bottom(10)+sides(4 rows *2)
        assertEquals(32, interior.size()); // 8x4
    }

    @Test
    void rejectsInvalidSlot() {
        assertThrows(IllegalArgumentException.class, () -> BoardLayout10x6.tilesForSlot(0));
        assertThrows(IllegalArgumentException.class, () -> BoardLayout10x6.tilesForSlot(9));
    }
}
