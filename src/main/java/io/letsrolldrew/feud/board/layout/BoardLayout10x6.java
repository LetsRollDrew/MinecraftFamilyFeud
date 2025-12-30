package io.letsrolldrew.feud.board.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Pure mapping helpers for the 10x6 board (8x4 interior with 8 slots)
public final class BoardLayout10x6 {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 6;

    private static final int INTERIOR_X_START = 1;
    private static final int INTERIOR_X_END = 8;
    private static final int INTERIOR_Y_START = 1;
    private static final int INTERIOR_Y_END = 4;

    private BoardLayout10x6() {
    }

    //All border tiles cords (top, bottom, left, right)
    public static Set<TilePos> borderTiles() {
        Set<TilePos> tiles = new HashSet<>();
        // top and bottom
        for (int x = 0; x < WIDTH; x++) {
            tiles.add(new TilePos(x, 0));
            tiles.add(new TilePos(x, HEIGHT - 1));
        }
        // sides
        for (int y = INTERIOR_Y_START; y <= INTERIOR_Y_END; y++) {
            tiles.add(new TilePos(0, y));
            tiles.add(new TilePos(WIDTH - 1, y));
        }
        return Collections.unmodifiableSet(tiles);
    }

    //All interior tiles cords (8x4)
    public static Set<TilePos> interiorTiles() {
        Set<TilePos> tiles = new HashSet<>();
        for (int y = INTERIOR_Y_START; y <= INTERIOR_Y_END; y++) {
            for (int x = INTERIOR_X_START; x <= INTERIOR_X_END; x++) {
                tiles.add(new TilePos(x, y));
            }
        }
        return Collections.unmodifiableSet(tiles);
    }

    //Slot numbering:
    //1-4 = left column top to bottom (rows y=1..4)
    //5-8 = right column top to bottom (rows y=1..4)
    public static List<TilePos> tilesForSlot(int slot) {
        validateSlot(slot);
        int row = (slot - 1) % 4; // 0-based row index in interior
        int y = INTERIOR_Y_START + row;
        List<TilePos> tiles = new ArrayList<>(4);
        if (slot <= 4) {
            // left side: text x=1..3, points x=4
            tiles.add(new TilePos(1, y));
            tiles.add(new TilePos(2, y));
            tiles.add(new TilePos(3, y));
            tiles.add(new TilePos(4, y));
        } else {
            // right side: text x=5..7, points x=8
            tiles.add(new TilePos(5, y));
            tiles.add(new TilePos(6, y));
            tiles.add(new TilePos(7, y));
            tiles.add(new TilePos(8, y));
        }
        return Collections.unmodifiableList(tiles);
    }

    public static Set<TilePos> dirtyTilesForSlot(int slot) {
        return new HashSet<>(tilesForSlot(slot));
    }

    private static void validateSlot(int slot) {
        if (slot < 1 || slot > 8) {
            throw new IllegalArgumentException("slot must be 1-8");
        }
    }
}
