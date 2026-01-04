package io.letsrolldrew.feud.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

final class FastMoneyRoundStateTest {

    @Test
    void idleStartsUnboundAndEmpty() {
        FastMoneyRoundState state = FastMoneyRoundState.idle();

        assertEquals(FastMoneyPhase.IDLE, state.phase());
        assertEquals("", state.surveySetId());
        assertFalse(state.player1().isBound());
        assertFalse(state.player2().isBound());
        assertEquals(0, state.activeQuestionIndex());
        assertEquals(List.of(), state.questions());
    }

    @Test
    void readyRoundRequiresExactlyFiveSurveys() {
        assertThrows(IllegalArgumentException.class, () -> FastMoneyRoundState.readyRound("s1", List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> FastMoneyRoundState.readyRound("s1", List.of("a", "b", "c", "d")));
        assertThrows(
                IllegalArgumentException.class,
                () -> FastMoneyRoundState.readyRound("s1", List.of("a", "b", "c", "d", "e", "f")));
    }

    @Test
    void readyRoundBuildsFiveQuestions() {
        FastMoneyRoundState state = FastMoneyRoundState.readyRound("s1", List.of("a", "b", "c", "d", "e"));

        assertEquals(FastMoneyPhase.READY, state.phase());
        assertEquals("s1", state.surveySetId());
        assertEquals(5, state.questions().size());
        assertEquals(1, state.questions().get(0).questionIndex());
        assertEquals("a", state.questions().get(0).surveyId());
        assertEquals(5, state.questions().get(4).questionIndex());
        assertEquals("e", state.questions().get(4).surveyId());
    }
}
