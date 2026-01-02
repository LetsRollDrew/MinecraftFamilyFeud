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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class ScorePanelPresenter {
    private static final float CMD_SCORE_PANEL = 9005.0f;

    // keep these “nudge” semantics consistent with the board factory
    private static final double PANEL_FORWARD_NUDGE = 0.05;
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

        // scale to selection footprint
        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();

        if (panelWidth <= 0.0 || panelHeight <= 0.0) {
            return;
        }

        float yaw = layout.facing().yaw();
        ItemStack panelStack = stackWithCmd(CMD_SCORE_PANEL);

        if (team == null || team == TeamId.BLUE) {
            Vector3d center = ScorePanelPlacement.computeCenter(
                    layout, panelWidth, panelHeight, PANEL_FORWARD_NUDGE, TeamId.BLUE);
            Location centerLoc = toLocation(world, center, yaw);

            spawnBackground(
                    new DisplayKey("team", boardId, "blue-panel", "bg"),
                    world,
                    centerLoc,
                    yaw,
                    panelWidth,
                    panelHeight,
                    panelStack);

            // text nudges inside the panel
            // currently using team name and score to test
            // might just move to pure score fully scaled to panel
            double nameVerticalNudge = panelHeight * 0.10;
            double scoreVerticalNudge = -panelHeight * 0.10;

            // scale text from panel height
            double nameScale = clamp(panelHeight * 0.30, 0.8, 10.0);
            double scoreScale = clamp(panelHeight * 0.38, 0.8, 12.0);

            spawnText(
                    new DisplayKey("team", boardId, "blue-panel", "name"),
                    world,
                    centerLoc,
                    yaw,
                    layout,
                    NAME_LINE_WIDTH,
                    nameScale,
                    nameVerticalNudge);

            spawnText(
                    new DisplayKey("team", boardId, "blue-panel", "score"),
                    world,
                    centerLoc,
                    yaw,
                    layout,
                    SCORE_LINE_WIDTH,
                    scoreScale,
                    scoreVerticalNudge);
        }

        if (team == null || team == TeamId.RED) {
            Vector3d center =
                    ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, PANEL_FORWARD_NUDGE, TeamId.RED);
            Location centerLoc = toLocation(world, center, yaw);

            spawnBackground(
                    new DisplayKey("team", boardId, "red-panel", "bg"),
                    world,
                    centerLoc,
                    yaw,
                    panelWidth,
                    panelHeight,
                    panelStack);

            double nameVerticalNudge = panelHeight * 0.10;
            double scoreVerticalNudge = -panelHeight * 0.10;

            double nameScale = clamp(panelHeight * 0.30, 0.8, 10.0);
            double scoreScale = clamp(panelHeight * 0.38, 0.8, 12.0);

            spawnText(
                    new DisplayKey("team", boardId, "red-panel", "name"),
                    world,
                    centerLoc,
                    yaw,
                    layout,
                    NAME_LINE_WIDTH,
                    nameScale,
                    nameVerticalNudge);

            spawnText(
                    new DisplayKey("team", boardId, "red-panel", "score"),
                    world,
                    centerLoc,
                    yaw,
                    layout,
                    SCORE_LINE_WIDTH,
                    scoreScale,
                    scoreVerticalNudge);
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

    private static Location toLocation(World world, Vector3d center, float yaw) {
        return new Location(world, center.x, center.y, center.z, yaw, 0f);
    }

    private void spawnBackground(
            DisplayKey key,
            World world,
            Location centerLoc,
            float yaw,
            double panelWidth,
            double panelHeight,
            ItemStack stack) {

        ItemDisplay display = world.spawn(centerLoc, ItemDisplay.class, entity -> {
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
            DisplayKey key,
            World world,
            Location centerLoc,
            float yaw,
            DynamicBoardLayout layout,
            int lineWidth,
            double textScale,
            double verticalNudge) {

        Location spawnLoc = centerLoc.clone();
        spawnLoc.add(
                layout.facing().forwardX() * TEXT_FORWARD_NUDGE,
                0,
                layout.facing().forwardZ() * TEXT_FORWARD_NUDGE);

        TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setShadowed(true);
            entity.setSeeThrough(false);
            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }
            try {
                entity.setBrightness(new Display.Brightness(15, 15));
            } catch (Throwable ignored) {
            }

            entity.setViewRange(48f);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setLineWidth(lineWidth);
            entity.text(Component.empty());
            entity.setTextOpacity((byte) 0xFF);

            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, (float) verticalNudge, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) textScale, (float) textScale, (float) textScale),
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
        org.bukkit.inventory.meta.ItemMeta meta = stack.getItemMeta();
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

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
