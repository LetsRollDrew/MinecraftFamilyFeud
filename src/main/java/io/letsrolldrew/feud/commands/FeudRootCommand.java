package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.letsrolldrew.feud.game.GameController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class FeudRootCommand implements CommandExecutor {
    private final Plugin plugin;
    private final SurveyRepository surveyRepository;
    private final UiCommand uiCommand;
    private final String hostPermission;
    private final GameController gameController;
    private final HostBookUiBuilder hostBookUiBuilder;
    private final HostRemoteService hostRemoteService;

    public FeudRootCommand(
        Plugin plugin,
        SurveyRepository surveyRepository,
        UiCommand uiCommand,
        HostBookUiBuilder hostBookUiBuilder,
        HostRemoteService hostRemoteService,
        String hostPermission,
        GameController gameController
    ) {
        this.plugin = plugin;
        this.surveyRepository = surveyRepository;
        this.uiCommand = uiCommand;
        this.hostBookUiBuilder = hostBookUiBuilder;
        this.hostRemoteService = hostRemoteService;
        this.hostPermission = hostPermission;
        this.gameController = gameController;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return handleVersion(sender);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            return handleHelp(sender);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("version")) {
            return handleVersion(sender);
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("ui")) {
            String[] remaining = new String[Math.max(0, args.length - 1)];
            if (args.length > 1) {
                System.arraycopy(args, 1, remaining, 0, args.length - 1);
            }
            return uiCommand.handle(sender, remaining);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("host") && args[1].equalsIgnoreCase("book")) {
            return handleHostBook(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("list")) {
            return handleSurveyList(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("load")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /feud survey load <id>");
                return true;
            }
            return handleSurveyLoad(sender, args[2]);
        }

        return handleHelp(sender);
    }

    private boolean handleVersion(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        sender.sendMessage("FamilyFeud v" + version + " - game state: not started");
        sender.sendMessage("Use /feud help for commands.");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage("FamilyFeud commands:");
        sender.sendMessage("/feud - show version");
        sender.sendMessage("/feud help - this help");
        sender.sendMessage("/feud version - show version");
        sender.sendMessage("/feud survey list - list loaded surveys");
        sender.sendMessage("/feud survey load <id> - set active survey");
        sender.sendMessage("/feud host book - give host remote");
        sender.sendMessage("/feud ui reveal <1-8> - reveal slot");
        sender.sendMessage("/feud ui strike - add a strike");
        sender.sendMessage("/feud ui clearstrikes - clear strikes");
        sender.sendMessage("/feud ui add <points> - add points to round");
        return true;
    }

    private boolean handleSurveyList(CommandSender sender) {
        if (surveyRepository == null) {
            sender.sendMessage("Surveys not loaded.");
            return true;
        }
        if (surveyRepository.listAll().isEmpty()) {
            sender.sendMessage("No surveys loaded.");
            return true;
        }
        sender.sendMessage("Loaded surveys:");
        for (Survey survey : surveyRepository.listAll()) {
            sender.sendMessage("- " + survey.id() + ": " + survey.question());
        }
        return true;
    }

    private boolean handleSurveyLoad(CommandSender sender, String surveyId) {
        if (!sender.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to do that.");
            return true;
        }
        if (surveyRepository == null) {
            sender.sendMessage("Surveys not loaded.");
            return true;
        }
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            sender.sendMessage("Survey not found: " + surveyId);
            return true;
        }
        gameController.setActiveSurvey(survey);
        sender.sendMessage("Active survey set to " + survey.id() + ": " + survey.question());
        if (sender instanceof Player player) {
            giveOrReplaceHostBook(player);
        }
        return true;
    }

    private boolean handleHostBook(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the host book.");
            return true;
        }
        if (!player.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to use this.");
            return true;
        }
        giveOrReplaceHostBook(player);
        player.sendMessage("Host remote given.");
        return true;
    }

    private void giveOrReplaceHostBook(Player player) {
        var fresh = hostBookUiBuilder.createBook(gameController.slotHoverTexts());
        hostRemoteService.giveOrReplace(player, fresh);
    }
}
