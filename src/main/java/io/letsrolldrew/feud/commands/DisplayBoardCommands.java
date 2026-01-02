package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.board.display.panels.TimerPanelPresenter;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionListener;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.ui.DisplayHostRemoteBookBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DisplayBoardCommands {
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

    public DisplayBoardCommands(
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
            ScorePanelStore scorePanelStore) {
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
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("remote")) {
            handleRemote(sender, args);
            return true;
        }
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permissions");
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
            sender.sendMessage("Host only");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board display remote <id> ...");
            return;
        }
        String boardId = args[1];
        if (boardId == null || boardId.isBlank()) {
            sender.sendMessage("Missing board id");
            return;
        }
        if (presenter == null || controller == null) {
            sender.sendMessage("Not ready");
            return;
        }

        if (args.length < 3) {
            refreshRemote(player, boardId);
            sender.sendMessage("Remote refreshed");
            return;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "reveal" -> handleRemoteReveal(sender, player, boardId, args);
            case "strike" -> {
                controller.strike();
                sender.sendMessage("Strike " + controller.strikeCount() + "/" + controller.maxStrikes());
                refreshRemote(player, boardId);
            }
            case "clearstrikes" -> {
                controller.clearStrikes();
                sender.sendMessage("Strikes cleared");
                refreshRemote(player, boardId);
            }
            case "control" -> handleRemoteControl(sender, player, boardId, args);
            case "award" -> handleRemoteAward(sender, player, boardId);
            case "reset" -> handleRemoteReset(sender, player, boardId);
            default -> sender.sendMessage("Remote: reveal/strike/clearstrikes/control/award/reset");
        }
    }

    private void handleRemoteReveal(CommandSender sender, Player player, String boardId, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /feud board display remote " + boardId + " reveal <1-8>");
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Slot must be 1-8");
            return;
        }
        if (slot < 1 || slot > 8) {
            sender.sendMessage("Slot must be 1-8");
            return;
        }

        controller.revealSlot(slot);
        var survey = controller.getActiveSurvey();
        if (survey != null && slot - 1 < survey.answers().size()) {
            var answer = survey.answers().get(slot - 1);
            presenter.revealSlot(boardId, slot, answer.text(), answer.points());
        }
        sender.sendMessage("Revealed " + slot);
        refreshRemote(player, boardId);
    }

    private void handleRemoteControl(CommandSender sender, Player player, String boardId, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /feud board display remote " + boardId + " control <red|blue>");
            return;
        }
        TeamControl team = TeamControl.fromString(args[3]);
        if (team == TeamControl.NONE) {
            sender.sendMessage("Team must be red or blue");
            return;
        }
        controller.setControllingTeam(team);
        sender.sendMessage("Control " + team.name());
        refreshRemote(player, boardId);
    }

    private void handleRemoteAward(CommandSender sender, Player player, String boardId) {
        if (controller.controllingTeam() == TeamControl.NONE) {
            sender.sendMessage("Set control first");
            return;
        }
        int before = controller.roundPoints();
        controller.awardRoundPoints();
        sender.sendMessage("Awarded " + before);
        awardToTeam(before, controller.controllingTeam(), boardId);
        refreshRemote(player, boardId);
    }

    private void handleRemoteReset(CommandSender sender, Player player, String boardId) {
        controller.resetRoundState();
        for (int i = 1; i <= 8; i++) {
            presenter.hideSlot(boardId, i);
        }
        sender.sendMessage("Reset");
        refreshRemote(player, boardId);
    }

    private void refreshRemote(Player player, String boardId) {
        if (player == null || hostRemoteService == null || surveyRepository == null || hostKey == null) {
            return;
        }
        java.util.List<String> ids = new java.util.ArrayList<>(presenter.listBoards());
        java.util.Collections.sort(ids);
        String target = (boardId == null || boardId.isBlank()) && !ids.isEmpty() ? ids.get(0) : boardId;
        hostRemoteService.giveOrReplace(
                player, DisplayHostRemoteBookBuilder.create(target, ids, surveyRepository, hostKey, controller));
    }

    private void awardToTeam(int points, TeamControl control, String boardId) {
        if (points <= 0 || control == null || teamService == null) {
            return;
        }
        TeamId teamId = control == TeamControl.RED ? TeamId.RED : control == TeamControl.BLUE ? TeamId.BLUE : null;
        if (teamId == null) {
            return;
        }
        teamService.addScore(teamId, points);
        if (scorePanelPresenter != null && boardId != null && !boardId.isBlank()) {
            scorePanelPresenter.updateForBoard(boardId);
        }
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create a board");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board create <boardId>");
            return;
        }
        String boardId = args[1];
        presenter.createBoard(boardId, player.getLocation(), player);
        sender.sendMessage("Board '" + boardId + "' created at your location.");
    }

    private void handleCreateDynamic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create a board");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board display dynamic <boardId>");
            return;
        }
        var boardResult = presenter.resolveDynamicLayout(args[1], player, false, true);
        if (!boardResult.success()) {
            sender.sendMessage("Selection invalid: " + boardResult.error());
            return;
        }
        String boardId = args[1];
        if (presenter.createDynamicBoard(boardId, boardResult.layout()) == null) {
            sender.sendMessage("Board id already exists or creation failed.");
            return;
        }
        sender.sendMessage("Dynamic board '" + boardId + "' created.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board display remove <boardId>");
            return;
        }
        presenter.destroyBoard(args[1]);
        sender.sendMessage("Board '" + args[1] + "' removed.");
    }

    private void handleSelectionSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can spawn from a selection");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud board display selection <board|panels|timer> <boardId> [team]");
            return;
        }
        String target = args[1].toLowerCase();
        String boardId = args[2];

        if (target.equals("board")) {
            var boardResult = presenter.resolveDynamicLayout(boardId, player, false, true);
            if (!boardResult.success()) {
                sender.sendMessage("Selection invalid: " + boardResult.error());
                return;
            }
            if (presenter.createDynamicBoard(boardId, boardResult.layout()) == null) {
                sender.sendMessage("Board id already exists or creation failed.");
                return;
            }
            sender.sendMessage("Dynamic board '" + boardId + "' created from selection.");
            return;
        }

        if (target.equals("panels")) {
            if (scorePanelPresenter == null || scorePanelStore == null) {
                sender.sendMessage("Score panels are not available.");
                return;
            }
            boolean isRemove = args.length >= 4 && "remove".equalsIgnoreCase(args[3]);
            int teamIndex = isRemove ? 4 : 3;
            if (teamIndex >= args.length) {
                sender.sendMessage("Usage: /feud board display selection panels <boardId> <red|blue|both> [remove]");
                sender.sendMessage("       /feud board display selection panels <boardId> remove <red|blue|both>");
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
                sender.sendMessage("Removed " + targetLabel + " for '" + boardId + "'.");
                return;
            }

            var layoutResult = presenter.resolveDynamicLayout(boardId, player, true, true);
            if (!layoutResult.success()) {
                sender.sendMessage("Selection invalid: " + layoutResult.error());
                return;
            }
            spawnPanels(boardId, layoutResult.layout(), team);
            String targetLabel = team == null ? "both panels" : team == TeamId.RED ? "red panel" : "blue panel";
            sender.sendMessage("Spawned " + targetLabel + " for '" + boardId + "' using selection.");
            return;
        }

        if (target.equals("timer")) {
            if (timerPanelPresenter == null) {
                sender.sendMessage("Timer panel is not available.");
                return;
            }
            var layoutResult = presenter.resolveDynamicLayout(boardId, player, true, true);
            if (!layoutResult.success()) {
                sender.sendMessage("Selection invalid: " + layoutResult.error());
                return;
            }
            timerPanelPresenter.spawnForBoard(boardId, layoutResult.layout());
            sender.sendMessage("Timer panel spawned for '" + boardId + "' using selection.");
            return;
        }

        sender.sendMessage("Unknown selection target. Use board/panels/timer.");
    }

    private void handleList(CommandSender sender) {
        var ids = presenter.listBoards();
        if (ids.isEmpty()) {
            sender.sendMessage("No boards active.");
            return;
        }
        sender.sendMessage("Boards: " + String.join(", ", ids));
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the selector wand");
            return;
        }
        if (selectionListener == null) {
            sender.sendMessage("Selector wand is not available");
            return;
        }
        selectionListener.giveWand(player);
        sender.sendMessage("Display selector given.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Board commands: create/dynamic/list/remove/wand (selector)");
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

    private static String panelId(String boardId, TeamId team) {
        String base = boardId == null ? "" : boardId.trim();
        String suffix = team == null ? "" : team.name().toLowerCase(java.util.Locale.ROOT);
        return base + ":" + suffix;
    }
}
