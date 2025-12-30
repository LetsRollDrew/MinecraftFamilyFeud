package io.letsrolldrew.feud.board;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.block.BlockFace;

// frame info (block coords + facing + id)
public record FrameLocation(UUID uuid, int x, int y, int z, BlockFace facing) {
    public FrameLocation {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(facing, "facing");
    }
}
