package io.letsrolldrew.feud.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class FastMoneyQuestionStateTest {

    @Test
    void forSurveyCreatesEmptyPlayerState() {
        FastMoneyQuestionState state = FastMoneyQuestionState.forSurvey(1, "survey_1");

        assertEquals(1, state.questionIndex());
        assertEquals("survey_1", state.surveyId());
        assertEquals("", state.player1RawAnswer());
        assertEquals(0, state.player1AwardedSlot());
        assertEquals("", state.player2RawAnswer());
        assertEquals(0, state.player2AwardedSlot());
    }

    @Test
    void rejectsQuestionIndexOutsideOneToFive() {
        assertThrows(IllegalArgumentException.class, () -> new FastMoneyQuestionState(0, "survey", "", 0, "", 0));
        assertThrows(IllegalArgumentException.class, () -> new FastMoneyQuestionState(6, "survey", "", 0, "", 0));
    }

    @Test
    void rejectsAwardedSlotOutsideZeroToEight() {
        assertThrows(IllegalArgumentException.class, () -> new FastMoneyQuestionState(1, "survey", "", -1, "", 0));
        assertThrows(IllegalArgumentException.class, () -> new FastMoneyQuestionState(1, "survey", "", 9, "", 0));
    }
}
