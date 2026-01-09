package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.effects.buzz.BuzzerCommands;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.effects.timer.TimerCommands;
import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import io.letsrolldrew.feud.team.TeamCommands;
import java.util.Objects;

// aggregate the command modules so callers arent cluttered with args

public final class CommandModules {
    private final HologramCommands hologramCommands;
    private final DisplayBoardCommands displayBoardCommands;
    private final SurveyCommands surveyCommands;
    private final TeamCommands teamCommands;
    private final TimerCommands timerCommands;
    private final BuzzerCommands buzzerCommands;
    private final FastMoneyCommands fastMoneyCommands;

    public CommandModules(
            HologramCommands hologramCommands,
            DisplayBoardCommands displayBoardCommands,
            SurveyCommands surveyCommands,
            TeamCommands teamCommands,
            TimerCommands timerCommands,
            BuzzerCommands buzzerCommands,
            FastMoneyCommands fastMoneyCommands) {
        this.hologramCommands = Objects.requireNonNull(hologramCommands, "hologramCommands");
        this.displayBoardCommands = Objects.requireNonNull(displayBoardCommands, "displayBoardCommands");
        this.surveyCommands = Objects.requireNonNull(surveyCommands, "surveyCommands");
        this.teamCommands = Objects.requireNonNull(teamCommands, "teamCommands");
        this.timerCommands = Objects.requireNonNull(timerCommands, "timerCommands");
        this.buzzerCommands = Objects.requireNonNull(buzzerCommands, "buzzerCommands");
        this.fastMoneyCommands = Objects.requireNonNull(fastMoneyCommands, "fastMoneyCommands");
    }

    public HologramCommands hologramCommands() {
        return hologramCommands;
    }

    public DisplayBoardCommands displayBoardCommands() {
        return displayBoardCommands;
    }

    public SurveyCommands surveyCommands() {
        return surveyCommands;
    }

    public TeamCommands teamCommands() {
        return teamCommands;
    }

    public TimerCommands timerCommands() {
        return timerCommands;
    }

    public BuzzerCommands buzzerCommands() {
        return buzzerCommands;
    }

    public FastMoneyCommands fastMoneyCommands() {
        return fastMoneyCommands;
    }
}
