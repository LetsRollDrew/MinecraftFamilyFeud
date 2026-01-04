package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

public final class FastMoneyBoardPresenter {
    private static final String NAMESPACE = "fastmoney";
    private static final String CELL_ID = "cell";
    private static final double TEXT_SCALE = 0.8;
    private static final int LINE_WIDTH = 80;

    private final DisplayRegistry displayRegistry;
    private final FastMoneyBoardPlacement placement;

    public FastMoneyBoardPresenter(DisplayRegistry displayRegistry, FastMoneyBoardPlacement placement) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.placement = Objects.requireNonNull(placement, "placement");
    }

    public void spawn(String boardId, DynamicBoardLayout layout) {
        if (layout == null) {
            return;
        }
        String group = setBoardId(boardId);
        remove(group);

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        float yaw = layout.facing().yaw();
        List<FastMoneyBoardPlacement.RowAnchors> rows = placement.compute(layout);
        for (FastMoneyBoardPlacement.RowAnchors row : rows) {
            spawnCell(
                    new DisplayKey(NAMESPACE, group, CELL_ID, "row" + row.rowIndex() + "-text"),
                    world,
                    yaw,
                    row.textCell());
            spawnCell(
                    new DisplayKey(NAMESPACE, group, CELL_ID, "row" + row.rowIndex() + "-points"),
                    world,
                    yaw,
                    row.pointsCell());
        }
    }

    public void remove(String boardId) {
        String group = setBoardId(boardId);
        displayRegistry.removeByGroup(NAMESPACE, group);
    }

    private void spawnCell(DisplayKey key, World world, float yaw, org.joml.Vector3d pos) {
        Location loc = new Location(world, pos.x, pos.y, pos.z, yaw, 0f);
        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setLineWidth(LINE_WIDTH);
            entity.text(Component.text(" "));
            try {
                entity.setBackgroundColor(Color.fromRGB(0, 0, 0));
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
                        new org.joml.Vector3f((float) TEXT_SCALE, (float) TEXT_SCALE, (float) TEXT_SCALE),
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

    private String setBoardId(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return "board1";
        }
        return boardId;
    }
}
