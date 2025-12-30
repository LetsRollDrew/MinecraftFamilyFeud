package io.letsrolldrew.feud.board.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TileBufferTest {

    @Test
    void setAndGetPixel() {
        TileBuffer buf = new TileBuffer();
        buf.set(0, 0, (byte) 5);
        buf.set(127, 127, (byte) 9);
        assertEquals(5, buf.get(0, 0));
        assertEquals(9, buf.get(127, 127));
    }

    @Test
    void rejectsOutOfBounds() {
        TileBuffer buf = new TileBuffer();
        assertThrows(IllegalArgumentException.class, () -> buf.set(-1, 0, (byte) 1));
        assertThrows(IllegalArgumentException.class, () -> buf.get(128, 0));
    }
}
