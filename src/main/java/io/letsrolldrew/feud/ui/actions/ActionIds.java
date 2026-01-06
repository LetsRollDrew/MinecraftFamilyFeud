package io.letsrolldrew.feud.ui.actions;

public final class ActionIds {
    private ActionIds() {}

    // Control
    public static String controlReveal(int slot) {
        return "control.reveal." + slot;
    }

    public static String controlStrike() {
        return "control.strike";
    }

    public static String controlClearStrikes() {
        return "control.clearstrikes";
    }

    public static String controlControlRed() {
        return "control.control.red";
    }

    public static String controlControlBlue() {
        return "control.control.blue";
    }

    public static String controlAward() {
        return "control.award";
    }

    public static String controlReset() {
        return "control.reset";
    }

    // Host config
    public static String hostConfigTeamInfo() {
        return "hostconfig.team.info";
    }

    public static String hostConfigTimerStart() {
        return "hostconfig.timer.start";
    }

    public static String hostConfigTimerStop() {
        return "hostconfig.timer.stop";
    }

    public static String hostConfigTimerReset() {
        return "hostconfig.timer.reset";
    }

    public static String hostConfigTimerStatus() {
        return "hostconfig.timer.status";
    }

    public static String hostConfigBuzzReset() {
        return "hostconfig.buzz.reset";
    }

    // Selector
    public static String selectorViewSelection() {
        return "selector.viewselection";
    }

    public static String selectorGiveSelector() {
        return "selector.giveselector";
    }

    public static String selectorSpawnBoard() {
        return "selector.spawn.board";
    }

    public static String selectorSpawnPanelsRed() {
        return "selector.spawn.panels.red";
    }

    public static String selectorSpawnPanelsBlue() {
        return "selector.spawn.panels.blue";
    }

    public static String selectorSpawnTimer() {
        return "selector.spawn.timer";
    }

    public static String selectorBindBlue() {
        return "selector.bind.blue";
    }

    public static String selectorBindRed() {
        return "selector.bind.red";
    }

    // Surveys
    public static String surveysLoad(String surveyId) {
        return "surveys.load." + surveyId;
    }

    // Fast money
    public static String fastMoneySet(String setId) {
        return "fastmoney.set." + setId;
    }

    public static String fastMoneyBindP1() {
        return "fastmoney.bind.p1";
    }

    public static String fastMoneyBindP2() {
        return "fastmoney.bind.p2";
    }

    public static String fastMoneyBindClear() {
        return "fastmoney.bind.clear";
    }

    public static String fastMoneyStart() {
        return "fastmoney.start";
    }

    public static String fastMoneyStop() {
        return "fastmoney.stop";
    }

    public static String fastMoneyStatus() {
        return "fastmoney.status";
    }

    public static String fastMoneyReveal(int questionIndex, int slot) {
        return "fastmoney.reveal." + questionIndex + "." + slot;
    }
}
