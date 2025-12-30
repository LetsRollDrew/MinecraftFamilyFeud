package io.letsrolldrew.feud.board.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.board.layout.TilePos;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SlotCoverPainterTest {

    @Test
    void paintsAllSlotsAndMarksDirty() {
        TileFramebufferStore store = new TileFramebufferStore();
        DirtyTracker dirty = new DirtyTracker();
        BigDigitPainter big = new BigDigitPainter();
        byte coverText = 5;
        byte digit = 9;
        SlotCoverPainter coverPainter = new SlotCoverPainter(store, dirty, big, coverText, digit);
        coverPainter.paintAllHidden();

        // should have marked 32 tiles (8 slots by 4 tiles)
        Set<TilePos> dirtyTiles = dirty.drain();
        assertEquals(32, dirtyTiles.size());

        // slot 1 text tiles should be painted and contain digit color somewhere
        TileBuffer textBuf = store.get(new TilePos(1, 1));
        boolean foundDigitPixel = false;
        for (int y = 0; y < TileBuffer.SIZE; y++) {
            for (int x = 0; x < TileBuffer.SIZE; x++) {
                byte val = textBuf.get(x, y);
                if (val != 0) {
                    foundDigitPixel = true;
                    break;
                }
            }
            if (foundDigitPixel) break;
        }
        assertTrue(foundDigitPixel);
        // slot 1 points tile should be painted with coverPoints color
        TileBuffer ptsBuf = store.get(new TilePos(4, 1));
        assertNotEquals(0, ptsBuf.get(0, 0));
    }
}
