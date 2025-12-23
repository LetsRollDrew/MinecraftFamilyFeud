package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapIdStoreTest {

    @Test
    void saveAndLoad() throws Exception {
        File temp = Files.createTempFile("mapids", ".yml").toFile();
        temp.deleteOnExit();
        MapIdStore store = new MapIdStore(temp);
        store.save(Map.of(
            new TilePos(1, 1), 100,
            new TilePos(2, 2), 200
        ));
        Map<TilePos, Integer> loaded = store.load();
        assertEquals(2, loaded.size());
        assertEquals(100, loaded.get(new TilePos(1, 1)));
        assertEquals(200, loaded.get(new TilePos(2, 2)));
    }
}
