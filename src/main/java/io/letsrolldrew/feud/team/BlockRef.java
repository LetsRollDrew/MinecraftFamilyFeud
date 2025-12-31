package io.letsrolldrew.feud.team;

import java.util.Objects;
import java.util.UUID;

public record BlockRef(UUID worldId, int x, int y, int z) {
    public BlockRef {
        Objects.requireNonNull(worldId, "worldId");
    }
}

