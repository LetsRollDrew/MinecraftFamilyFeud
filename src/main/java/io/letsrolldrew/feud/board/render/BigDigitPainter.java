package io.letsrolldrew.feud.board.render;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

    // Draws a large single digit across multiple tiles
    // For Example:
    // Current layout 10x6 interior 8x4: Q Q Q P (points tile covered like Q) then
    // we pass the middle 2 text tiles to center the digit between them "Q QNum BerQ Q"
    // If expanded to Q Q Q Q Q, we would pass all 5 text tiles so the digit centers on the middle Q
public final class BigDigitPainter {
    private static final int GLYPH_W = 5;
    private static final int GLYPH_H = 7;
    private static final int SCALE = 6; // final size roughly 30x42 pixels

    private final Map<Character, boolean[][]> glyphs = new HashMap<>();

    public BigDigitPainter() {
        loadDigits();
    }

    public void drawDigitAcrossBuffers(List<TileBuffer> buffers, String digitText, byte color) {
        if (buffers == null || buffers.size() < 2 || digitText == null || digitText.isEmpty()) {
            return;
        }
        char ch = digitText.charAt(0);
        boolean[][] glyph = glyphs.get(ch);
        if (glyph == null) {
            return;
        }

        // center the scaled glyph across the provided text tiles (currently 2 tiles wide)
        int glyphWidth = GLYPH_W * SCALE;
        int glyphHeight = GLYPH_H * SCALE;
        int totalWidth = buffers.size() * TileBuffer.SIZE;
        int startX = (totalWidth - glyphWidth) / 2;
        int startY = (TileBuffer.SIZE - glyphHeight) / 2;

        for (int gy = 0; gy < glyph.length; gy++) {
            for (int gx = 0; gx < glyph[gy].length; gx++) {
                if (!glyph[gy][gx]) {
                    continue;
                }
                for (int sy = 0; sy < SCALE; sy++) {
                    for (int sx = 0; sx < SCALE; sx++) {
                        int px = startX + gx * SCALE + sx;
                        int py = startY + gy * SCALE + sy;
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

    private void loadDigits() {
        glyphs.put('1', rows(
            "..1..",
            ".11..",
            "..1..",
            "..1..",
            "..1..",
            "..1..",
            ".111."
        ));
        glyphs.put('2', rows(
            ".111.",
            "1...1",
            "....1",
            "..11.",
            ".1...",
            "1....",
            "11111"
        ));
        glyphs.put('3', rows(
            ".111.",
            "....1",
            "...1.",
            "..11.",
            "....1",
            "1...1",
            ".111."
        ));
        glyphs.put('4', rows(
            "...1.",
            "..11.",
            ".1.1.",
            "1..1.",
            "11111",
            "...1.",
            "...1."
        ));
        glyphs.put('5', rows(
            "11111",
            "1....",
            "1111.",
            "....1",
            "....1",
            "1...1",
            ".111."
        ));
        glyphs.put('6', rows(
            ".111.",
            "1....",
            "1....",
            "1111.",
            "1...1",
            "1...1",
            ".111."
        ));
        glyphs.put('7', rows(
            "11111",
            "....1",
            "...1.",
            "..1..",
            ".1...",
            ".1...",
            ".1..."
        ));
        glyphs.put('8', rows(
            ".111.",
            "1...1",
            "1...1",
            ".111.",
            "1...1",
            "1...1",
            ".111."
        ));
        glyphs.put('9', rows(
            ".111.",
            "1...1",
            "1...1",
            ".1111",
            "....1",
            "....1",
            ".111."
        ));
        glyphs.put('0', rows(
            "11111",
            "1...1",
            "1..11",
            "1.1.1",
            "11..1",
            "1...1",
            "11111"
        ));
    }

    private boolean[][] rows(String... rows) {
        boolean[][] out = new boolean[rows.length][rows[0].length()];
        for (int y = 0; y < rows.length; y++) {
            String line = rows[y];
            for (int x = 0; x < line.length(); x++) {
                out[y][x] = line.charAt(x) == '1';
            }
        }
        return out;
    }
}
