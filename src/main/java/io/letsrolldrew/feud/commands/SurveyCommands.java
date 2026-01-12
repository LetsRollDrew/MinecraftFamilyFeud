package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class SurveyCommands {
    private final Messages messages;
    private final SurveyRepository surveyRepository;
    private final String hostPermission;
    private final GameController controller;

    public SurveyCommands(
            Messages messages, SurveyRepository surveyRepository, String hostPermission, GameController controller) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.surveyRepository = surveyRepository;
        this.hostPermission = hostPermission;
        this.controller = controller;
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
            messages.error(sender, Msg.HOST_ONLY);
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
        if (controller != null) {
            controller.setActiveSurvey(survey);
            sender.sendMessage("Loaded survey: " + surveyId);
        } else {
            sender.sendMessage("Loaded survey: " + surveyId);
        }
        return true;
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage("Survey commands:");
        sender.sendMessage("/feud survey list");
        sender.sendMessage("/feud survey load <id>");
        return true;
    }
}
