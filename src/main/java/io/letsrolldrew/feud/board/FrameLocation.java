package io.letsrolldrew.feud.board;

import org.bukkit.block.BlockFace;

import java.util.Objects;
import java.util.UUID;

// frame info (block coords + facing + id)
public record FrameLocation(UUID uuid, int x, int y, int z, BlockFace facing) {
    public FrameLocation {
        Objects.requireNonNull(uuid, "uuid");
        Objects.requireNonNull(facing, "facing");
    }
}
