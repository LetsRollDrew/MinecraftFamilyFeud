package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;

public final class BoardSpace {
    private final Location anchor;
    private final BoardFacing facing;

    public BoardSpace(Location anchor, BoardFacing facing) {
        this.anchor = anchor;
        this.facing = facing;
    }

    /***********************************************************************
     * Apply an offset in board space relative to yaw (user camera angle)
     * - x is "to the right of the board"
     * - z is "forward out of the board"
     * This keeps board aligned to world axis, idea of 0/90/180/270 degrees only
     * to prevent awkward diagonal orientations.
     * TODO: Potentially enable 45/135/225/315 angles later
     ************************************************************************/
    public Location at(double right, double up, double forward) {
        double x = anchor.getX() + (right * facing.rightX()) + (forward * facing.forwardX());
        double y = anchor.getY() + up;
        double z = anchor.getZ() + (right * facing.rightZ()) + (forward * facing.forwardZ());
        return new Location(anchor.getWorld(), x, y, z, facing.yaw(), 0f);
    }

    // these map col/row in a DynamicBoardLayout to world space top left anchor + center
    public static Location atCellCenter(
            Location anchor, BoardFacing facing, int col, int row, DynamicBoardLayout layout) {
        double right = (col * layout.cellWidth()) + (layout.cellWidth() / 2.0);
        double up = -((row * layout.cellHeight()) + (layout.cellHeight() / 2.0));
        double forward = layout.forwardOffset();
        return new BoardSpace(anchor, facing).at(right, up, forward);
    }

    public static Location atCellTopLeft(
            Location anchor, BoardFacing facing, int col, int row, DynamicBoardLayout layout) {
        double right = col * layout.cellWidth();
        double up = -(row * layout.cellHeight());
        return new BoardSpace(anchor, facing).at(right, up, 0);
    }

    public BoardFacing facing() {
        return facing;
    }
}
