package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import java.util.UUID;

final class BoardFacingSpaceTest {

    /**********************************************************************************
    * verifies BoardFacing.fromYaw() snapping yaw angles to correct directions
    *
    * covers:
    * - exact cardinals (0/90/180/270) and wrap-around (360)
    * - near-cardinal values Ex: (89, 181) to validate snap behavior
    * - negative yaws (-90, -180, -270) to validate direction correction
    * - 'Cardinals' meaning South/West/North/East
    **********************************************************************************/
    @Test
    void snapsYawToCardinals() {
        assertEquals(BoardFacing.SOUTH, BoardFacing.fromYaw(0f));
        assertEquals(BoardFacing.WEST, BoardFacing.fromYaw(90f));
        assertEquals(BoardFacing.NORTH, BoardFacing.fromYaw(180f));
        assertEquals(BoardFacing.EAST, BoardFacing.fromYaw(270f));
        assertEquals(BoardFacing.SOUTH, BoardFacing.fromYaw(360f));

        assertEquals(BoardFacing.WEST, BoardFacing.fromYaw(89f));
        assertEquals(BoardFacing.SOUTH, BoardFacing.fromYaw(1f));
        assertEquals(BoardFacing.NORTH, BoardFacing.fromYaw(181f));
        assertEquals(BoardFacing.EAST, BoardFacing.fromYaw(-90f));
        assertEquals(BoardFacing.NORTH, BoardFacing.fromYaw(-180f));
        assertEquals(BoardFacing.WEST, BoardFacing.fromYaw(-270f));
    }

    /************************************************************************************
    * BoardSpace.at(right, up, forward) maps board local offsets into world
    * coordinates when we are facing SOUTH
    *
    * setup:
    * - origin: (0, 64, 0)
    * - offsets: (right=1, up=2, forward=3)
    *
    * expectations:
    * - up goes to +Y (64 + 2 = 66)
    * - forward/right rotate into the expected X/Z for SOUTH
    * - returned location yaw matches the facing yaw
    *
    * note:
    * - "close enough" check: Location coords = doubles (using 1e-6), yaw = float (using 1e-6f)
    * - Rest of the tests follow this same pattern for other facings
    ************************************************************************************/
    @Test
    void mapsOffsetsSouth() {
        World world = mock(World.class);
        BoardSpace space = new BoardSpace(new Location(world, 0, 64, 0), BoardFacing.SOUTH);

        Location loc = space.at(1, 2, 3);
        assertEquals(3, loc.getX(), 1e-6);
        assertEquals(66, loc.getY(), 1e-6);
        assertEquals(-1, loc.getZ(), 1e-6);
        assertEquals(BoardFacing.SOUTH.yaw(), loc.getYaw(), 1e-6f);
    }

    @Test
    void mapsCellCentersDynamicLayout() {
        World world = mock(World.class);
        Location anchor = new Location(world, 0, 10, 0);
        DynamicBoardLayout layout = new DynamicBoardLayout(
            UUID.randomUUID(),
            BoardFacing.SOUTH,
            new org.joml.Vector3d(anchor.getX(), anchor.getY(), anchor.getZ()),
            4.0,
            4.0,
            2.0,
            1.0,
            0.1,
            0.1,
            0,
            0,
            0.05,
            new org.joml.Vector3d(),
            new org.joml.Vector3d()
        );

        // col 0, row 0 center: right=1.0, up=-0.5, forward=0.05
        Location c00 = BoardSpace.atCellCenter(anchor, BoardFacing.SOUTH, 0, 0, layout);
        assertEquals(1.0, c00.getX(), 1e-6);
        assertEquals(9.5, c00.getY(), 1e-6);
        assertEquals(-0.05, c00.getZ(), 1e-6);

        // col 1, row 3 center: right=3.0, up=-3.5, forward=0.05
        Location c13 = BoardSpace.atCellCenter(anchor, BoardFacing.SOUTH, 1, 3, layout);
        assertEquals(3.0, c13.getX(), 1e-6);
        assertEquals(5.5, c13.getY(), 1e-6);
        assertEquals(-0.05, c13.getZ(), 1e-6);
    }

    @Test
    void mapsOffsetsNorth() {
        World world = mock(World.class);
        BoardSpace space = new BoardSpace(new Location(world, 0, 64, 0), BoardFacing.NORTH);

        Location loc = space.at(1, 2, 3);
        assertEquals(-3, loc.getX(), 1e-6);
        assertEquals(66, loc.getY(), 1e-6);
        assertEquals(1, loc.getZ(), 1e-6);
        assertEquals(BoardFacing.NORTH.yaw(), loc.getYaw(), 1e-6f);
    }

    @Test
    void mapsOffsetsEast() {
        World world = mock(World.class);
        BoardSpace space = new BoardSpace(new Location(world, 0, 64, 0), BoardFacing.EAST);

        Location loc = space.at(1, 2, 3);
        assertEquals(1, loc.getX(), 1e-6);
        assertEquals(66, loc.getY(), 1e-6);
        assertEquals(3, loc.getZ(), 1e-6);
        assertEquals(BoardFacing.EAST.yaw(), loc.getYaw(), 1e-6f);
    }

    @Test
    void mapsOffsetsWest() {
        World world = mock(World.class);
        BoardSpace space = new BoardSpace(new Location(world, 0, 64, 0), BoardFacing.WEST);

        Location loc = space.at(1, 2, 3);
        assertEquals(-1, loc.getX(), 1e-6);
        assertEquals(66, loc.getY(), 1e-6);
        assertEquals(-3, loc.getZ(), 1e-6);
        assertEquals(BoardFacing.WEST.yaw(), loc.getYaw(), 1e-6f);
    }
}
