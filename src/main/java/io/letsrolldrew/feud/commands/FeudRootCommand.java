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

    public FeudRootCommand(Plugin plugin, SurveyRepository surveyRepository) {
        this.plugin = plugin;
        this.surveyRepository = surveyRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("list")) {
            return handleSurveyList(sender);
        }

        String version = plugin.getDescription().getVersion();
        sender.sendMessage("FamilyFeud v" + version + " - game state: not started");
        sender.sendMessage("Use /feud survey list to see loaded surveys");
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
