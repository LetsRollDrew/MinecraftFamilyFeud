package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.team.TeamId;
import java.util.Objects;
import org.joml.Vector3d;

public final class ScorePanelPlacement {
    private ScorePanelPlacement() {}

    public static Vector3d computeCenter(
            DynamicBoardLayout layout, double panelWidth, double panelHeight, double forwardNudgeBlocks, TeamId team) {

        Objects.requireNonNull(layout, "layout");
        Objects.requireNonNull(team, "team");

        double totalW = layout.totalWidth();
        double totalH = layout.totalHeight();

        double centerRight = totalW / 2.0;
        double centerUp = -(totalH / 2.0);

        double halfW = totalW / 2.0;
        double halfPanel = panelWidth / 2.0;
        double maxShift = Math.max(0.0, halfW - halfPanel);

        double shift = maxShift * 0.55;

        double right = (team == TeamId.BLUE) ? (centerRight - shift) : (centerRight + shift);
        double forward = layout.forwardOffset() + forwardNudgeBlocks;

        return worldAt(layout, right, centerUp, forward);
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
