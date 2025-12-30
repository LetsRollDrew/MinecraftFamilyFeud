package io.letsrolldrew.feud.board.render;

import java.util.HashMap;
import java.util.Map;

// 5 by 7 bitmap font painter (A-Z, 0-9, ?, space)
public final class TextPainter {
    private static final int GLYPH_WIDTH = 5;
    private static final int GLYPH_HEIGHT = 7;
    private static final int GLYPH_SPACING = 1;

    private final Map<Character, boolean[][]> glyphs = new HashMap<>();

    public TextPainter() {
        loadDigits();
        loadQuestion();
        loadSpace();
        loadAlphabet();
    }

    public void drawString(TileBuffer buffer, int startX, int startY, String text, byte color) {
        if (text == null) {
            return;
        }
        int x = startX;
        for (char ch : text.toUpperCase().toCharArray()) {
            boolean[][] g = glyphs.get(ch);
            if (g != null) {
                drawGlyph(buffer, x, startY, g, color);
            }
            x += GLYPH_WIDTH + GLYPH_SPACING;
        }
    }

    public boolean[][] glyphFor(char ch) {
        return glyphs.get(Character.toUpperCase(ch));
    }

    public int glyphWidth() {
        return GLYPH_WIDTH;
    }

    public int glyphHeight() {
        return GLYPH_HEIGHT;
    }

    public int glyphSpacing() {
        return GLYPH_SPACING;
    }

    private void drawGlyph(TileBuffer buffer, int startX, int startY, boolean[][] glyph, byte color) {
        for (int y = 0; y < glyph.length; y++) {
            for (int x = 0; x < glyph[y].length; x++) {
                if (glyph[y][x]) {
                    buffer.set(startX + x, startY + y, color);
                }
            }
        }
    }

    private void loadDigits() {
        glyphs.put('0', rows("11111", "1...1", "1..11", "1.1.1", "11..1", "1...1", "11111"));
        glyphs.put('1', rows("..1..", ".11..", "..1..", "..1..", "..1..", "..1..", ".111."));
        glyphs.put('2', rows(".111.", "1...1", "....1", "..11.", ".1...", "1....", "11111"));
        glyphs.put('3', rows("1111.", "....1", "...1.", "..11.", "....1", "1...1", ".111."));
        glyphs.put('4', rows("...1.", "..11.", ".1.1.", "1..1.", "11111", "...1.", "...1."));
        glyphs.put('5', rows("11111", "1....", "1111.", "....1", "....1", "1...1", ".111."));
        glyphs.put('6', rows(".111.", "1....", "1....", "1111.", "1...1", "1...1", ".111."));
        glyphs.put('7', rows("11111", "....1", "...1.", "..1..", ".1...", ".1...", ".1..."));
        glyphs.put('8', rows(".111.", "1...1", "1...1", ".111.", "1...1", "1...1", ".111."));
        glyphs.put('9', rows(".111.", "1...1", "1...1", ".1111", "....1", "....1", ".111."));
    }

    private void loadQuestion() {
        glyphs.put('?', rows(".111.", "1...1", "...1.", "..1..", "..1..", ".....", "..1.."));
    }

    private void loadSpace() {
        glyphs.put(' ', rows(".....", ".....", ".....", ".....", ".....", ".....", "....."));
    }

    private void loadAlphabet() {
        glyphs.put('A', rows(".111.", "1...1", "1...1", "11111", "1...1", "1...1", "1...1"));
        glyphs.put('B', rows("1111.", "1...1", "1...1", "1111.", "1...1", "1...1", "1111."));
        glyphs.put('C', rows(".111.", "1...1", "1....", "1....", "1....", "1...1", ".111."));
        glyphs.put('D', rows("111..", "1..1.", "1...1", "1...1", "1...1", "1..1.", "111.."));
        glyphs.put('E', rows("11111", "1....", "1....", "1111.", "1....", "1....", "11111"));
        glyphs.put('F', rows("11111", "1....", "1....", "1111.", "1....", "1....", "1...."));
        glyphs.put('G', rows(".111.", "1...1", "1....", "1.111", "1...1", "1...1", ".111."));
        glyphs.put('H', rows("1...1", "1...1", "1...1", "11111", "1...1", "1...1", "1...1"));
        glyphs.put('I', rows(".111.", "..1..", "..1..", "..1..", "..1..", "..1..", ".111."));
        glyphs.put('J', rows("..111", "...1.", "...1.", "...1.", "...1.", "1..1.", ".11.."));
        glyphs.put('K', rows("1...1", "1..1.", "1.1..", "11...", "1.1..", "1..1.", "1...1"));
        glyphs.put('L', rows("1....", "1....", "1....", "1....", "1....", "1....", "11111"));
        glyphs.put('M', rows("1...1", "11.11", "1.1.1", "1.1.1", "1...1", "1...1", "1...1"));
        glyphs.put('N', rows("1...1", "11..1", "1.1.1", "1..11", "1...1", "1...1", "1...1"));
        glyphs.put('O', rows(".111.", "1...1", "1...1", "1...1", "1...1", "1...1", ".111."));
        glyphs.put('P', rows("1111.", "1...1", "1...1", "1111.", "1....", "1....", "1...."));
        glyphs.put('Q', rows(".111.", "1...1", "1...1", "1...1", "1.1.1", "1..1.", ".11.1"));
        glyphs.put('R', rows("1111.", "1...1", "1...1", "1111.", "1.1..", "1..1.", "1...1"));
        glyphs.put('S', rows(".111.", "1...1", "1....", ".111.", "....1", "1...1", ".111."));
        glyphs.put('T', rows("11111", "..1..", "..1..", "..1..", "..1..", "..1..", "..1.."));
        glyphs.put('U', rows("1...1", "1...1", "1...1", "1...1", "1...1", "1...1", ".111."));
        glyphs.put('V', rows("1...1", "1...1", "1...1", "1...1", "1...1", ".1.1.", "..1.."));
        glyphs.put('W', rows("1...1", "1...1", "1...1", "1.1.1", "1.1.1", "1.1.1", ".1.1."));
        glyphs.put('X', rows("1...1", "1...1", ".1.1.", "..1..", ".1.1.", "1...1", "1...1"));
        glyphs.put('Y', rows("1...1", "1...1", ".1.1.", "..1..", "..1..", "..1..", "..1.."));
        glyphs.put('Z', rows("11111", "....1", "...1.", "..1..", ".1...", "1....", "11111"));
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
