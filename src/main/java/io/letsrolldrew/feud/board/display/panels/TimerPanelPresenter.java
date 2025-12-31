package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class TimerPanelPresenter {
    private static final float CMD_TIMER_PANEL = 9005.0f;
    private static final double PANEL_WIDTH_BLOCKS = 3.0;
    private static final double PANEL_HEIGHT_BLOCKS = 2.0;
    private static final double GAP_ABOVE_BOARD_BLOCKS = 1.0;
    private static final double FORWARD_NUDGE = 0.05;

    private final DisplayRegistry displayRegistry;

    public TimerPanelPresenter(DisplayRegistry displayRegistry) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
    }

    public void spawnForBoard(String boardId, DynamicBoardLayout layout) {
        if (boardId == null || boardId.isBlank() || layout == null) {
            return;
        }
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        removeForBoard(boardId);

        double panelWidth = PANEL_WIDTH_BLOCKS;
        double panelHeight = PANEL_HEIGHT_BLOCKS;
        double gapAbove = GAP_ABOVE_BOARD_BLOCKS;

        Vector3d center = TimerPanelPlacement.computeCenter(layout, gapAbove, panelHeight, FORWARD_NUDGE);
        spawnBackground(world, center, layout.facing().yaw(), panelWidth, panelHeight, boardId);
    }

    public void updateForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
    }

    public void removeForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
        displayRegistry.remove(new DisplayKey("timer", boardId, "top-timer", "bg"));
    }

    private void spawnBackground(
            World world, Vector3d center, float yaw, double panelWidth, double panelHeight, String boardId) {
        Location loc = new Location(world, center.x, center.y, center.z, yaw, 0f);
        ItemStack stack = stackWithCmd(CMD_TIMER_PANEL);

        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) panelWidth, (float) panelHeight, 0.01f),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayKey key = new DisplayKey("timer", boardId, "top-timer", "bg");
        DisplayTags.tag(display, "timer", key.group());
        displayRegistry.register(key, display);
    }

    private static ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }
}
