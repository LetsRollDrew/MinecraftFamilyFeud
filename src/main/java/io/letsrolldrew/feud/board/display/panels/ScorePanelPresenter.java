package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.Map;
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

    // semantics similar to the board factory
    private static final double PANEL_FORWARD_NUDGE = 0.05;
    private static final double TEXT_FORWARD_NUDGE = 0.04;
    private static final int NAME_LINE_WIDTH = 160;
    private static final int SCORE_LINE_WIDTH = 64;

    private static final String PANEL_NAMESPACE = "panel";
    private static final String TEAM_NAMESPACE = "team";
    private static final String BLUE_PREFIX = "blue-panel";
    private static final String RED_PREFIX = "red-panel";

    private final DisplayRegistry displayRegistry;
    private final TeamService teamService;

    public ScorePanelPresenter(DisplayRegistry displayRegistry, TeamService teamService) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
    }

    public void rehydrateStoredPanels(ScorePanelStore store) {
        if (store == null) {
            return;
        }
        Map<String, StoredScorePanel> storedPanels = store.loadPanels();
        for (StoredScorePanel panel : storedPanels.values()) {
            if (panel == null || panel.layout() == null || panel.team() == null) {
                continue;
            }
            spawnStored(panel.id(), panel.layout(), panel.team());
        }
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

        if (team == null || team == TeamId.BLUE) {
            spawnTeamPanel(TEAM_NAMESPACE, boardId, BLUE_PREFIX, layout, TeamId.BLUE);
        }

        if (team == null || team == TeamId.RED) {
            spawnTeamPanel(TEAM_NAMESPACE, boardId, RED_PREFIX, layout, TeamId.RED);
        }

        updateForBoard(boardId);
    }

    public void spawnStored(String panelId, DynamicBoardLayout layout, TeamId team) {
        if (panelId == null || panelId.isBlank() || layout == null || team == null) {
            return;
        }
        spawnTeamPanel(PANEL_NAMESPACE, panelId, teamPrefix(team), layout, team);
    }

    public void updateForBoard(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }

        setText(
                new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "name"),
                Component.text(teamService.getName(TeamId.BLUE)));
        setText(
                new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "score"),
                Component.text(Integer.toString(teamService.getScore(TeamId.BLUE))));

        setText(
                new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "name"),
                Component.text(teamService.getName(TeamId.RED)));
        setText(
                new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "score"),
                Component.text(Integer.toString(teamService.getScore(TeamId.RED))));
    }

    public void removeForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "bg"));
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "bg"));
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "name"));
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "score"));
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "name"));
        displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "score"));
    }

    private void removeTeamPanels(String boardId, TeamId team) {
        if (team == TeamId.BLUE) {
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "bg"));
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "name"));
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "score"));
            return;
        }
        if (team == TeamId.RED) {
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "bg"));
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "name"));
            displayRegistry.remove(new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "score"));
        }
    }

    public void removeStored(String panelId) {
        if (panelId == null || panelId.isBlank()) {
            return;
        }
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.BLUE), "bg"));
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.BLUE), "name"));
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.BLUE), "score"));
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.RED), "bg"));
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.RED), "name"));
        displayRegistry.remove(new DisplayKey(PANEL_NAMESPACE, panelId, teamPrefix(TeamId.RED), "score"));
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

    private void spawnTeamPanel(
            String namespace, String group, String idPrefix, DynamicBoardLayout layout, TeamId team) {

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();
        if (panelWidth <= 0.0 || panelHeight <= 0.0) {
            return;
        }

        removePanelKeys(namespace, group, idPrefix);

        float yaw = layout.facing().yaw();
        ItemStack panelStack = stackWithCmd(CMD_SCORE_PANEL);

        Vector3d center = ScorePanelPlacement.computeCenter(layout, panelWidth, panelHeight, PANEL_FORWARD_NUDGE, team);
        Location centerLoc = toLocation(world, center, yaw);

        DisplayKey bgKey = new DisplayKey(namespace, group, idPrefix, "bg");
        DisplayKey nameKey = new DisplayKey(namespace, group, idPrefix, "name");
        DisplayKey scoreKey = new DisplayKey(namespace, group, idPrefix, "score");

        spawnBackground(bgKey, world, centerLoc, yaw, panelWidth, panelHeight, panelStack);

        double nameVerticalNudge = panelHeight * 0.10;
        double scoreVerticalNudge = -panelHeight * 0.10;

        double nameScale = clamp(panelHeight * 0.30, 0.8, 10.0);
        double scoreScale = clamp(panelHeight * 0.38, 0.8, 12.0);

        spawnText(nameKey, world, centerLoc, yaw, layout, NAME_LINE_WIDTH, nameScale, nameVerticalNudge);
        spawnText(scoreKey, world, centerLoc, yaw, layout, SCORE_LINE_WIDTH, scoreScale, scoreVerticalNudge);

        // use live team data for stored and board panels
        setText(nameKey, Component.text(teamService.getName(team)));
        setText(scoreKey, Component.text(Integer.toString(teamService.getScore(team))));
    }

    private void removePanelKeys(String namespace, String group, String idPrefix) {
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "bg"));
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "name"));
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "score"));
    }

    private static String teamPrefix(TeamId team) {
        return team == TeamId.RED ? RED_PREFIX : BLUE_PREFIX;
    }
}
