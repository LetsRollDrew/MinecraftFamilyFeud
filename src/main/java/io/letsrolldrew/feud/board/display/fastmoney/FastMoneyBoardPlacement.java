package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joml.Vector3d;

public final class FastMoneyBoardPlacement {
    private static final double MARGIN = 0.05;
    private static final double TEXT_WIDTH = 0.8;
    private static final int ROW_COUNT = 6; // five questions + total

    public List<RowAnchors> compute(DynamicBoardLayout layout) {
        Objects.requireNonNull(layout, "layout");

        double marginX = layout.totalWidth() * MARGIN;
        double marginY = layout.totalHeight() * MARGIN;

        double usableWidth = layout.totalWidth() - (2 * marginX);
        double textWidth = usableWidth * TEXT_WIDTH;
        double pointsWidth = usableWidth - textWidth;

        double usableHeight = layout.totalHeight() - (2 * marginY);
        double rowHeight = usableHeight / ROW_COUNT;

        BoardFacing facing = layout.facing();
        Vector3d right = new Vector3d(facing.rightX(), 0, facing.rightZ());
        Vector3d down = new Vector3d(0, -1, 0);
        Vector3d forward = new Vector3d(facing.forwardX(), 0, facing.forwardZ());

        Vector3d topLeft = new Vector3d(layout.anchor()).add(forward.mul(layout.forwardOffset(), new Vector3d()));

        List<RowAnchors> rows = new ArrayList<>(ROW_COUNT);
        for (int i = 0; i < ROW_COUNT; i++) {
            double yOffset = marginY + (rowHeight * i) + (rowHeight / 2.0);
            Vector3d base =
                    new Vector3d(topLeft).add(new Vector3d(right).mul(marginX)).add(new Vector3d(down).mul(yOffset));

            Vector3d textPos = new Vector3d(base).add(new Vector3d(right).mul(textWidth / 2.0));
            Vector3d pointsPos = new Vector3d(base).add(new Vector3d(right).mul(textWidth + (pointsWidth / 2.0)));

            rows.add(new RowAnchors(i + 1, textPos, pointsPos));
        }
        return rows;
    }

    public record RowAnchors(int rowIndex, Vector3d textCell, Vector3d pointsCell) {}
}
