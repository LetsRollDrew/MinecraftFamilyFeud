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

    private DynamicBoardLayoutBuilder() {
    }

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
        Vector3d minCorner = new Vector3d(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y),
            Math.min(a.z, b.z)
        );
        Vector3d maxCorner = new Vector3d(
            Math.max(a.x, b.x),
            Math.max(a.y, b.y),
            Math.max(a.z, b.z)
        );

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

        // anchor at top-left in board space: highest Y and min along width axis
        double anchorY = maxCorner.y;
        double anchorX = facing == BoardFacing.NORTH || facing == BoardFacing.SOUTH ? minCorner.x : maxCorner.x;
        double anchorZ = facing == BoardFacing.NORTH || facing == BoardFacing.SOUTH ? maxCorner.z : minCorner.z;
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
            maxCorner
        );
        return new Result(layout, null);
    }

    public record Result(DynamicBoardLayout layout, String error) {
        public boolean success() {
            return layout != null && error == null;
        }
    }
}
