package io.letsrolldrew.feud.board;

import io.letsrolldrew.feud.board.BoardBinding;
import io.letsrolldrew.feud.board.layout.TilePos;
import io.letsrolldrew.feud.board.HorizontalAxis;
import org.bukkit.block.BlockFace;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// validates 10x6 frames and produces TilePos --> FrameLocation mapping
public final class BoardGridValidator {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 6;

    private BoardGridValidator() {
    }

    public static Map<TilePos, FrameLocation> validate(BoardBinding binding, Collection<FrameLocation> frames) {
        if (binding == null) {
            throw new IllegalArgumentException("binding required");
        }
        if (frames == null || frames.isEmpty()) {
            throw new IllegalArgumentException("frames required");
        }

        Map<TilePos, FrameLocation> mapped = new HashMap<>();
        int expectedSize = WIDTH * HEIGHT;
        for (FrameLocation frame : frames) {
            TilePos pos = toTile(binding, frame);
            if (pos == null) {
                continue;
            }
            if (mapped.containsKey(pos)) {
                throw new IllegalArgumentException("Duplicate frame at tile " + pos);
            }
            mapped.put(pos, frame);
        }
        if (mapped.size() != expectedSize) {
            throw new IllegalArgumentException("Expected " + expectedSize + " frames, found " + mapped.size());
        }
        return mapped;
    }

    private static TilePos toTile(BoardBinding binding, FrameLocation frame) {
        if (frame.facing() != binding.facing()) {
            return null;
        }

        // validate plane (the non-width axis stays constant)
        if (binding.widthAxis() == HorizontalAxis.X && frame.z() != binding.origin().getBlockZ()) {
            return null;
        }
        if (binding.widthAxis() == HorizontalAxis.Z && frame.x() != binding.origin().getBlockX()) {
            return null;
        }

        int xIndex;
        if (binding.widthAxis() == HorizontalAxis.X) {
            int delta = frame.x() - binding.origin().getBlockX();
            xIndex = binding.widthSign() > 0 ? delta : -delta;
        } else {
            int delta = frame.z() - binding.origin().getBlockZ();
            xIndex = binding.widthSign() > 0 ? delta : -delta;
        }
        if (xIndex < 0 || xIndex >= WIDTH) {
            return null;
        }

        int yIndex = binding.origin().getBlockY() - frame.y();
        if (yIndex < 0 || yIndex >= HEIGHT) {
            return null;
        }

        return new TilePos(xIndex, yIndex);
    }
}
