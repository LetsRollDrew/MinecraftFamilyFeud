package io.letsrolldrew.feud.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FastMoneyServiceTest {
    private static final List<String> SURVEY_IDS = List.of("q1", "q2", "q3", "q4", "q5");

    private FastMoneyService service;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        service = new FastMoneyService();
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
    }

    @Test
    void loadMovesToReadyWithQuestions() {
        service.loadSurveySet("s1", SURVEY_IDS);

        FastMoneyRoundState state = service.state();
        assertEquals(FastMoneyPhase.READY, state.phase());
        assertEquals(0, state.activeQuestionIndex());
        assertEquals(5, state.questions().size());
        assertEquals(1, state.questions().get(0).questionIndex());
        assertEquals("q1", state.questions().get(0).surveyId());
    }

    @Test
    void startRequiresBothPlayersBound() {
        service.loadSurveySet("s1", SURVEY_IDS);
        assertThrows(IllegalStateException.class, service::startRound);

        service.bindPlayer1(player1Id, "P1");
        assertThrows(IllegalStateException.class, service::startRound);

        service.bindPlayer2(player2Id, "P2");
        service.startRound();

        FastMoneyRoundState state = service.state();
        assertEquals(FastMoneyPhase.PLAYER1_TURN, state.phase());
        assertEquals(1, state.activeQuestionIndex());
    }

    @Test
    void submitAnswerStoresTrimmedTextForActivePlayer() {
        startPlayer1Turn();

        service.submitAnswer(player1Id, "  apples  ");

        FastMoneyQuestionState question = service.state().questions().get(0);
        assertEquals("apples", question.player1RawAnswer());
        assertEquals("", question.player2RawAnswer());
    }

    @Test
    void submitAnswerRejectsWrongPlayerDuringTurn() {
        startPlayer1Turn();

        assertThrows(IllegalArgumentException.class, () -> service.submitAnswer(player2Id, "bananas"));
    }

    @Test
    void advanceQuestionMovesWithinBounds() {
        startPlayer1Turn();

        service.advanceQuestion();
        assertEquals(2, service.state().activeQuestionIndex());

        service.advanceQuestion();
        service.advanceQuestion();
        service.advanceQuestion();
        assertEquals(5, service.state().activeQuestionIndex());
        assertThrows(IllegalStateException.class, service::advanceQuestion);
    }

    @Test
    void player2TurnRequiresPlayer1CompletingQuestions() {
        startPlayer1Turn();
        assertThrows(IllegalStateException.class, service::beginPlayer2Turn);

        advanceToLastQuestion();
        service.beginPlayer2Turn();

        FastMoneyRoundState state = service.state();
        assertEquals(FastMoneyPhase.PLAYER2_TURN, state.phase());
        assertEquals(1, state.activeQuestionIndex());
    }

    @Test
    void player2AnswersOnlyDuringTheirTurn() {
        startPlayer1Turn();
        advanceToLastQuestion();
        service.beginPlayer2Turn();

        service.submitAnswer(player2Id, "grapes");

        FastMoneyQuestionState question = service.state().questions().get(0);
        assertEquals("grapes", question.player2RawAnswer());
    }

    @Test
    void stopResetsToIdle() {
        startPlayer1Turn();
        service.stop();

        FastMoneyRoundState state = service.state();
        assertEquals(FastMoneyPhase.IDLE, state.phase());
        assertEquals("", state.surveySetId());
        assertEquals(0, state.activeQuestionIndex());
    }

    @Test
    void awardPlayer1SetsSlotForActiveQuestion() {
        startPlayer1Turn();

        service.awardPlayer1(1, 3);

        FastMoneyQuestionState question = service.state().questions().get(0);
        assertEquals(3, question.player1AwardedSlot());
    }

    @Test
    void awardRejectsInvalidQuestionIndexOrInactiveQuestion() {
        startPlayer1Turn();

        assertThrows(IllegalArgumentException.class, () -> service.awardPlayer1(0, 1));
        assertThrows(IllegalArgumentException.class, () -> service.awardPlayer1(6, 1));
        assertThrows(IllegalStateException.class, () -> service.awardPlayer1(2, 1));
    }

    @Test
    void awardPlayer2RejectsPlayer1Slot() {
        startPlayer1Turn();
        service.awardPlayer1(1, 2);
        advanceToLastQuestion();
        service.beginPlayer2Turn();

        assertThrows(IllegalStateException.class, () -> service.awardPlayer2(1, 2));
    }

    private void startPlayer1Turn() {
        service.loadSurveySet("s1", SURVEY_IDS);
        service.bindPlayer1(player1Id, "P1");
        service.bindPlayer2(player2Id, "P2");
        service.startRound();
    }

    private void advanceToLastQuestion() {
        service.advanceQuestion();
        service.advanceQuestion();
        service.advanceQuestion();
        service.advanceQuestion();
    }
}
