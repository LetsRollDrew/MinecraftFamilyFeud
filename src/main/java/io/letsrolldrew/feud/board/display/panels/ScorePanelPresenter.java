package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3d;

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

        PanelDisplayHelper.setText(
                displayRegistry,
                new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "name"),
                Component.text(teamService.getName(TeamId.BLUE)));
        PanelDisplayHelper.setText(
                displayRegistry,
                new DisplayKey(TEAM_NAMESPACE, boardId, BLUE_PREFIX, "score"),
                Component.text(Integer.toString(teamService.getScore(TeamId.BLUE))));

        PanelDisplayHelper.setText(
                displayRegistry,
                new DisplayKey(TEAM_NAMESPACE, boardId, RED_PREFIX, "name"),
                Component.text(teamService.getName(TeamId.RED)));
        PanelDisplayHelper.setText(
                displayRegistry,
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

    private void spawnTeamPanel(
            String namespace, String group, String idPrefix, DynamicBoardLayout layout, TeamId team) {
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        // replace existing
        removePanelKeys(namespace, group, idPrefix);

        PanelDimensions dims = panelDimensions(layout);
        if (dims.width() <= 0.0 || dims.height() <= 0.0) {
            return;
        }

        float yaw = layout.facing().yaw();
        ItemStack panelStack = PanelDisplayHelper.stackWithCmd(CMD_SCORE_PANEL);

        Vector3d center =
                ScorePanelPlacement.computeCenter(layout, dims.width(), dims.height(), PANEL_FORWARD_NUDGE, team);

        Location centerLoc = PanelDisplayHelper.toLocation(world, center, yaw);

        DisplayKey bgKey = new DisplayKey(namespace, group, idPrefix, "bg");
        DisplayKey nameKey = new DisplayKey(namespace, group, idPrefix, "name");
        DisplayKey scoreKey = new DisplayKey(namespace, group, idPrefix, "score");

        PanelDisplayHelper.spawnBackground(
                displayRegistry, bgKey, world, centerLoc, yaw, dims.width(), dims.height(), panelStack, namespace);

        double nameVerticalNudge = dims.height() * 0.10;
        double scoreVerticalNudge = -dims.height() * 0.10;

        double nameScale = PanelDisplayHelper.clamp(dims.height() * 0.30, 0.8, 10.0);
        double scoreScale = PanelDisplayHelper.clamp(dims.height() * 0.38, 0.8, 12.0);

        PanelDisplayHelper.spawnText(
                displayRegistry,
                nameKey,
                world,
                centerLoc,
                yaw,
                layout,
                NAME_LINE_WIDTH,
                nameScale,
                nameVerticalNudge,
                TEXT_FORWARD_NUDGE,
                namespace,
                true);

        PanelDisplayHelper.spawnText(
                displayRegistry,
                scoreKey,
                world,
                centerLoc,
                yaw,
                layout,
                SCORE_LINE_WIDTH,
                scoreScale,
                scoreVerticalNudge,
                TEXT_FORWARD_NUDGE,
                namespace,
                true);

        // use live team data for stored and board panels
        PanelDisplayHelper.setText(displayRegistry, nameKey, Component.text(teamService.getName(team)));
        PanelDisplayHelper.setText(
                displayRegistry, scoreKey, Component.text(Integer.toString(teamService.getScore(team))));
    }

    private void removePanelKeys(String namespace, String group, String idPrefix) {
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "bg"));
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "name"));
        displayRegistry.remove(new DisplayKey(namespace, group, idPrefix, "score"));
    }

    private PanelDimensions panelDimensions(DynamicBoardLayout layout) {
        double width = layout.totalWidth();
        double height = layout.totalHeight();
        return new PanelDimensions(width, height);
    }

    private static String teamPrefix(TeamId team) {
        return team == TeamId.RED ? RED_PREFIX : BLUE_PREFIX;
    }

    private record PanelDimensions(double width, double height) {}
}
