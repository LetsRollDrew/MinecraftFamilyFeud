package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.util.Validation;

public record FastMoneyQuestionState(
        int questionIndex,
        String surveyId,
        String player1RawAnswer,
        int player1AwardedSlot,
        String player2RawAnswer,
        int player2AwardedSlot) {

    public FastMoneyQuestionState {
        Validation.requireInRange(questionIndex, 1, 5, "questionIndex");
        surveyId = Validation.requireNonBlank(surveyId, "surveyId");
        player1RawAnswer = trimOrEmpty(player1RawAnswer);
        player2RawAnswer = trimOrEmpty(player2RawAnswer);
        player1AwardedSlot = normalizeAwardedSlot(player1AwardedSlot, "player1AwardedSlot");
        player2AwardedSlot = normalizeAwardedSlot(player2AwardedSlot, "player2AwardedSlot");
    }

    public static FastMoneyQuestionState forSurvey(int questionIndex, String surveyId) {
        return new FastMoneyQuestionState(questionIndex, surveyId, "", 0, "", 0);
    }

    public boolean isPlayer1Awarded() {
        return player1AwardedSlot > 0;
    }

    public boolean isPlayer2Awarded() {
        return player2AwardedSlot > 0;
    }

    private static String trimOrEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private static int normalizeAwardedSlot(int slot, String field) {
        if (slot == 0) {
            return 0;
        }
        return Validation.requireInRange(slot, 1, 8, field);
    }
}
