package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.BoardFacing;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.joml.Vector3d;

public final class FastMoneyBoardPlacement {

    private static final double MARGIN = 0.014;
    private static final double POINTS_WIDTH = 0.18;
    private static final double INNER_GAP = 0.012;
    private static final double GROUP_GAP = 0.04;
    private static final int QUESTION_ROWS = 5;
    private static final double TOTAL_ZONE_FRACTION = 0.18;

    public LayoutAnchors compute(DynamicBoardLayout layout) {
        Objects.requireNonNull(layout, "layout");

        double totalWidth = layout.totalWidth();
        double totalHeight = layout.totalHeight();

        double marginX = totalWidth * MARGIN;
        double marginY = totalHeight * MARGIN;

        double usableWidth = totalWidth - (2 * marginX);
        double usableHeight = totalHeight - (2 * marginY);

        double totalZoneHeight = usableHeight * TOTAL_ZONE_FRACTION;
        double questionAreaHeight = usableHeight - totalZoneHeight;

        double questionRowHeight = questionAreaHeight / QUESTION_ROWS;

        double groupGap = usableWidth * GROUP_GAP;
        double innerGap = usableWidth * INNER_GAP;

        double groupWidth = (usableWidth - groupGap) / 2.0;

        double pointsWidth = groupWidth * POINTS_WIDTH;
        double textWidth = Math.max(0.01, groupWidth - pointsWidth - innerGap);

        double totalBarWidth = textWidth + innerGap + pointsWidth;

        BoardFacing facing = layout.facing();
        Vector3d screenRight = new Vector3d(-facing.rightX(), 0, -facing.rightZ());
        Vector3d forward = new Vector3d(facing.forwardX(), 0, facing.forwardZ());
        Vector3d boardRight = new Vector3d(facing.rightX(), 0, facing.rightZ());
        Vector3d down = new Vector3d(0, -1, 0);

        Vector3d topLeft = new Vector3d(layout.anchor());
        Vector3d forwardOffset = new Vector3d(forward).mul(layout.forwardOffset());
        topLeft.add(forwardOffset);

        Vector3d center = new Vector3d(topLeft);
        Vector3d rightOffset = new Vector3d(boardRight).mul(totalWidth / 2.0);
        Vector3d downOffset = new Vector3d(down).mul(totalHeight / 2.0);
        center.add(rightOffset);
        center.add(downOffset);

        double leftInnerEdgeFromCenter = -(totalWidth / 2.0) + marginX;
        double p1GroupStart = leftInnerEdgeFromCenter;
        double p2GroupStart = leftInnerEdgeFromCenter + groupWidth + groupGap;

        double topInnerEdgeFromCenter = (totalHeight / 2.0) - marginY;

        List<QuestionRowAnchors> questions = new ArrayList<>(QUESTION_ROWS);
        for (int i = 0; i < QUESTION_ROWS; i++) {
            double rowTopFromCenter = topInnerEdgeFromCenter - (questionRowHeight * i);
            double rowCenterYFromCenter = rowTopFromCenter - (questionRowHeight / 2.0);

            Vector3d rowCenter = new Vector3d(center);
            rowCenter.add(0, rowCenterYFromCenter, 0);

            double p1AnswerCenter = p1GroupStart + (textWidth / 2.0);
            double p1PtsCenter = p1GroupStart + textWidth + innerGap + (pointsWidth / 2.0);

            double p2AnswerCenter = p2GroupStart + (textWidth / 2.0);
            double p2PtsCenter = p2GroupStart + textWidth + innerGap + (pointsWidth / 2.0);

            Vector3d p1TextPos = offsetFrom(rowCenter, screenRight, p1AnswerCenter);
            Vector3d p1PointsPos = offsetFrom(rowCenter, screenRight, p1PtsCenter);

            Vector3d p2TextPos = offsetFrom(rowCenter, screenRight, p2AnswerCenter);
            Vector3d p2PointsPos = offsetFrom(rowCenter, screenRight, p2PtsCenter);

            questions.add(new QuestionRowAnchors(
                    i + 1, p1TextPos, p1PointsPos, p2TextPos, p2PointsPos, textWidth, pointsWidth, questionRowHeight));
        }

        double totalZoneTopFromCenter = topInnerEdgeFromCenter - questionAreaHeight;
        double totalZoneCenterYFromCenter = totalZoneTopFromCenter - (totalZoneHeight / 2.0);
        Vector3d totalZoneCenter = new Vector3d(center);
        totalZoneCenter.add(0, totalZoneCenterYFromCenter, 0);

        double p1TotalCenter = p1GroupStart + (totalBarWidth / 2.0);
        double p2TotalCenter = p2GroupStart + (totalBarWidth / 2.0);

        Vector3d p1TotalPos = offsetFrom(totalZoneCenter, screenRight, p1TotalCenter);
        Vector3d p2TotalPos = offsetFrom(totalZoneCenter, screenRight, p2TotalCenter);

        TotalAnchors totals = new TotalAnchors(p1TotalPos, p2TotalPos, totalBarWidth, totalZoneHeight);

        return new LayoutAnchors(questions, totals);
    }

    private static Vector3d offsetFrom(Vector3d origin, Vector3d direction, double distance) {
        Vector3d offset = new Vector3d(direction).mul(distance);

        Vector3d result = new Vector3d(origin);
        result.add(offset);

        return result;
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
