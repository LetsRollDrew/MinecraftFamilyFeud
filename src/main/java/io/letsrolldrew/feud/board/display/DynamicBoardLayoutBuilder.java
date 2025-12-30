package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import java.util.UUID;
import org.joml.Vector3d;

// this builds a DynamicBoardLayout from a validated DisplayBoardSelection

public final class DynamicBoardLayoutBuilder {

    private static final double MIN_WIDTH = 2.0;
    private static final double MIN_HEIGHT = 2.5;
    private static final double DEFAULT_PAD_FRACTION = 0.05;
    private static final double FORWARD_OFFSET = 0.06;

    private DynamicBoardLayoutBuilder() {}

    public static Result build(DisplayBoardSelection selection) {
        if (selection == null || selection.isInvalid()) {
            return new Result(null, "selection invalid");
        }

        BoardFacing facing = selection.facing();
        UUID worldId = selection.worldId();
        Vector3d a = selection.cornerA();
        Vector3d b = selection.cornerB();

        double width;
        double height = Math.abs(a.y - b.y) + 1.0;
        Vector3d minCorner = new Vector3d(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.min(a.z, b.z));
        Vector3d maxCorner = new Vector3d(Math.max(a.x, b.x), Math.max(a.y, b.y), Math.max(a.z, b.z));

        // width along the horizontal axis depending on facing direction
        if (facing == BoardFacing.NORTH || facing == BoardFacing.SOUTH) {
            width = Math.abs(a.x - b.x) + 1.0;
        } else {
            width = Math.abs(a.z - b.z) + 1.0;
        }

        if (width < MIN_WIDTH) {
            return new Result(null, "selection too narrow");
        }
        if (height < MIN_HEIGHT) {
            return new Result(null, "selection too short");
        }

        double cellWidth = width / 2.0;
        double cellHeight = height / 4.0;
        double padX = cellWidth * DEFAULT_PAD_FRACTION;
        double padY = cellHeight * DEFAULT_PAD_FRACTION;
        double gapX = 0;
        double gapY = 0;

        // anchor at the top-left corner on the wall
        double anchorY = maxCorner.y + 1.0;
        double anchorX;
        double anchorZ;
        // NOTE: +1.0 is to fix off by one issue, not sure if theres a more elegant way
        // to do this but this hurt my head enough already so here we are
        if (facing == BoardFacing.NORTH || facing == BoardFacing.SOUTH) {

            // width runs along X axis, choose left edge by right-vector sign
            // if right points negative, left edge is the far side (maxX + 1)
            anchorX = facing.rightX() > 0 ? minCorner.x : maxCorner.x + 1.0;

            // wall plane Z: south is +Z face (maxZ + 1), north is minZ
            anchorZ = facing == BoardFacing.SOUTH ? maxCorner.z + 1.0 : minCorner.z;

        } else {
            // width runs along Z axis, choose left edge by right-vector sign
            // if right points negative, left edge is the far side (maxZ + 1)
            anchorZ = facing.rightZ() > 0 ? minCorner.z : maxCorner.z + 1.0;

            // wall plane X: east is +X face (maxX + 1), west is minX
            anchorX = facing == BoardFacing.EAST ? maxCorner.x + 1.0 : minCorner.x;
        }

        Vector3d anchor = new Vector3d(anchorX, anchorY, anchorZ);

        DynamicBoardLayout layout = new DynamicBoardLayout(
                worldId,
                facing,
                anchor,
                width,
                height,
                cellWidth,
                cellHeight,
                padX,
                padY,
                gapX,
                gapY,
                FORWARD_OFFSET,
                minCorner,
                maxCorner);
        return new Result(layout, null);
    }

    public record Result(DynamicBoardLayout layout, String error) {
        public boolean success() {
            return layout != null && error == null;
        }
    }
}
