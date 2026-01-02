package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.Objects;
import org.joml.Vector3d;

public final class TimerPanelPlacement {
    private TimerPanelPlacement() {}

    public static Vector3d computeCenterOnSelection(
            DynamicBoardLayout layout, double panelWidth, double panelHeight, double forwardNudgeBlocks) {

        Objects.requireNonNull(layout, "layout");

        if (panelWidth <= 0.0) {
            throw new IllegalArgumentException("panelWidth");
        }
        if (panelHeight <= 0.0) {
            throw new IllegalArgumentException("panelHeight");
        }

        double totalW = layout.totalWidth();
        double totalH = layout.totalHeight();

        double rightFromAnchor = totalW / 2.0;

        double downFromTop = -(totalH / 2.0);

        double forward = layout.forwardOffset() + forwardNudgeBlocks;

        return worldAt(layout, rightFromAnchor, downFromTop, forward);
    }

    public static Vector3d computeCenter(
            DynamicBoardLayout layout, double gapAbove, double panelHeight, double forwardNudge) {

        Objects.requireNonNull(layout, "layout");

        if (panelHeight <= 0.0) {
            throw new IllegalArgumentException("panelHeight");
        }
        if (gapAbove < 0.0) {
            throw new IllegalArgumentException("gapAbove");
        }

        double rightFromAnchor = layout.totalWidth() / 2.0;
        double up = gapAbove + (panelHeight / 2.0);
        double forward = layout.forwardOffset() + forwardNudge;

        return worldAt(layout, rightFromAnchor, up, forward);
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
