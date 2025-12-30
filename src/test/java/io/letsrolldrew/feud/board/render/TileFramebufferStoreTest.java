package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TileFramebufferStoreTest {

    @Test
    void returnsBuffersForAllTiles() {
        TileFramebufferStore store = new TileFramebufferStore();
        assertNotNull(store.get(new TilePos(0, 0)));
        assertNotNull(store.get(new TilePos(9, 5)));
    }

    @Test
    void rejectsUnknownTile() {
        TileFramebufferStore store = new TileFramebufferStore();
        assertThrows(IllegalArgumentException.class, () -> store.get(new TilePos(10, 0)));
    }
}
