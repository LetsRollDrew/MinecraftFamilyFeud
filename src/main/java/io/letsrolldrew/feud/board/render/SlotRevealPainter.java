package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.BoardLayout10x6;
import io.letsrolldrew.feud.board.layout.TilePos;

import java.awt.Color;
import java.util.List;

@SuppressWarnings("removal") // MapPalette.matchColor is deprecated, fix later
//Renders a revealed answer into the 3 text tiles + points into the points tile
public final class SlotRevealPainter {
    private final TileFramebufferStore store;
    private final DirtyTracker dirty;
    private final TextPainter textPainter;

    private final byte answerColor;
    private final byte pointsColor;
    private final byte bgColor;

    public SlotRevealPainter(TileFramebufferStore store, DirtyTracker dirty, BoardRenderer renderer) {
        this(
            store,
            dirty,
            new TextPainter(),
            org.bukkit.map.MapPalette.matchColor(Color.WHITE),
            org.bukkit.map.MapPalette.matchColor(Color.WHITE),
            renderer.getCoverTextColor()
        );
    }

    public SlotRevealPainter(TileFramebufferStore store, DirtyTracker dirty, TextPainter textPainter, byte answerColor, byte pointsColor, byte bgColor) {
        this.store = store;
        this.dirty = dirty;
        this.textPainter = textPainter;
        this.answerColor = answerColor;
        this.pointsColor = pointsColor;
        this.bgColor = bgColor;
    }

    public void reveal(int slot, String answer, int points) {
        List<TilePos> tiles = BoardLayout10x6.tilesForSlot(slot);
        // clear text tiles
        List<TilePos> textTiles = tiles.subList(0, 3);
        for (TilePos pos : textTiles) {
            fill(store.get(pos), bgColor);
            dirty.markDirty(pos);
        }
        // draw answer centered across the three text tiles (scaled to fit)
        List<TileBuffer> buffers = textTiles.stream().map(store::get).toList();
        drawAcross(buffers, answer == null ? "" : answer.toUpperCase(), answerColor);

        // points tile
        TilePos ptsPos = tiles.get(tiles.size() - 1);
        TileBuffer ptsBuf = store.get(ptsPos);
        fill(ptsBuf, bgColor);
        drawAcross(List.of(ptsBuf), String.valueOf(points), pointsColor);
        dirty.markDirty(ptsPos);
    }

    private void drawAcross(List<TileBuffer> buffers, String text, byte color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        int len = text.length();
        int glyphW = textPainter.glyphWidth();
        int glyphH = textPainter.glyphHeight();
        int spacing = textPainter.glyphSpacing();

        int totalWidth = buffers.size() * TileBuffer.SIZE;
        int baseCharWidth = glyphW + spacing;
        // scale up while fitting width/height; cap to avoid giant text
        int scaleByWidth = totalWidth / Math.max(1, len * baseCharWidth);
        int scaleByHeight = TileBuffer.SIZE / glyphH;
        int scale = Math.max(1, Math.min(Math.min(scaleByWidth, scaleByHeight), 4));

        int charWidth = baseCharWidth * scale;
        int textWidth = len * charWidth - spacing * scale; // drop trailing spacing
        // truncate if still too wide
        while (textWidth > totalWidth && len > 0) {
            len--;
            text = text.substring(0, len);
            textWidth = len * charWidth - spacing * scale;
        }
        if (len == 0) {
            return;
        }

        int glyphHeightScaled = glyphH * scale;
        int startX = (totalWidth - textWidth) / 2;
        int startY = (TileBuffer.SIZE - glyphHeightScaled) / 2;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            boolean[][] glyph = textPainter.glyphFor(ch);
            if (glyph == null) {
                continue;
            }
            int glyphWidth = glyph[0].length;
            for (int gy = 0; gy < glyph.length; gy++) {
                for (int gx = 0; gx < glyphWidth; gx++) {
                    if (!glyph[gy][gx]) {
                        continue;
                    }
                    for (int sy = 0; sy < scale; sy++) {
                        for (int sx = 0; sx < scale; sx++) {
                            int px = startX + i * charWidth + gx * scale + sx;
                            int py = startY + gy * scale + sy;
                            int bufIndex = px / TileBuffer.SIZE;
                            int tileX = px % TileBuffer.SIZE;
                            if (bufIndex < 0 || bufIndex >= buffers.size()) {
                                continue;
                            }
                            buffers.get(bufIndex).set(tileX, py, color);
                        }
                    }
                }
            }
        }
    }

    private void fill(TileBuffer buf, byte color) {
        for (int y = 0; y < TileBuffer.SIZE; y++) {
            for (int x = 0; x < TileBuffer.SIZE; x++) {
                buf.set(x, y, color);
            }
        }
    }
}
