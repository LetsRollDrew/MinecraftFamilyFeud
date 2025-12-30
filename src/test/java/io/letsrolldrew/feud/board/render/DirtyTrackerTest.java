package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirtyTrackerTest {

    @Test
    void marksAndDrains() {
        DirtyTracker tracker = new DirtyTracker();
        tracker.markDirty(new TilePos(1, 1));
        tracker.markDirty(new TilePos(2, 2));
        tracker.markDirty(new TilePos(1, 1)); // dupe should be ignored
        assertTrue(tracker.isDirty());
        Set<TilePos> drained = tracker.drain();
        assertEquals(2, drained.size());
        assertTrue(drained.contains(new TilePos(1, 1)));
        assertTrue(drained.contains(new TilePos(2, 2)));
        assertTrue(tracker.drain().isEmpty());
    }
}
