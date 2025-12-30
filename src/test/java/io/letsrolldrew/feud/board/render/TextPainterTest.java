package io.letsrolldrew.feud.board.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TextPainterTest {

    @Test
    void drawsDigitsAndQuestionMark() {
        TileBuffer buf = new TileBuffer();
        TextPainter painter = new TextPainter();
        painter.drawString(buf, 0, 0, "1?2A", (byte) 7);
        int colored = 0;
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 20; x++) {
                if (buf.get(x, y) != 0) {
                    colored++;
                }
            }
        }
        assertNotEquals(0, colored); // something was drawn
        assertEquals(0, buf.get(30, 10)); // untouched area remains zero
    }
}
