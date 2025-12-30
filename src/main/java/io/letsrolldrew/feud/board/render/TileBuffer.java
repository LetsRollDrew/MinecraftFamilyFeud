package io.letsrolldrew.feud.board.render;

// 128x128 palette index buffer for a single map tile
public final class TileBuffer {
    public static final int SIZE = 128;
    private static final int TOTAL = SIZE * SIZE;

    private final byte[] pixels = new byte[TOTAL];

    public byte get(int x, int y) {
        validate(x, y);
        return pixels[index(x, y)];
    }

    public void set(int x, int y, byte color) {
        validate(x, y);
        pixels[index(x, y)] = color;
    }

    public byte[] data() {
        return pixels;
    }

    private void validate(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
            throw new IllegalArgumentException("Pixel out of bounds: " + x + "," + y);
        }
    }

    private int index(int x, int y) {
        return y * SIZE + x;
    }
}
