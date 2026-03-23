package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
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

    /** Which TOTAL (row 6) is active. Rows 1–5 always show both columns. */
    public enum ActiveSide {
        P1,
        P2,
        BOTH // optional reveal mode
    }

    private static final double TEXT_SCALE = 0.80;
    private static final double PX_PER_BLOCK_AT_SCALE_1 = 40.0;
    private static final int MIN_LINE_WIDTH_PX = 16;

    // Make bars fill most of each row vertically so rows feel less spaced.
    private static final double BAR_HEIGHT_FACTOR = 0.90;

    // Less inset = fatter bars.
    private static final double BAR_WIDTH_INSET_BLOCKS = 0.01;

    private static final double BAR_FORWARD_NUDGE_BLOCKS = 0.06;
    private static final float BAR_DEPTH = 0.01f;

    // ✅ TOTAL bar should be smaller and right-aligned within its half
    private static final double TOTAL_WIDTH_FACTOR = 0.50; // 50% of half width
    private static final double TOTAL_RIGHT_INSET_BLOCKS = 0.02; // padding from the pts-end edge

    private static final BlockData BAR_BLOCK = Bukkit.createBlockData(Material.BLACK_CONCRETE);

    private final DisplayRegistry displayRegistry;
    private final FastMoneyBoardPlacement placement;

    public FastMoneyBoardPresenter(DisplayRegistry displayRegistry, FastMoneyBoardPlacement placement) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.placement = Objects.requireNonNull(placement, "placement");
    }

    /** Default spawn = P1 turn. (Rows 1–5 both columns. TOTAL only on left.) */
    public void spawn(String boardId, DynamicBoardLayout layout) {
        spawn(boardId, layout, ActiveSide.P1);
    }

    /**
     * Rows 1–5: ALWAYS spawn both columns (P1+P2), like the TV board.
     * Row 6 (TOTAL): spawn ONLY for the active side (P1 or P2).
     */
    public void spawn(String boardId, DynamicBoardLayout layout, ActiveSide activeSide) {
        if (layout == null) return;

        String group = sanitizeBoardId(boardId);
        remove(group);

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) return;

        float yaw = layout.facing().yaw();

        // Board-plane "screenRight" (same concept you used in placement)
        Vector3d screenRight =
                new Vector3d(-layout.facing().rightX(), 0, -layout.facing().rightZ());

        FastMoneyBoardPlacement.LayoutAnchors anchors = placement.compute(layout);

        // ----------------------------
        // Rows 1–5 (ALWAYS both columns)
        // ----------------------------
        for (FastMoneyBoardPlacement.QuestionRowAnchors row : anchors.questions()) {
            double barHeight = Math.max(0.01, row.rowHeight() * BAR_HEIGHT_FACTOR);

            // P1 answer + pts
            spawnBar(
                    group,
                    "q" + row.rowIndex() + "-p1-answer",
                    world,
                    yaw,
                    layout,
                    row.p1TextCell(),
                    row.textWidth(),
                    barHeight);
            spawnBar(
                    group,
                    "q" + row.rowIndex() + "-p1-pts",
                    world,
                    yaw,
                    layout,
                    row.p1PointsCell(),
                    row.pointsWidth(),
                    barHeight);

            spawnCell(
                    group,
                    "q" + row.rowIndex() + "-p1-answer",
                    world,
                    yaw,
                    row.p1TextCell(),
                    row.textWidth(),
                    TextDisplay.TextAlignment.LEFT);
            spawnCell(
                    group,
                    "q" + row.rowIndex() + "-p1-pts",
                    world,
                    yaw,
                    row.p1PointsCell(),
                    row.pointsWidth(),
                    TextDisplay.TextAlignment.CENTER);

            // P2 answer + pts (always present, even during P1 turn)
            spawnBar(
                    group,
                    "q" + row.rowIndex() + "-p2-answer",
                    world,
                    yaw,
                    layout,
                    row.p2TextCell(),
                    row.textWidth(),
                    barHeight);
            spawnBar(
                    group,
                    "q" + row.rowIndex() + "-p2-pts",
                    world,
                    yaw,
                    layout,
                    row.p2PointsCell(),
                    row.pointsWidth(),
                    barHeight);

            spawnCell(
                    group,
                    "q" + row.rowIndex() + "-p2-answer",
                    world,
                    yaw,
                    row.p2TextCell(),
                    row.textWidth(),
                    TextDisplay.TextAlignment.LEFT);
            spawnCell(
                    group,
                    "q" + row.rowIndex() + "-p2-pts",
                    world,
                    yaw,
                    row.p2PointsCell(),
                    row.pointsWidth(),
                    TextDisplay.TextAlignment.CENTER);
        }

        // ----------------------------
        // Row 6 (TOTAL): ONLY ONE SIDE AT A TIME
        // TOTAL bar is 50% width and RIGHT-ALIGNED to the pts-end of that half.
        // ----------------------------
        FastMoneyBoardPlacement.TotalAnchors totals = anchors.totals();
        double totalBarHeight = Math.max(0.01, totals.totalZoneHeight() * 0.70);

        double fullHalfWidth = totals.totalWidth(); // width of (answer + gap + pts)
        double totalWidth = Math.max(0.01, fullHalfWidth * TOTAL_WIDTH_FACTOR);

        // We want the bar's RIGHT edge to align with the half's RIGHT edge (end of pts),
        // minus a small inset. We shift from the half CENTER along screenRight.
        double shift = (fullHalfWidth / 2.0) - TOTAL_RIGHT_INSET_BLOCKS - (totalWidth / 2.0);

        if (activeSide == ActiveSide.P1) {
            Vector3d pos = new Vector3d(totals.p1TotalCell()).add(new Vector3d(screenRight).mul(shift));
            spawnBar(group, "total-p1", world, yaw, layout, pos, totalWidth, totalBarHeight);
            spawnCell(group, "total-p1", world, yaw, pos, totalWidth, TextDisplay.TextAlignment.RIGHT);
            return;
        }

        if (activeSide == ActiveSide.P2) {
            Vector3d pos = new Vector3d(totals.p2TotalCell()).add(new Vector3d(screenRight).mul(shift));
            spawnBar(group, "total-p2", world, yaw, layout, pos, totalWidth, totalBarHeight);
            spawnCell(group, "total-p2", world, yaw, pos, totalWidth, TextDisplay.TextAlignment.RIGHT);
            return;
        }

        // BOTH mode: keep total on right column like TV
        Vector3d pos = new Vector3d(totals.p2TotalCell()).add(new Vector3d(screenRight).mul(shift));
        spawnBar(group, "total-p2", world, yaw, layout, pos, totalWidth, totalBarHeight);
        spawnCell(group, "total-p2", world, yaw, pos, totalWidth, TextDisplay.TextAlignment.RIGHT);
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
            org.joml.Vector3d pos,
            double widthBlocks,
            TextDisplay.TextAlignment alignment) {

        DisplayKey key = new DisplayKey(NAMESPACE, group, CELL_ID, name);

        Location loc = new Location(world, pos.x, pos.y, pos.z, yaw, 0f);
        int lineWidthPx = lineWidthPx(widthBlocks);

        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setAlignment(alignment == null ? TextDisplay.TextAlignment.CENTER : alignment);
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

        if (display == null) return;

        DisplayTags.tag(display, NAMESPACE, key.group());
        displayRegistry.register(key, display);
    }

    private void spawnBar(
            String group,
            String name,
            World world,
            float yaw,
            DynamicBoardLayout layout,
            org.joml.Vector3d pos,
            double widthBlocks,
            double heightBlocks) {

        if (layout == null) return;

        DisplayKey barKey = new DisplayKey(NAMESPACE, group, BAR_ID, name);

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
            entity.setBlock(BAR_BLOCK);

            try {
                entity.setBrightness(new Display.Brightness(15, 15));
            } catch (Throwable ignored) {
            }

            // Center the scaled cube around the entity origin
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

        if (display == null) return;

        DisplayTags.tag(display, NAMESPACE, barKey.group());
        displayRegistry.register(barKey, display);
    }

    private static int lineWidthPx(double widthBlocks) {
        double width = Math.max(0.01, widthBlocks);
        int px = (int) Math.round((width * PX_PER_BLOCK_AT_SCALE_1) / TEXT_SCALE);
        return Math.max(MIN_LINE_WIDTH_PX, px);
    }

    private static String sanitizeBoardId(String boardId) {
        if (boardId == null || boardId.isBlank()) return "board1";
        return boardId;
    }
}
