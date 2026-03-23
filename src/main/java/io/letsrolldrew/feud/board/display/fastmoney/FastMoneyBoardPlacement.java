package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joml.Vector3d;

public final class FastMoneyBoardPlacement {

    private static final double MARGIN = 0.014;

    // Width fractions inside each
    private static final double POINTS_WIDTH = 0.18; // fraction of each half
    private static final double INNER_GAP = 0.012; // answer ↔ points gap (fraction of usableWidth)
    private static final double GROUP_GAP = 0.04; // P1 ↔ P2 divider gap (fraction of usableWidth)

    // Rows
    private static final int QUESTION_ROWS = 5;

    // How much height we reserve for the bottom "TOTAL" area (fraction of usableHeight).
    // Increase if you want a chunkier bottom bar.
    private static final double TOTAL_ZONE_FRACTION = 0.18;

    public LayoutAnchors compute(DynamicBoardLayout layout) {
        Objects.requireNonNull(layout, "layout");

        double totalW = layout.totalWidth();
        double totalH = layout.totalHeight();

        double marginX = totalW * MARGIN;
        double marginY = totalH * MARGIN;

        double usableWidth = totalW - (2 * marginX);
        double usableHeight = totalH - (2 * marginY);

        // Split usable height into question area + total zone
        double totalZoneHeight = usableHeight * TOTAL_ZONE_FRACTION;
        double questionAreaHeight = usableHeight - totalZoneHeight;

        double questionRowHeight = questionAreaHeight / QUESTION_ROWS;

        // Horizontal sizing
        double groupGap = usableWidth * GROUP_GAP;
        double innerGap = usableWidth * INNER_GAP;

        double groupWidth = (usableWidth - groupGap) / 2.0;

        double pointsWidth = groupWidth * POINTS_WIDTH;
        double textWidth = Math.max(0.01, groupWidth - pointsWidth - innerGap);

        // For TOTAL bar: span across answer+pts (but still inside one half)
        double totalBarWidth = textWidth + innerGap + pointsWidth;

        BoardFacing facing = layout.facing();

        // IMPORTANT:
        // We want "left-to-right on the board as the player sees it".
        // Player stands in front of the board looking opposite its forward normal,
        // so screenRight is -facing.right().
        Vector3d screenRight = new Vector3d(-facing.rightX(), 0, -facing.rightZ());
        Vector3d forward = new Vector3d(facing.forwardX(), 0, facing.forwardZ());
        Vector3d boardRight = new Vector3d(facing.rightX(), 0, facing.rightZ());
        Vector3d down = new Vector3d(0, -1, 0);

        // anchor = top-left in board space
        Vector3d topLeft = new Vector3d(layout.anchor()).add(forward.mul(layout.forwardOffset(), new Vector3d()));

        // Convert to board center (so we can offset +/- X and +/- Y cleanly)
        Vector3d center = new Vector3d(topLeft)
                .add(new Vector3d(boardRight).mul(totalW / 2.0))
                .add(new Vector3d(down).mul(totalH / 2.0));

        // Horizontal start offsets measured from center along screenRight
        double leftInnerEdgeFromCenter = -(totalW / 2.0) + marginX;
        double p1GroupStart = leftInnerEdgeFromCenter;
        double p2GroupStart = leftInnerEdgeFromCenter + groupWidth + groupGap;

        // Vertical top inner edge from center
        double topInnerEdgeFromCenter = (totalH / 2.0) - marginY;

        // Question rows occupy the top part
        List<QuestionRowAnchors> questions = new ArrayList<>(QUESTION_ROWS);
        for (int i = 0; i < QUESTION_ROWS; i++) {
            double yFromCenter = topInnerEdgeFromCenter - (questionRowHeight * i) - (questionRowHeight / 2.0);
            Vector3d rowCenter = new Vector3d(center).add(0, yFromCenter, 0);

            // Per group: [ANSWER longbar] then [PTS]
            double p1AnswerCenter = p1GroupStart + (textWidth / 2.0);
            double p1PtsCenter = p1GroupStart + textWidth + innerGap + (pointsWidth / 2.0);

            double p2AnswerCenter = p2GroupStart + (textWidth / 2.0);
            double p2PtsCenter = p2GroupStart + textWidth + innerGap + (pointsWidth / 2.0);

            Vector3d p1TextPos = new Vector3d(rowCenter).add(new Vector3d(screenRight).mul(p1AnswerCenter));
            Vector3d p1PointsPos = new Vector3d(rowCenter).add(new Vector3d(screenRight).mul(p1PtsCenter));

            Vector3d p2TextPos = new Vector3d(rowCenter).add(new Vector3d(screenRight).mul(p2AnswerCenter));
            Vector3d p2PointsPos = new Vector3d(rowCenter).add(new Vector3d(screenRight).mul(p2PtsCenter));

            questions.add(new QuestionRowAnchors(
                    i + 1, p1TextPos, p1PointsPos, p2TextPos, p2PointsPos, textWidth, pointsWidth, questionRowHeight));
        }

        // TOTAL row center: lives at the bottom zone center
        double totalZoneTopFromCenter = topInnerEdgeFromCenter - questionAreaHeight;
        double totalZoneCenterYFromCenter = totalZoneTopFromCenter - (totalZoneHeight / 2.0);
        Vector3d totalZoneCenter = new Vector3d(center).add(0, totalZoneCenterYFromCenter, 0);

        // Total bars are centered within each half (answer+pts combined width)
        double p1TotalCenter = p1GroupStart + (totalBarWidth / 2.0);
        double p2TotalCenter = p2GroupStart + (totalBarWidth / 2.0);

        Vector3d p1TotalPos = new Vector3d(totalZoneCenter).add(new Vector3d(screenRight).mul(p1TotalCenter));
        Vector3d p2TotalPos = new Vector3d(totalZoneCenter).add(new Vector3d(screenRight).mul(p2TotalCenter));

        TotalAnchors totals = new TotalAnchors(p1TotalPos, p2TotalPos, totalBarWidth, totalZoneHeight);

        return new LayoutAnchors(questions, totals);
    }

    public record LayoutAnchors(List<QuestionRowAnchors> questions, TotalAnchors totals) {}

    public record QuestionRowAnchors(
            int rowIndex,
            Vector3d p1TextCell,
            Vector3d p1PointsCell,
            Vector3d p2TextCell,
            Vector3d p2PointsCell,
            double textWidth,
            double pointsWidth,
            double rowHeight) {}

    public record TotalAnchors(Vector3d p1TotalCell, Vector3d p2TotalCell, double totalWidth, double totalZoneHeight) {}
}
