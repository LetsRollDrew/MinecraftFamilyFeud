package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class FeudRootCommand implements CommandExecutor {
    private final Plugin plugin;
    private final SurveyRepository surveyRepository;
    private final UiCommand uiCommand;

    public FeudRootCommand(Plugin plugin, SurveyRepository surveyRepository, UiCommand uiCommand) {
        this.plugin = plugin;
        this.surveyRepository = surveyRepository;
        this.uiCommand = uiCommand;
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

        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("list")) {
            return handleSurveyList(sender);
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
}
