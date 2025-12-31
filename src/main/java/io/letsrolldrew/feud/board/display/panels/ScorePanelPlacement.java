package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.Objects;
import org.joml.Vector3d;

public final class ScorePanelPlacement {
    private ScorePanelPlacement() {}

    public static Panels compute(
            DynamicBoardLayout layout, double panelWidth, double panelHeight, double margin, double forwardNudge) {

        if (layout == null) {
            throw new IllegalArgumentException("layout");
        }
        if (panelWidth <= 0) {
            throw new IllegalArgumentException("panelWidth");
        }
        if (panelHeight <= 0) {
            throw new IllegalArgumentException("panelHeight");
        }
        if (margin < 0) {
            throw new IllegalArgumentException("margin");
        }

        double panelHalfWidth = panelWidth / 2.0;
        double verticalCenter = -(layout.totalHeight() / 2.0);
        double forward = layout.forwardOffset() + forwardNudge;

        double leftRight = -(margin + panelHalfWidth);
        double rightRight = layout.totalWidth() + margin + panelHalfWidth;

        Vector3d leftCenter = worldAt(layout, leftRight, verticalCenter, forward);
        Vector3d rightCenter = worldAt(layout, rightRight, verticalCenter, forward);

        return new Panels(leftCenter, rightCenter);
    }

    private static Vector3d worldAt(DynamicBoardLayout layout, double right, double up, double forward) {
        BoardFacing facing = Objects.requireNonNull(layout.facing(), "facing");
        Vector3d anchor = Objects.requireNonNull(layout.anchor(), "anchor");

        double x = anchor.x + (right * facing.rightX()) + (forward * facing.forwardX());
        double y = anchor.y + up;
        double z = anchor.z + (right * facing.rightZ()) + (forward * facing.forwardZ());
        return new Vector3d(x, y, z);
    }

    public record Panels(Vector3d leftCenter, Vector3d rightCenter) {
        public Panels {
            Objects.requireNonNull(leftCenter, "leftCenter");
            Objects.requireNonNull(rightCenter, "rightCenter");
        }
    }
}
