package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.util.Validation;
import java.util.Objects;
import java.util.function.Consumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class UiCommand {
    private final Messages messages;
    private final GameController controller;
    private final String hostPermission;
    private final Consumer<Player> bookRefresher;
    private final Consumer<Integer> revealCallback;
    private final TeamService teamService;
    private final ScorePanelPresenter scorePanelPresenter;
    private final ScorePanelStore scorePanelStore;
    private final DisplayBoardPresenter displayBoardPresenter;

    public UiCommand(
            Messages messages,
            GameController controller,
            String hostPermission,
            Consumer<Player> bookRefresher,
            Consumer<Integer> revealCallback,
            TeamService teamService,
            ScorePanelPresenter scorePanelPresenter,
            ScorePanelStore scorePanelStore,
            DisplayBoardPresenter displayBoardPresenter) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.controller = controller;
        this.hostPermission = Validation.requireNonBlank(hostPermission, "host-permission");
        this.bookRefresher = bookRefresher;
        this.revealCallback = revealCallback;
        this.teamService = teamService;
        this.scorePanelPresenter = scorePanelPresenter;
        this.scorePanelStore = scorePanelStore;
        this.displayBoardPresenter = displayBoardPresenter;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission(hostPermission)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "reveal" -> handleReveal(sender, args);
            case "strike" -> handleStrike(sender);
            case "clearstrikes" -> handleClearStrikes(sender);
            case "add" -> handleAddPoints(sender, args);
            case "control" -> handleControl(sender, args);
            case "award" -> handleAward(sender);
            case "reset" -> handleReset(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleReveal(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_UI_REVEAL);
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            messages.error(sender, Msg.SLOT_MUST_BE_1_8);
            return;
        }
        if (slot < 1 || slot > 8) {
            messages.error(sender, Msg.SLOT_MUST_BE_1_8);
            return;
        }
        controller.revealSlot(slot);
        messages.success(sender, Msg.UI_REVEALED_SLOT, Placeholder.of("slot", slot));
        if (revealCallback != null) {
            revealCallback.accept(slot);
        }
        refreshIfPlayer(sender);
    }

    private void handleStrike(CommandSender sender) {
        controller.strike();
        messages.success(
                sender,
                Msg.UI_STRIKE_RECORDED,
                Placeholder.of("count", controller.strikeCount()),
                Placeholder.of("max", controller.maxStrikes()));
        refreshIfPlayer(sender);
    }

    private void handleClearStrikes(CommandSender sender) {
        controller.clearStrikes();
        messages.success(sender, Msg.UI_STRIKES_CLEARED);
        refreshIfPlayer(sender);
    }

    private void handleAddPoints(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_UI_ADD);
            return;
        }
        int points;
        try {
            points = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            messages.error(sender, Msg.UI_POINTS_MUST_BE_POSITIVE);
            return;
        }
        if (points <= 0) {
            messages.error(sender, Msg.UI_POINTS_MUST_BE_POSITIVE);
            return;
        }
        controller.addPoints(points);
        messages.success(
                sender,
                Msg.UI_ADDED_POINTS,
                Placeholder.of("points", points),
                Placeholder.of("total", controller.roundPoints()));
        refreshIfPlayer(sender);
    }

    private void handleControl(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_UI_CONTROL);
            return;
        }
        TeamControl team = TeamControl.fromString(args[1]);
        if (team == TeamControl.NONE) {
            messages.error(sender, Msg.TEAM_MUST_BE_RED_BLUE);
            return;
        }
        controller.setControllingTeam(team);
        messages.success(sender, Msg.UI_CONTROL_SET, Placeholder.of("team", team.name()));
        refreshIfPlayer(sender);
    }

    private void handleAward(CommandSender sender) {
        TeamControl team = controller.controllingTeam();
        if (team == TeamControl.NONE) {
            messages.error(sender, Msg.UI_CONTROL_REQUIRED_TO_AWARD);
            return;
        }
        int before = controller.roundPoints();
        controller.awardRoundPoints();
        messages.success(
                sender, Msg.UI_AWARDED_POINTS, Placeholder.of("points", before), Placeholder.of("team", team.name()));
        awardToTeam(before, team);
        refreshIfPlayer(sender);
    }

    private void handleReset(CommandSender sender) {
        controller.resetRoundState();
        messages.success(sender, Msg.UI_ROUND_RESET);
        refreshIfPlayer(sender);
    }

    private void sendUsage(CommandSender sender) {
        messages.usage(sender, Msg.USAGE_UI_ROOT);
    }

    private void refreshIfPlayer(CommandSender sender) {
        if (bookRefresher != null && sender instanceof Player player) {
            bookRefresher.accept(player);
        }
    }

    private void awardToTeam(int points, TeamControl control) {
        if (points <= 0 || control == null || teamService == null) {
            return;
        }
        TeamId teamId = control == TeamControl.RED ? TeamId.RED : control == TeamControl.BLUE ? TeamId.BLUE : null;
        if (teamId == null) {
            return;
        }
        teamService.addScore(teamId, points);
        if (scorePanelPresenter == null || displayBoardPresenter == null) {
            return;
        }
        for (String boardId : displayBoardPresenter.listBoards()) {
            scorePanelPresenter.updateForBoard(boardId);
        }
        scorePanelPresenter.updateStoredPanels(scorePanelStore);
    }
}
