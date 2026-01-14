package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
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
                messages.usage(sender, Msg.USAGE_SURVEY_LOAD);
                return true;
            }
            return handleLoad(sender, args[1]);
        }
        return help(sender);
    }

    private boolean handleList(CommandSender sender) {
        if (surveyRepository == null) {
            messages.error(sender, Msg.SURVEYS_NOT_LOADED);
            return true;
        }
        if (surveyRepository.listAll().isEmpty()) {
            messages.info(sender, Msg.SURVEY_LIST_EMPTY);
            return true;
        }
        messages.info(sender, Msg.SURVEY_LIST_HEADER);
        for (Survey survey : surveyRepository.listAll()) {
            messages.info(
                    sender,
                    Msg.SURVEY_LIST_ENTRY,
                    Placeholder.of("id", survey.id()),
                    Placeholder.of("question", survey.question()));
        }
        return true;
    }

    private boolean handleLoad(CommandSender sender, String surveyId) {
        if (!sender.hasPermission(hostPermission)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }
        if (surveyRepository == null) {
            messages.error(sender, Msg.SURVEYS_NOT_LOADED);
            return true;
        }
        Survey survey = surveyRepository.findById(surveyId).orElse(null);
        if (survey == null) {
            messages.error(sender, Msg.SURVEY_NOT_FOUND, Placeholder.of("id", surveyId));
            return true;
        }
        if (controller != null) {
            controller.setActiveSurvey(survey);
        }
        messages.success(sender, Msg.SURVEY_LOADED, Placeholder.of("id", surveyId));
        return true;
    }

    private boolean help(CommandSender sender) {
        messages.usage(sender, Msg.SURVEY_HELP);
        return true;
    }
}
