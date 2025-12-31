package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.team.TeamService;
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
import org.joml.Vector3f;

public final class ScorePanelPresenter {
    private static final float CMD_SCORE_PANEL = 9005.0f;
    private static final double PANEL_WIDTH_TILES = 2.0;
    private static final double PANEL_HEIGHT_TILES = 2.0;
    private static final double MARGIN_TILES = 0.6;
    private static final double FORWARD_NUDGE = 0.05;

    private final DisplayRegistry displayRegistry;
    private final TeamService teamService;

    public ScorePanelPresenter(DisplayRegistry displayRegistry, TeamService teamService) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
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

        double panelWidth = layout.cellWidth() * PANEL_WIDTH_TILES;
        double panelHeight = layout.cellHeight() * PANEL_HEIGHT_TILES;
        double margin = layout.cellWidth() * MARGIN_TILES;

        var panels = ScorePanelPlacement.compute(layout, panelWidth, panelHeight, margin, FORWARD_NUDGE);

        spawnBackground(
                world,
                panels.leftCenter(),
                layout.facing().yaw(),
                layout,
                panelWidth,
                panelHeight,
                new DisplayKey("team", boardId, "blue-panel", "bg"));

        spawnBackground(
                world,
                panels.rightCenter(),
                layout.facing().yaw(),
                layout,
                panelWidth,
                panelHeight,
                new DisplayKey("team", boardId, "red-panel", "bg"));
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
        displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "bg"));
        displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "bg"));
    }

    private void spawnBackground(
            World world,
            org.joml.Vector3d center,
            float yaw,
            DynamicBoardLayout layout,
            double panelWidth,
            double panelHeight,
            DisplayKey key) {

        Location loc = new Location(world, center.x, center.y, center.z, yaw, 0f);
        ItemStack stack = stackWithCmd(CMD_SCORE_PANEL);

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
        DisplayTags.tag(display, "team", key.group());
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
