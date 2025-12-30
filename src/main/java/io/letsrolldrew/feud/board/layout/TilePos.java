package io.letsrolldrew.feud.board.layout;

// Zero-based tile coordinates on the 10x6 board grid

public record TilePos(int x, int y) {
    public TilePos {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("TilePos coordinates must be non-negative");
        }
    }
}
