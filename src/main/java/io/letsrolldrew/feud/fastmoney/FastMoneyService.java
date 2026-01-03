package io.letsrolldrew.feud.fastmoney;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class FastMoneyService {
    private FastMoneyRoundState state = FastMoneyRoundState.idle();

    public FastMoneyRoundState state() {
        return state;
    }

    public void loadSurveySet(String surveySetId, List<String> surveyIds) {
        // disables change survey set mid round
        requireNotRunning();
        state = FastMoneyRoundState.readyRound(surveySetId, surveyIds);
    }

    public void bindPlayer1(UUID playerId, String playerName) {
        bindPlayer(true, playerId, playerName);
    }

    public void bindPlayer2(UUID playerId, String playerName) {
        bindPlayer(false, playerId, playerName);
    }

    public void startRound() {
        // "ready to start" is the idea
        requireReadyPhase();
        // makes sure both players are bound by host
        requireBound(state.player1(), "player1");
        requireBound(state.player2(), "player2");
        state = new FastMoneyRoundState(
                FastMoneyPhase.PLAYER1_TURN,
                state.surveySetId(),
                state.player1(),
                state.player2(),
                1,
                state.questions());
    }

    public void stop() {
        state = FastMoneyRoundState.idle();
    }

    // for capturing answers during player turns so host
    // can view onhover in their book UI
    public void submitAnswer(UUID playerId, String rawAnswer) {
        FastMoneyPhase phase = state.phase();
        if (phase != FastMoneyPhase.PLAYER1_TURN && phase != FastMoneyPhase.PLAYER2_TURN) {
            throw new IllegalStateException("Cannot submit answers when no player turn is active");
        }

        Objects.requireNonNull(playerId, "playerId");
        FastMoneyRoundState.BoundPlayer expected;
        if (phase == FastMoneyPhase.PLAYER1_TURN) {
            expected = state.player1();
        } else {
            expected = state.player2();
        }
        if (!playerId.equals(expected.playerId())) {
            throw new IllegalArgumentException("Answer submitted by the wrong player");
        }

        // update only the current question
        int questionIndex = currentQuestionIndexOrThrow();
        FastMoneyQuestionState current = state.questions().get(questionIndex - 1);
        FastMoneyQuestionState updated;
        if (phase == FastMoneyPhase.PLAYER1_TURN) {
            updated = withPlayer1Answer(current, rawAnswer);
        } else {
            updated = withPlayer2Answer(current, rawAnswer);
        }

        List<FastMoneyQuestionState> questions = new ArrayList<>(state.questions());
        questions.set(questionIndex - 1, updated);
        state = new FastMoneyRoundState(
                phase, state.surveySetId(), state.player1(), state.player2(), state.activeQuestionIndex(), questions);
    }

    public void advanceQuestion() {
        ensurePlayerTurnActive();

        int current = currentQuestionIndexOrThrow();
        if (current >= state.questions().size()) {
            throw new IllegalStateException("No more questions remain for this player");
        }

        state = new FastMoneyRoundState(
                state.phase(), state.surveySetId(), state.player1(), state.player2(), current + 1, state.questions());
    }

    public void beginPlayer2Turn() {
        if (state.phase() != FastMoneyPhase.PLAYER1_TURN) {
            throw new IllegalStateException("Player 2 turn can only start after Player 1 finishes");
        }
        if (state.activeQuestionIndex() != state.questions().size()) {
            throw new IllegalStateException("Player 1 must complete all questions before Player 2 begins");
        }
        // reset question index when swapping to player 2
        state = new FastMoneyRoundState(
                FastMoneyPhase.PLAYER2_TURN,
                state.surveySetId(),
                state.player1(),
                state.player2(),
                1,
                state.questions());
    }

    public void completeRound() {
        if (state.phase() != FastMoneyPhase.PLAYER2_TURN) {
            throw new IllegalStateException("Round can only complete after Player 2 turn");
        }
        if (state.activeQuestionIndex() != state.questions().size()) {
            throw new IllegalStateException("Player 2 must complete all questions before finishing");
        }
        // end of phases mark complete
        state = new FastMoneyRoundState(
                FastMoneyPhase.COMPLETE,
                state.surveySetId(),
                state.player1(),
                state.player2(),
                state.activeQuestionIndex(),
                state.questions());
    }

    private void bindPlayer(boolean isPlayer1, UUID playerId, String playerName) {
        requireReadyPhase();
        Objects.requireNonNull(playerId, "playerId");
        FastMoneyRoundState.BoundPlayer bound = new FastMoneyRoundState.BoundPlayer(playerId, playerName);
        state = isPlayer1
                ? new FastMoneyRoundState(
                        state.phase(),
                        state.surveySetId(),
                        bound,
                        state.player2(),
                        state.activeQuestionIndex(),
                        state.questions())
                : new FastMoneyRoundState(
                        state.phase(),
                        state.surveySetId(),
                        state.player1(),
                        bound,
                        state.activeQuestionIndex(),
                        state.questions());
    }

    private void requireNotRunning() {
        if (state.phase() == FastMoneyPhase.PLAYER1_TURN || state.phase() == FastMoneyPhase.PLAYER2_TURN) {
            throw new IllegalStateException("Cannot load a survey set while a round is active");
        }
    }

    private void requireReadyPhase() {
        if (state.phase() != FastMoneyPhase.READY) {
            throw new IllegalStateException("Requires READY phase");
        }
    }

    private void ensurePlayerTurnActive() {
        if (state.phase() != FastMoneyPhase.PLAYER1_TURN && state.phase() != FastMoneyPhase.PLAYER2_TURN) {
            throw new IllegalStateException("No player turn is active");
        }
    }

    private void requireBound(FastMoneyRoundState.BoundPlayer player, String label) {
        if (player == null || !player.isBound()) {
            throw new IllegalStateException(label + " must be bound");
        }
    }

    private int currentQuestionIndexOrThrow() {
        int index = state.activeQuestionIndex();
        if (index < 1 || index > state.questions().size()) {
            throw new IllegalStateException("Active question is not set");
        }
        return index;
    }

    private static FastMoneyQuestionState withPlayer1Answer(FastMoneyQuestionState current, String rawAnswer) {
        return new FastMoneyQuestionState(
                current.questionIndex(),
                current.surveyId(),
                rawAnswer.trim(),
                current.player1AwardedSlot(),
                current.player2RawAnswer(),
                current.player2AwardedSlot());
    }

    private static FastMoneyQuestionState withPlayer2Answer(FastMoneyQuestionState current, String rawAnswer) {
        return new FastMoneyQuestionState(
                current.questionIndex(),
                current.surveyId(),
                current.player1RawAnswer(),
                current.player1AwardedSlot(),
                rawAnswer.trim(),
                current.player2AwardedSlot());
    }
}
