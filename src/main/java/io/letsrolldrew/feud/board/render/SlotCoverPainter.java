package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.BoardLayout10x6;
import io.letsrolldrew.feud.board.layout.TilePos;
import java.util.List;

// Draws hidden covers for all slots and marks tiles dirty
public final class SlotCoverPainter {
    private final TileFramebufferStore store;
    private final DirtyTracker dirty;
    private final BigDigitPainter bigDigitPainter;
    private final byte coverTextColor;
    private final byte digitColor;

    public SlotCoverPainter(
            TileFramebufferStore store,
            DirtyTracker dirty,
            BigDigitPainter bigDigitPainter,
            byte coverTextColor,
            byte digitColor) {
        this.store = store;
        this.dirty = dirty;
        this.bigDigitPainter = bigDigitPainter;
        this.coverTextColor = coverTextColor;
        this.digitColor = digitColor;
    }

    public void paintAllHidden() {
        for (int slot = 1; slot <= 8; slot++) {
            paintHidden(slot);
        }
    }

    private void paintHidden(int slot) {
        List<TilePos> tiles = BoardLayout10x6.tilesForSlot(slot);
        // fill all four tiles with the same cover color (points tile matches text until reveal)
        for (TilePos pos : tiles) {
            TileBuffer buf = store.get(pos);
            fill(buf, coverTextColor);
            dirty.markDirty(pos);
        }
        // large slot number centered across the middle two text tiles
        List<TileBuffer> textBuffers =
                tiles.subList(1, 3).stream().map(store::get).toList();
        bigDigitPainter.drawDigitAcrossBuffers(textBuffers, String.valueOf(slot), digitColor);
    }

    private void fill(TileBuffer buf, byte color) {
        for (int y = 0; y < TileBuffer.SIZE; y++) {
            for (int x = 0; x < TileBuffer.SIZE; x++) {
                buf.set(x, y, color);
            }
        }
    }
}
