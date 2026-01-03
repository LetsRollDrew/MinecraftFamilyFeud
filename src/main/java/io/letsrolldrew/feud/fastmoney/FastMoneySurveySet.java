package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.util.Validation;
import java.util.List;
import java.util.Objects;

public record FastMoneySurveySet(
        String id, int targetScore, int player1Seconds, int player2Seconds, List<String> surveyIds) {

    public FastMoneySurveySet {
        id = Validation.requireNonBlank(id, "id");
        targetScore = Validation.requirePositive(targetScore, "targetScore");

        player1Seconds = Validation.requirePositive(player1Seconds, "player1Seconds");
        player2Seconds = Validation.requirePositive(player2Seconds, "player2Seconds");

        surveyIds = List.copyOf(Objects.requireNonNull(surveyIds, "surveyIds"));

        if (surveyIds.size() != 5) {
            throw new IllegalArgumentException("surveyIds must contain exactly 5 survey ids");
        }

        for (String surveyId : surveyIds) {
            Validation.requireNonBlank(surveyId, "surveyId");
        }
    }
}
