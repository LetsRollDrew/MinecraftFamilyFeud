package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.Objects;
import org.joml.Vector3d;

public final class TimerPanelPlacement {
    private TimerPanelPlacement() {}

    public static Vector3d computeCenter(
            DynamicBoardLayout layout, double gapAbove, double panelHeight, double forwardNudge) {
        if (layout == null) {
            throw new IllegalArgumentException("layout");
        }
        if (gapAbove < 0) {
            throw new IllegalArgumentException("gapAbove");
        }
        if (panelHeight <= 0) {
            throw new IllegalArgumentException("panelHeight");
        }

        double right = layout.totalWidth() / 2.0;
        double up = gapAbove + (panelHeight / 2.0);
        double forward = layout.forwardOffset() + forwardNudge;

        return worldAt(layout, right, up, forward);
    }

    private static Vector3d worldAt(DynamicBoardLayout layout, double right, double up, double forward) {
        BoardFacing facing = Objects.requireNonNull(layout.facing(), "facing");
        Vector3d anchor = Objects.requireNonNull(layout.anchor(), "anchor");

        double x = anchor.x + (right * facing.rightX()) + (forward * facing.forwardX());
        double y = anchor.y + up;
        double z = anchor.z + (right * facing.rightZ()) + (forward * facing.forwardZ());
        return new Vector3d(x, y, z);
    }
}
