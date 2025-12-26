package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import org.bukkit.command.CommandSender;

public final class SurveyCommands {
    private final SurveyRepository surveyRepository;
    private final String hostPermission;

    public SurveyCommands(SurveyRepository surveyRepository, String hostPermission) {
        this.surveyRepository = surveyRepository;
        this.hostPermission = hostPermission;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return help(sender);
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("list")) {
            return handleList(sender);
        }
        if (sub.equals("load")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /feud survey load <id>");
                return true;
            }
            return handleLoad(sender, args[1]);
        }
        return help(sender);
    }

    private boolean handleList(CommandSender sender) {
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

    private boolean handleLoad(CommandSender sender, String surveyId) {
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
        // The actual setting of the survey is still handled in FeudRootCommand
        sender.sendMessage("Use /feud survey load <id> via root handler.");
        return true;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage("Survey commands:");
        sender.sendMessage("/feud survey list");
        sender.sendMessage("/feud survey load <id>");
        return true;
    }
}
