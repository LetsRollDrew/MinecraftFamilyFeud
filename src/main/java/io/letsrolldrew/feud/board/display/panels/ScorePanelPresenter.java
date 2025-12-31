package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public final class ScorePanelPresenter {
    private static final float CMD_SCORE_PANEL = 9005.0f;
    private static final double PANEL_WIDTH_BLOCKS = 3.0;
    private static final double PANEL_HEIGHT_BLOCKS = 2.0;
    private static final double GAP_FROM_BOARD_BLOCKS = 1.0;
    private static final double FORWARD_NUDGE = 0.05;
    private static final double TEXT_FORWARD_NUDGE = 0.04;
    private static final int NAME_LINE_WIDTH = 160;
    private static final int SCORE_LINE_WIDTH = 64;

    private final DisplayRegistry displayRegistry;
    private final TeamService teamService;

    public ScorePanelPresenter(DisplayRegistry displayRegistry, TeamService teamService) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
    }

    public void spawnForBoard(String boardId, DynamicBoardLayout layout) {
        spawnForBoard(boardId, layout, null);
    }

    public void spawnForBoard(String boardId, DynamicBoardLayout layout, TeamId team) {
        if (boardId == null || boardId.isBlank() || layout == null) {
            return;
        }
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        if (team == null) {
            removeForBoard(boardId);
        } else {
            removeTeamPanels(boardId, team);
        }

        double panelWidth = PANEL_WIDTH_BLOCKS;
        double panelHeight = PANEL_HEIGHT_BLOCKS;
        double margin = GAP_FROM_BOARD_BLOCKS;

        var panels = ScorePanelPlacement.compute(layout, panelWidth, panelHeight, margin, FORWARD_NUDGE);

        if (team == null || team == TeamId.BLUE) {
            spawnBackground(
                    world,
                    panels.leftCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "blue-panel", "bg"));
            spawnText(
                    world,
                    panels.leftCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "blue-panel", "name"),
                    true);
            spawnText(
                    world,
                    panels.leftCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "blue-panel", "score"),
                    false);
        }

        if (team == null || team == TeamId.RED) {
            spawnBackground(
                    world,
                    panels.rightCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "red-panel", "bg"));
            spawnText(
                    world,
                    panels.rightCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "red-panel", "name"),
                    true);
            spawnText(
                    world,
                    panels.rightCenter(),
                    layout.facing().yaw(),
                    layout,
                    panelWidth,
                    panelHeight,
                    new DisplayKey("team", boardId, "red-panel", "score"),
                    false);
        }

        updateForBoard(boardId);
    }

    public void updateForBoard(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        setText(
                new DisplayKey("team", boardId, "blue-panel", "name"),
                Component.text(teamService.getName(TeamId.BLUE)));
        setText(
                new DisplayKey("team", boardId, "blue-panel", "score"),
                Component.text(Integer.toString(teamService.getScore(TeamId.BLUE))));

        setText(new DisplayKey("team", boardId, "red-panel", "name"), Component.text(teamService.getName(TeamId.RED)));
        setText(
                new DisplayKey("team", boardId, "red-panel", "score"),
                Component.text(Integer.toString(teamService.getScore(TeamId.RED))));
    }

    public void removeForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
        displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "bg"));
        displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "bg"));
        displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "name"));
        displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "score"));
        displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "name"));
        displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "score"));
    }

    private void removeTeamPanels(String boardId, TeamId team) {
        if (team == TeamId.BLUE) {
            displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "bg"));
            displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "name"));
            displayRegistry.remove(new DisplayKey("team", boardId, "blue-panel", "score"));
            return;
        }
        if (team == TeamId.RED) {
            displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "bg"));
            displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "name"));
            displayRegistry.remove(new DisplayKey("team", boardId, "red-panel", "score"));
        }
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

    private void spawnText(
            World world,
            org.joml.Vector3d center,
            float yaw,
            DynamicBoardLayout layout,
            double panelWidth,
            double panelHeight,
            DisplayKey key,
            boolean isName) {
        double verticalOffset = isName ? panelHeight * 0.25 : -panelHeight * 0.25;
        double scale = isName ? layout.cellHeight() * 0.9 : layout.cellHeight() * 1.3;
        int lineWidth = isName ? NAME_LINE_WIDTH : SCORE_LINE_WIDTH;

        Location loc = new Location(world, center.x, center.y + verticalOffset, center.z, yaw, 0f);

        Location spawnLoc = loc.clone()
                .add(
                        layout.facing().forwardX() * TEXT_FORWARD_NUDGE,
                        0,
                        layout.facing().forwardZ() * TEXT_FORWARD_NUDGE);

        TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setShadowed(true);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(org.bukkit.Color.fromARGB(0));
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setViewRange(48f);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setLineWidth(lineWidth);
            entity.text(Component.empty());
            entity.setTextOpacity((byte) 0);
            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) scale, (float) scale, (float) scale),
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

    private void setText(DisplayKey key, Component text) {
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.text(text == null ? Component.empty() : text);
            display.setTextOpacity((byte) 0xFF);
        });
    }
}
