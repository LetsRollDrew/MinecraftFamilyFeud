package io.letsrolldrew.feud.board;

import io.letsrolldrew.feud.board.layout.TilePos;
import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import java.util.Objects;
import java.util.UUID;

//Snapshot of where the 10x6 board lives in the world (top-left origin + facing)
public final class BoardBinding {
    //world the board is in
    private final UUID worldId;
    //which way the item frames face
    private final BlockFace facing;
    //which axis the width runs along (X or Z)
    private final HorizontalAxis widthAxis;
    //+1 if width moves positive along widthAxis, -1 if negative
    private final int widthSign;
    //block coords of tile (0,0) (top-left)
    private final int originX;
    private final int originY;
    private final int originZ;

    public BoardBinding(
        UUID worldId,
        BlockFace facing,
        HorizontalAxis widthAxis,
        int widthSign,
        int originX,
        int originY,
        int originZ
    ) {
        this.worldId = Objects.requireNonNull(worldId, "worldId");
        this.facing = Objects.requireNonNull(facing, "facing");
        this.widthAxis = Objects.requireNonNull(widthAxis, "widthAxis");
        if (widthSign == 0) {
            throw new IllegalArgumentException("widthSign must be +1 or -1");
        }
        this.widthSign = widthSign > 0 ? 1 : -1;
        this.originX = originX;
        this.originY = originY;
        this.originZ = originZ;
    }

    public UUID worldId() {
        return worldId;
    }

    public BlockFace facing() {
        return facing;
    }

    public HorizontalAxis widthAxis() {
        return widthAxis;
    }

    public int widthSign() {
        return widthSign;
    }

    //Top-left tile world block coordinate (x,z,y) for tile (0,0)
    public BlockVector origin() {
        return new BlockVector(originX, originY, originZ);
    }

    //Convert a tile position (0..9, 0..5) to its world block location
    public BlockVector tileToWorld(TilePos pos) {
        int x = originX;
        int y = originY - pos.y();
        int z = originZ;

        int step = widthSign * pos.x();
        if (widthAxis == HorizontalAxis.X) {
            x += step;
        } else {
            z += step;
        }
        return new BlockVector(x, y, z);
    }
}
