package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.util.Validation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record FastMoneyRoundState(
        FastMoneyPhase phase,
        String surveySetId,
        BoundPlayer player1,
        BoundPlayer player2,
        int activeQuestionIndex,
        List<FastMoneyQuestionState> questions) {

    public FastMoneyRoundState {
        phase = Objects.requireNonNull(phase, "phase");
        surveySetId = trimOrEmpty(surveySetId);
        player1 = (player1 == null) ? BoundPlayer.unbound() : player1;
        player2 = (player2 == null) ? BoundPlayer.unbound() : player2;
        Validation.requireInRange(activeQuestionIndex, 0, 5, "activeQuestionIndex");

        questions = (questions == null) ? List.of() : List.copyOf(questions);
        for (FastMoneyQuestionState question : questions) {
            Objects.requireNonNull(question, "questions element");
        }
    }

    public static FastMoneyRoundState idle() {
        return new FastMoneyRoundState(
                FastMoneyPhase.IDLE, "", BoundPlayer.unbound(), BoundPlayer.unbound(), 0, List.of());
    }

    public static FastMoneyRoundState readyRound(String surveySetId, List<String> surveyIds) {
        String normalizedSetId = Validation.requireNonBlank(surveySetId, "surveySetId");

        if (surveyIds == null || surveyIds.size() != 5) {
            throw new IllegalArgumentException("surveyIds must contain exactly 5 entries");
        }

        List<FastMoneyQuestionState> questions = new ArrayList<>(surveyIds.size());
        for (int i = 0; i < surveyIds.size(); i++) {
            int questionIndex = i + 1;
            questions.add(FastMoneyQuestionState.forSurvey(questionIndex, surveyIds.get(i)));
        }

        return new FastMoneyRoundState(
                FastMoneyPhase.READY, normalizedSetId, BoundPlayer.unbound(), BoundPlayer.unbound(), 0, questions);
    }

    public record BoundPlayer(UUID playerId, String playerName) {
        public BoundPlayer {
            playerName = trimOrEmpty(playerName);
        }

        public static BoundPlayer unbound() {
            return new BoundPlayer(null, "");
        }

        public boolean isBound() {
            return playerId != null;
        }
    }

    private static String trimOrEmpty(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
