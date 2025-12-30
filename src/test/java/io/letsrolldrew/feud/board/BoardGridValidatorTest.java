package io.letsrolldrew.feud.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.letsrolldrew.feud.board.layout.TilePos;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.bukkit.block.BlockFace;
import org.junit.jupiter.api.Test;

class BoardGridValidatorTest {

    @Test
    void mapsFramesLeftToRightTopToBottom() {
        BoardBinding binding = new BoardBinding(
                UUID.randomUUID(),
                BlockFace.SOUTH,
                HorizontalAxis.X,
                1,
                10, // origin x
                64, // origin y
                20 // origin z
                );

        var frames = new ArrayList<FrameLocation>();
        // create a full 10x6 wall starting at origin (x grows +1, y decreases)
        for (int y = 0; y < BoardGridValidator.HEIGHT; y++) {
            for (int x = 0; x < BoardGridValidator.WIDTH; x++) {
                frames.add(new FrameLocation(UUID.randomUUID(), 10 + x, 64 - y, 20, BlockFace.SOUTH));
            }
        }

        Map<TilePos, FrameLocation> mapped = BoardGridValidator.validate(binding, frames);
        assertEquals(BoardGridValidator.WIDTH * BoardGridValidator.HEIGHT, mapped.size());
        // spot check corners
        assertEquals(10, mapped.get(new TilePos(0, 0)).x());
        assertEquals(10 + 9, mapped.get(new TilePos(9, 0)).x());
        assertEquals(64 - 5, mapped.get(new TilePos(0, 5)).y());
    }

    @Test
    void rejectsMissingFrames() {
        BoardBinding binding = new BoardBinding(UUID.randomUUID(), BlockFace.NORTH, HorizontalAxis.Z, 1, 5, 70, 30);
        var frames = new ArrayList<FrameLocation>();
        // only 2 frames instead of 60
        frames.add(new FrameLocation(UUID.randomUUID(), 5, 70, 30, BlockFace.NORTH));
        frames.add(new FrameLocation(UUID.randomUUID(), 5, 69, 31, BlockFace.NORTH));

        assertThrows(IllegalArgumentException.class, () -> BoardGridValidator.validate(binding, frames));
    }
}
