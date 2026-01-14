package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.board.display.panels.TimerPanelPresenter;
import io.letsrolldrew.feud.board.display.panels.TimerPanelStore;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionListener;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.ui.DisplayHostRemoteBookBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import java.util.Objects;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DisplayBoardCommands {
    private final Messages messages;
    private final DisplayBoardService presenter;
    private final String adminPermission;
    private final DisplayBoardSelectionListener selectionListener;
    private final GameController controller;
    private final String hostPermission;
    private final HostRemoteService hostRemoteService;
    private final SurveyRepository surveyRepository;
    private final NamespacedKey hostKey;
    private final TeamService teamService;
    private final ScorePanelPresenter scorePanelPresenter;
    private final TimerPanelPresenter timerPanelPresenter;
    private final ScorePanelStore scorePanelStore;
    private final TimerPanelStore timerPanelStore;

    public DisplayBoardCommands(
            Messages messages,
            DisplayBoardService presenter,
            String adminPermission,
            DisplayBoardSelectionListener selectionListener,
            GameController controller,
            String hostPermission,
            HostRemoteService hostRemoteService,
            SurveyRepository surveyRepository,
            NamespacedKey hostKey,
            TeamService teamService,
            ScorePanelPresenter scorePanelPresenter,
            TimerPanelPresenter timerPanelPresenter,
            ScorePanelStore scorePanelStore,
            TimerPanelStore timerPanelStore) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.presenter = presenter;
        this.adminPermission = adminPermission;
        this.selectionListener = selectionListener;
        this.controller = controller;
        this.hostPermission = hostPermission;
        this.hostRemoteService = hostRemoteService;
        this.surveyRepository = surveyRepository;
        this.hostKey = hostKey;
        this.teamService = teamService;
        this.scorePanelPresenter = scorePanelPresenter;
        this.timerPanelPresenter = timerPanelPresenter;
        this.scorePanelStore = scorePanelStore;
        this.timerPanelStore = timerPanelStore;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("remote")) {
            handleRemote(sender, args);
            return true;
        }
        if (!sender.hasPermission(adminPermission)) {
            messages.error(sender, Msg.NEED_PERMISSION, Placeholder.of("permission", adminPermission));
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "create" -> handleCreate(sender, args);
            case "dynamic" -> handleCreateDynamic(sender, args);
            case "selection" -> handleSelectionSpawn(sender, args);
            case "list" -> handleList(sender);
            case "remove", "delete" -> handleRemove(sender, args);
            case "wand", "selector" -> handleWand(sender);
            case "destroy", "reveal", "hide", "demo" -> sendUsage(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleRemote(CommandSender sender, String[] args) {
        if (hostPermission == null || !sender.hasPermission(hostPermission)) {
            messages.error(sender, Msg.HOST_ONLY);
            return;
        }
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_REMOTE);
            return;
        }
        String boardId = args[1];
        if (boardId == null || boardId.isBlank()) {
            messages.error(sender, Msg.MISSING_BOARD_ID);
            return;
        }
        if (presenter == null || controller == null) {
            messages.error(sender, Msg.NOT_READY);
            return;
        }

        if (args.length < 3) {
            refreshRemote(player, boardId);
            messages.success(sender, Msg.DISPLAY_REMOTE_REFRESHED);
            return;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "reveal" -> handleRemoteReveal(sender, player, boardId, args);
            case "strike" -> {
                controller.strike();
                messages.info(
                        sender,
                        Msg.DISPLAY_REMOTE_STRIKE,
                        Placeholder.of("count", controller.strikeCount()),
                        Placeholder.of("max", controller.maxStrikes()));
                refreshRemote(player, boardId);
            }
            case "clearstrikes" -> {
                controller.clearStrikes();
                messages.success(sender, Msg.UI_STRIKES_CLEARED);
                refreshRemote(player, boardId);
            }
            case "control" -> handleRemoteControl(sender, player, boardId, args);
            case "award" -> handleRemoteAward(sender, player, boardId);
            case "reset" -> handleRemoteReset(sender, player, boardId);
            default -> messages.usage(sender, Msg.DISPLAY_REMOTE_HELP);
        }
    }

    private void handleRemoteReveal(CommandSender sender, Player player, String boardId, String[] args) {
        if (args.length < 4) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_REMOTE_REVEAL, Placeholder.of("boardId", boardId));
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            messages.error(sender, Msg.SLOT_MUST_BE_1_8);
            return;
        }
        if (slot < 1 || slot > 8) {
            messages.error(sender, Msg.SLOT_MUST_BE_1_8);
            return;
        }

        controller.revealSlot(slot);
        var survey = controller.getActiveSurvey();
        if (survey != null && slot - 1 < survey.answers().size()) {
            var answer = survey.answers().get(slot - 1);
            presenter.revealSlot(boardId, slot, answer.text(), answer.points());
        }
        messages.success(sender, Msg.DISPLAY_REMOTE_REVEALED, Placeholder.of("slot", slot));
        refreshRemote(player, boardId);
    }

    private void handleRemoteControl(CommandSender sender, Player player, String boardId, String[] args) {
        if (args.length < 4) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_REMOTE_CONTROL, Placeholder.of("boardId", boardId));
            return;
        }
        TeamControl team = TeamControl.fromString(args[3]);
        if (team == TeamControl.NONE) {
            messages.error(sender, Msg.TEAM_MUST_BE_RED_BLUE);
            return;
        }
        controller.setControllingTeam(team);
        messages.success(sender, Msg.DISPLAY_REMOTE_CONTROL, Placeholder.of("team", team.name()));
        refreshRemote(player, boardId);
    }

    private void handleRemoteAward(CommandSender sender, Player player, String boardId) {
        if (controller.controllingTeam() == TeamControl.NONE) {
            messages.error(sender, Msg.UI_CONTROL_REQUIRED_TO_AWARD);
            return;
        }
        int before = controller.roundPoints();
        controller.awardRoundPoints();
        messages.success(sender, Msg.DISPLAY_REMOTE_AWARDED, Placeholder.of("points", before));
        awardToTeam(before, controller.controllingTeam(), boardId);
        refreshRemote(player, boardId);
    }

    private void handleRemoteReset(CommandSender sender, Player player, String boardId) {
        controller.resetRoundState();
        for (int i = 1; i <= 8; i++) {
            presenter.hideSlot(boardId, i);
        }
        messages.success(sender, Msg.DISPLAY_REMOTE_RESET);
        refreshRemote(player, boardId);
    }

    private void refreshRemote(Player player, String boardId) {
        if (player == null || hostRemoteService == null || surveyRepository == null || hostKey == null) {
            return;
        }
        java.util.List<String> ids = new java.util.ArrayList<>(presenter.listBoards());
        java.util.Collections.sort(ids);

        String target = boardId;
        if ((target == null || target.isBlank()) && !ids.isEmpty()) {
            target = ids.get(0);
        }

        hostRemoteService.giveOrReplace(
                player, DisplayHostRemoteBookBuilder.create(target, ids, surveyRepository, hostKey, controller));
    }

    private void awardToTeam(int points, TeamControl control, String boardId) {
        if (points <= 0 || control == null || teamService == null) {
            return;
        }

        TeamId teamId = null;
        if (control == TeamControl.RED) {
            teamId = TeamId.RED;
        } else if (control == TeamControl.BLUE) {
            teamId = TeamId.BLUE;
        }

        if (teamId == null) {
            return;
        }
        teamService.addScore(teamId, points);
        if (scorePanelPresenter == null || boardId == null || boardId.isBlank()) {
            return;
        }
        scorePanelPresenter.updateForBoard(boardId);
        scorePanelPresenter.updateStoredPanels(boardId);
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_BOARD_CREATE);
            return;
        }
        String boardId = args[1];
        presenter.createBoard(boardId, player.getLocation(), player);
        messages.success(sender, Msg.BOARD_CREATED_AT_LOCATION, Placeholder.of("boardId", boardId));
    }

    private void handleCreateDynamic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_DYNAMIC);
            return;
        }
        var boardResult = presenter.resolveDynamicLayout(args[1], player, false, true);
        if (!boardResult.success()) {
            messages.error(sender, Msg.SELECTION_INVALID_REASON, Placeholder.of("reason", boardResult.error()));
            return;
        }
        String boardId = args[1];
        if (presenter.createDynamicBoard(boardId, boardResult.layout()) == null) {
            messages.error(sender, Msg.BOARD_CREATE_FAILED_OR_EXISTS);
            return;
        }
        messages.success(sender, Msg.DYNAMIC_BOARD_CREATED, Placeholder.of("boardId", boardId));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_REMOVE);
            return;
        }
        presenter.destroyBoard(args[1]);
        messages.success(sender, Msg.BOARD_REMOVED, Placeholder.of("boardId", args[1]));
    }

    private void handleSelectionSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 3) {
            messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_SELECTION);
            return;
        }
        String target = args[1].toLowerCase();
        String boardId = args[2];

        if (target.equals("board")) {
            var boardResult = presenter.resolveDynamicLayout(boardId, player, false, true);
            if (!boardResult.success()) {
                messages.error(sender, Msg.SELECTION_INVALID_REASON, Placeholder.of("reason", boardResult.error()));
                return;
            }
            if (presenter.createDynamicBoard(boardId, boardResult.layout()) == null) {
                messages.error(sender, Msg.BOARD_CREATE_FAILED_OR_EXISTS);
                return;
            }
            messages.success(sender, Msg.DYNAMIC_BOARD_CREATED_FROM_SELECTION, Placeholder.of("boardId", boardId));
            return;
        }

        if (target.equals("panels")) {
            if (scorePanelPresenter == null || scorePanelStore == null) {
                messages.error(sender, Msg.SCORE_PANELS_NOT_AVAILABLE);
                return;
            }
            boolean isRemove = args.length >= 4 && "remove".equalsIgnoreCase(args[3]);
            int teamIndex = isRemove ? 4 : 3;
            if (teamIndex >= args.length) {
                messages.usage(sender, Msg.USAGE_BOARD_DISPLAY_SELECTION_PANELS);
                return;
            }
            String teamArg = args[teamIndex].toLowerCase();
            TeamId team = null;
            if ("red".equals(teamArg)) {
                team = TeamId.RED;
            } else if ("blue".equals(teamArg)) {
                team = TeamId.BLUE;
            }

            if (isRemove) {
                removePanels(boardId, team);
                String targetLabel = team == null ? "both panels" : team == TeamId.RED ? "red panel" : "blue panel";
                messages.success(
                        sender,
                        Msg.PANELS_REMOVED_FOR_BOARD,
                        Placeholder.of("target", targetLabel),
                        Placeholder.of("boardId", boardId));
                return;
            }

            var layoutResult = presenter.resolveDynamicLayout(boardId, player, true, true);
            if (!layoutResult.success()) {
                messages.error(sender, Msg.SELECTION_INVALID_REASON, Placeholder.of("reason", layoutResult.error()));
                return;
            }
            spawnPanels(boardId, layoutResult.layout(), team);
            String targetLabel = panelLabel(team);
            messages.success(
                    sender,
                    Msg.PANELS_SPAWNED_FOR_BOARD,
                    Placeholder.of("target", targetLabel),
                    Placeholder.of("boardId", boardId));
            return;
        }

        if (target.equals("timer")) {
            if (timerPanelPresenter == null || timerPanelStore == null) {
                messages.error(sender, Msg.TIMER_PANEL_NOT_AVAILABLE);
                return;
            }
            boolean isRemove = args.length >= 4 && "remove".equalsIgnoreCase(args[3]);
            if (isRemove) {
                removeTimerPanel(boardId);
                messages.success(sender, Msg.TIMER_PANEL_REMOVED_FOR_BOARD, Placeholder.of("boardId", boardId));
                return;
            }
            var layoutResult = presenter.resolveDynamicLayout(boardId, player, true, true);
            if (!layoutResult.success()) {
                messages.error(sender, Msg.SELECTION_INVALID_REASON, Placeholder.of("reason", layoutResult.error()));
                return;
            }
            spawnTimerPanel(boardId, layoutResult.layout());
            messages.success(sender, Msg.TIMER_PANEL_SPAWNED_FOR_BOARD, Placeholder.of("boardId", boardId));
            return;
        }

        messages.error(sender, Msg.UNKNOWN_SELECTION_TARGET);
    }

    private void handleList(CommandSender sender) {
        var ids = presenter.listBoards();
        if (ids.isEmpty()) {
            messages.info(sender, Msg.BOARD_LIST_EMPTY);
            return;
        }
        messages.info(sender, Msg.BOARD_LIST_LINE, Placeholder.of("boards", String.join(", ", ids)));
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (selectionListener == null) {
            messages.error(sender, Msg.SELECTOR_WAND_NOT_AVAILABLE);
            return;
        }
        selectionListener.giveWand(player);
        messages.success(sender, Msg.DISPLAY_SELECTOR_GIVEN);
    }

    private void sendUsage(CommandSender sender) {
        messages.usage(sender, Msg.BOARD_DISPLAY_HELP);
    }

    private void spawnPanels(String boardId, DynamicBoardLayout layout, TeamId team) {
        if (team == null) {
            spawnPanel(boardId, layout, TeamId.RED);
            spawnPanel(boardId, layout, TeamId.BLUE);
            return;
        }
        spawnPanel(boardId, layout, team);
    }

    private void spawnPanel(String boardId, DynamicBoardLayout layout, TeamId team) {
        if (layout == null || team == null) {
            return;
        }
        String panelId = panelId(boardId, team);
        scorePanelPresenter.spawnStored(panelId, layout, team);
        scorePanelStore.savePanel(panelId, team, layout);
    }

    private void removePanels(String boardId, TeamId team) {
        if (team == null) {
            removePanel(boardId, TeamId.RED);
            removePanel(boardId, TeamId.BLUE);
            return;
        }
        removePanel(boardId, team);
    }

    private void removePanel(String boardId, TeamId team) {
        if (team == null) {
            return;
        }
        String panelId = panelId(boardId, team);
        scorePanelPresenter.removeStored(panelId);
        scorePanelStore.removePanel(panelId);
    }

    private void spawnTimerPanel(String boardId, DynamicBoardLayout layout) {
        if (layout == null) {
            return;
        }
        String panelId = timerPanelId(boardId);
        timerPanelPresenter.spawnStored(panelId, layout);
        timerPanelStore.savePanel(panelId, layout);
    }

    private void removeTimerPanel(String boardId) {
        String panelId = timerPanelId(boardId);
        timerPanelPresenter.removeStored(panelId);
        timerPanelStore.removePanel(panelId);
    }

    private static String panelId(String boardId, TeamId team) {
        String base = boardId == null ? "" : boardId.trim();
        String suffix = team == null ? "" : team.name().toLowerCase(java.util.Locale.ROOT);
        return base + ":" + suffix;
    }

    private static String timerPanelId(String boardId) {
        String base = boardId == null ? "" : boardId.trim();
        return base + ":timer";
    }

    private static String panelLabel(TeamId team) {
        if (team == null) {
            return "both panels";
        }
        if (team == TeamId.RED) {
            return "red panel";
        }
        return "blue panel";
    }
}
