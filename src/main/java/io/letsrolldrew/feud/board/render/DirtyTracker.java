package io.letsrolldrew.feud.board.render;

import io.letsrolldrew.feud.board.layout.TilePos;

import java.util.LinkedHashSet;
import java.util.Set;

// Tracks which tiles need redrawing
public final class DirtyTracker {
    private final Set<TilePos> dirty = new LinkedHashSet<>();

    public void markDirty(TilePos pos) {
        dirty.add(pos);
    }

    public Set<TilePos> drain() {
        Set<TilePos> copy = new LinkedHashSet<>(dirty);
        dirty.clear();
        return copy;
    }

    public boolean isDirty() {
        return !dirty.isEmpty();
    }
}
