package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.BoardLayout10x6;
import io.letsrolldrew.feud.board.layout.TilePos;

import java.util.HashMap;
import java.util.Map;

//Holds one TileBuffer per TilePos (10x6)
public final class TileFramebufferStore {
    private final Map<TilePos, TileBuffer> buffers = new HashMap<>();

    public TileFramebufferStore() {
        for (int y = 0; y < BoardLayout10x6.HEIGHT; y++) {
            for (int x = 0; x < BoardLayout10x6.WIDTH; x++) {
                buffers.put(new TilePos(x, y), new TileBuffer());
            }
        }
    }

    public TileBuffer get(TilePos pos) {
        TileBuffer buf = buffers.get(pos);
        if (buf == null) {
            throw new IllegalArgumentException("Unknown tile: " + pos);
        }
        return buf;
    }

    public Map<TilePos, TileBuffer> all() {
        return buffers;
    }
}
