package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.fastmoney.FastMoneyPhase;
import io.letsrolldrew.feud.fastmoney.FastMoneyQuestionState;
import io.letsrolldrew.feud.fastmoney.FastMoneyRoundState;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class FastMoneyBoardPresenter {
    private static final String NAMESPACE = "fastmoney";
    private static final String CELL_ID = "cell";
    private static final String BAR_ID = "bar";

    public enum ActiveSide {
        P1,
        P2,
        BOTH
    }

    private static final double TEXT_SCALE = 0.80;
    private static final double PX_PER_BLOCK_AT_SCALE_1 = 40.0;
    private static final int MIN_LINE_WIDTH_PX = 16;

    private static final double BAR_HEIGHT_FACTOR = 0.90;
    private static final double BAR_WIDTH_INSET_BLOCKS = 0.01;

    private static final double BAR_FORWARD_NUDGE_BLOCKS = 0.06;
    private static final float BAR_DEPTH = 0.01f;

    private static final double TOTAL_WIDTH_FACTOR = 0.50;
    private static final double TOTAL_RIGHT_INSET_BLOCKS = 0.02;

    private static final Material BAR_MATERIAL = Material.BLACK_CONCRETE;

    private final DisplayRegistry displayRegistry;
    private final FastMoneyBoardPlacement placement;

    public FastMoneyBoardPresenter(DisplayRegistry displayRegistry, FastMoneyBoardPlacement placement) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.placement = Objects.requireNonNull(placement, "placement");
    }

    public void spawn(String boardId, DynamicBoardLayout layout) {
        spawn(boardId, layout, ActiveSide.P1);
    }

    public void spawn(String boardId, DynamicBoardLayout layout, ActiveSide activeSide) {
        if (layout == null) {
            return;
        }

        ActiveSide sideToUse = ActiveSide.P1;
        if (activeSide != null) {
            sideToUse = activeSide;
        }

        String group = sanitizeBoardId(boardId);
        remove(group);

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        float yaw = layout.facing().yaw();

        FastMoneyBoardPlacement.LayoutAnchors anchors = placement.compute(layout);

        spawnQuestionRows(group, world, yaw, layout, anchors);
        spawnTotalRow(group, world, yaw, layout, anchors, sideToUse);
    }

    private void spawnQuestionRows(
            String group,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            FastMoneyBoardPlacement.LayoutAnchors anchors) {
        for (FastMoneyBoardPlacement.QuestionRowAnchors row : anchors.questions()) {
            String rowId = "q" + row.rowIndex();

            String p1AnswerId = rowId + "-p1-answer";
            String p1PointsId = rowId + "-p1-pts";
            String p2AnswerId = rowId + "-p2-answer";
            String p2PointsId = rowId + "-p2-pts";

            double barHeight = Math.max(0.01, row.rowHeight() * BAR_HEIGHT_FACTOR);

            spawnQuestionCell(
                    group,
                    world,
                    yaw,
                    layout,
                    p1AnswerId,
                    row.p1TextCell(),
                    row.textWidth(),
                    barHeight,
                    true);
            spawnQuestionCell(
                    group,
                    world,
                    yaw,
                    layout,
                    p1PointsId,
                    row.p1PointsCell(),
                    row.pointsWidth(),
                    barHeight,
                    false);

            spawnQuestionCell(
                    group,
                    world,
                    yaw,
                    layout,
                    p2AnswerId,
                    row.p2TextCell(),
                    row.textWidth(),
                    barHeight,
                    true);
            spawnQuestionCell(
                    group,
                    world,
                    yaw,
                    layout,
                    p2PointsId,
                    row.p2PointsCell(),
                    row.pointsWidth(),
                    barHeight,
                    false);
        }
    }

    private void spawnQuestionCell(
            String group,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            String name,
            Vector3d pos,
            double widthBlocks,
            double barHeight,
            boolean isAnswer) {
        TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
        if (isAnswer) {
            alignment = TextDisplay.TextAlignment.LEFT;
        }

        spawnBar(group, name, world, yaw, layout, pos, widthBlocks, barHeight);
        spawnCell(group, name, world, yaw, pos, widthBlocks, alignment);
    }

    private void spawnTotalRow(
            String group,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            FastMoneyBoardPlacement.LayoutAnchors anchors,
            ActiveSide activeSide) {
        Vector3d screenRight = new Vector3d(-layout.facing().rightX(), 0, -layout.facing().rightZ());

        FastMoneyBoardPlacement.TotalAnchors totals = anchors.totals();

        double totalBarHeight = Math.max(0.01, totals.totalZoneHeight() * 0.70);
        double fullHalfWidth = totals.totalWidth();
        double totalWidth = Math.max(0.01, fullHalfWidth * TOTAL_WIDTH_FACTOR);

        double shift = (fullHalfWidth / 2.0) - TOTAL_RIGHT_INSET_BLOCKS - (totalWidth / 2.0);

        switch (activeSide) {
            case P1 -> {
                spawnTotalSide(
                        group,
                        world,
                        yaw,
                        layout,
                        "total-p1",
                        totals.p1TotalCell(),
                        screenRight,
                        shift,
                        totalWidth,
                        totalBarHeight);
            }
            case P2 -> {
                spawnTotalSide(
                        group,
                        world,
                        yaw,
                        layout,
                        "total-p2",
                        totals.p2TotalCell(),
                        screenRight,
                        shift,
                        totalWidth,
                        totalBarHeight);
            }
            case BOTH -> {
                spawnTotalSide(
                        group,
                        world,
                        yaw,
                        layout,
                        "total-p1",
                        totals.p1TotalCell(),
                        screenRight,
                        shift,
                        totalWidth,
                        totalBarHeight);
                spawnTotalSide(
                        group,
                        world,
                        yaw,
                        layout,
                        "total-p2",
                        totals.p2TotalCell(),
                        screenRight,
                        shift,
                        totalWidth,
                        totalBarHeight);
            }
        }
    }

    private void spawnTotalSide(
            String group,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            String name,
            Vector3d basePos,
            Vector3d screenRight,
            double shift,
            double widthBlocks,
            double heightBlocks) {
        Vector3d offset = new Vector3d(screenRight).mul(shift);

        Vector3d pos = new Vector3d(basePos);
        pos.add(offset);

        spawnBar(group, name, world, yaw, layout, pos, widthBlocks, heightBlocks);
        spawnCell(group, name, world, yaw, pos, widthBlocks, TextDisplay.TextAlignment.RIGHT);
    }

    public void remove(String boardId) {
        String group = sanitizeBoardId(boardId);
        displayRegistry.removeByGroup(NAMESPACE, group);
    }

    private void spawnCell(
            String group,
            String name,
            World world,
            float yaw,
            Vector3d pos,
            double widthBlocks,
            TextDisplay.TextAlignment alignment) {

        DisplayKey key = new DisplayKey(NAMESPACE, group, CELL_ID, name);

        Location loc = new Location(world, pos.x, pos.y, pos.z, yaw, 0f);
        int lineWidthPx = lineWidthPx(widthBlocks);
        final TextDisplay.TextAlignment alignmentToUse =
                alignment == null ? TextDisplay.TextAlignment.CENTER : alignment;

        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setAlignment(alignmentToUse);
            entity.setLineWidth(lineWidthPx);
            entity.text(Component.empty());

            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }
            try {
                entity.setBrightness(new Display.Brightness(15, 15));
            } catch (Throwable ignored) {
            }

            entity.setShadowed(false);
            entity.setSeeThrough(false);

            try {
                entity.setTransformation(new org.bukkit.util.Transformation(
                        entity.getTransformation().getTranslation(),
                        entity.getTransformation().getLeftRotation(),
                        new Vector3f((float) TEXT_SCALE, (float) TEXT_SCALE, (float) TEXT_SCALE),
                        entity.getTransformation().getRightRotation()));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }

        DisplayTags.tag(display, NAMESPACE, key.group());
        displayRegistry.register(key, display);
    }

    public void render(String boardId, FastMoneyRoundState state, SurveyRepository surveyRepository) {
        if (state == null || surveyRepository == null) {
            return;
        }

        String group = sanitizeBoardId(boardId);
        for (int questionIndex = 1; questionIndex <= 5; questionIndex++) {
            FastMoneyQuestionState question = questionIndex <= state.questions().size()
                    ? state.questions().get(questionIndex - 1)
                    : null;
            String rowId = "q" + questionIndex;
            setCellText(group, rowId + "-p1-answer", question == null ? "" : question.player1RawAnswer());
            setCellText(group, rowId + "-p1-pts", awardedPointsText(question, true, surveyRepository));
            setCellText(group, rowId + "-p2-answer", question == null ? "" : question.player2RawAnswer());
            setCellText(group, rowId + "-p2-pts", awardedPointsText(question, false, surveyRepository));
        }

        setCellText(group, "total-p1", Integer.toString(totalPoints(state, true, surveyRepository)));
        setCellText(group, "total-p2", Integer.toString(totalPoints(state, false, surveyRepository)));
    }

    public ActiveSide activeSideForPhase(FastMoneyPhase phase) {
        if (phase == FastMoneyPhase.PLAYER1_TURN) {
            return ActiveSide.BOTH;
        }
        if (phase == FastMoneyPhase.PLAYER2_TURN || phase == FastMoneyPhase.COMPLETE) {
            return ActiveSide.BOTH;
        }
        return ActiveSide.BOTH;
    }

    private void spawnBar(
            String group,
            String name,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            Vector3d pos,
            double widthBlocks,
            double heightBlocks) {

        if (layout == null) {
            return;
        }

        DisplayKey barKey = new DisplayKey(NAMESPACE, group, BAR_ID, name);

        BlockData barBlock = createBarBlockData();
        if (barBlock == null) {
            return;
        }

        double width = Math.max(0.01, widthBlocks - (BAR_WIDTH_INSET_BLOCKS * 2.0));
        double height = Math.max(0.01, heightBlocks);

        Location loc = new Location(world, pos.x, pos.y, pos.z, yaw, 0f);
        loc.add(
                layout.facing().forwardX() * BAR_FORWARD_NUDGE_BLOCKS,
                0,
                layout.facing().forwardZ() * BAR_FORWARD_NUDGE_BLOCKS);

        BlockDisplay display = world.spawn(loc, BlockDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setBlock(barBlock);

            try {
                entity.setBrightness(new Display.Brightness(15, 15));
            } catch (Throwable ignored) {
            }

            Vector3f centeredTranslation =
                    new Vector3f((float) (-width / 2.0), (float) (-height / 2.0), (float) (-BAR_DEPTH / 2.0));

            try {
                entity.setTransformation(new org.bukkit.util.Transformation(
                        centeredTranslation,
                        entity.getTransformation().getLeftRotation(),
                        new Vector3f((float) width, (float) height, BAR_DEPTH),
                        entity.getTransformation().getRightRotation()));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }

        DisplayTags.tag(display, NAMESPACE, barKey.group());
        displayRegistry.register(barKey, display);
    }

    private static BlockData createBarBlockData() {
        try {
            return Bukkit.createBlockData(BAR_MATERIAL);
        } catch (Throwable ignored) {
        }

        try {
            return BAR_MATERIAL.createBlockData();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int lineWidthPx(double widthBlocks) {
        double width = Math.max(0.01, widthBlocks);
        int px = (int) Math.round((width * PX_PER_BLOCK_AT_SCALE_1) / TEXT_SCALE);
        return Math.max(MIN_LINE_WIDTH_PX, px);
    }

    private void setCellText(String group, String name, String text) {
        DisplayKey key = new DisplayKey(NAMESPACE, group, CELL_ID, name);
        displayRegistry.resolveText(key).ifPresent(display -> display.text(Component.text(text == null ? "" : text)));
    }

    private static String awardedPointsText(
            FastMoneyQuestionState question, boolean player1, SurveyRepository surveyRepository) {
        if (question == null) {
            return "";
        }

        int awardedSlot = player1 ? question.player1AwardedSlot() : question.player2AwardedSlot();
        if (awardedSlot <= 0) {
            return "";
        }

        return resolveAwardedAnswer(question, awardedSlot, surveyRepository)
                .map(AnswerOption::points)
                .map(String::valueOf)
                .orElse("");
    }

    private static int totalPoints(FastMoneyRoundState state, boolean player1, SurveyRepository surveyRepository) {
        int total = 0;
        for (FastMoneyQuestionState question : state.questions()) {
            int awardedSlot = player1 ? question.player1AwardedSlot() : question.player2AwardedSlot();
            total += resolveAwardedAnswer(question, awardedSlot, surveyRepository)
                    .map(AnswerOption::points)
                    .orElse(0);
        }
        return total;
    }

    private static java.util.Optional<AnswerOption> resolveAwardedAnswer(
            FastMoneyQuestionState question, int awardedSlot, SurveyRepository surveyRepository) {
        if (question == null || awardedSlot <= 0) {
            return java.util.Optional.empty();
        }

        return surveyRepository.findById(question.surveyId())
                .map(Survey::answers)
                .filter(answers -> awardedSlot <= answers.size())
                .map(answers -> answers.get(awardedSlot - 1));
    }

    private static String sanitizeBoardId(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return "board1";
        }

        return boardId;
    }
}
