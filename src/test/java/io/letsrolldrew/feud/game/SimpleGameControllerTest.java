package io.letsrolldrew.feud.game;

import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.game.TeamControl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleGameControllerTest {

    @Test
    void tracksRevealedSlots() {
        SimpleGameController controller = new SimpleGameController(3);
        controller.revealSlot(1);
        controller.revealSlot(3);

        assertTrue(controller.revealedSlots().contains(1));
        assertTrue(controller.revealedSlots().contains(3));
    }

    @Test
    void rejectsInvalidSlot() {
        SimpleGameController controller = new SimpleGameController(3);
        assertThrows(IllegalArgumentException.class, () -> controller.revealSlot(0));
        assertThrows(IllegalArgumentException.class, () -> controller.revealSlot(9));
    }

    @Test
    void tracksStrikesUpToMax() {
        SimpleGameController controller = new SimpleGameController(2);
        controller.strike();
        controller.strike();
        controller.strike(); // should not exceed max

        assertEquals(2, controller.strikeCount());
    }

    @Test
    void clearsStrikes() {
        SimpleGameController controller = new SimpleGameController(3);
        controller.strike();
        controller.clearStrikes();
        assertEquals(0, controller.strikeCount());
    }

    @Test
    void addsPoints() {
        SimpleGameController controller = new SimpleGameController(3);
        controller.addPoints(10);
        controller.addPoints(5);
        assertEquals(15, controller.roundPoints());
    }

    @Test
    void rejectsInvalidPoints() {
        SimpleGameController controller = new SimpleGameController(3);
        assertThrows(IllegalArgumentException.class, () -> controller.addPoints(0));
        assertThrows(IllegalArgumentException.class, () -> controller.addPoints(-5));
    }

    @Test
    void revealAddsAnswerPointsOnce() {
        SimpleGameController controller = new SimpleGameController(3);
        Survey survey = new Survey(
            "sample",
            "Sample Survey",
            "Q",
            java.util.List.of(
                new AnswerOption("One", 10, java.util.List.of()),
                new AnswerOption("Two", 5, java.util.List.of())
            )
        );
        controller.setActiveSurvey(survey);
        controller.revealSlot(1);
        controller.revealSlot(1); // no double-count
        controller.revealSlot(2);
        assertEquals(15, controller.roundPoints());
    }

    @Test
    void controlAndAwardMovesPoints() {
        SimpleGameController controller = new SimpleGameController(3);
        controller.setControllingTeam(TeamControl.RED);
        controller.addPoints(20);
        controller.awardRoundPoints();
        assertEquals(20, controller.redScore());
        assertEquals(0, controller.roundPoints());
    }

    @Test
    void resetRoundClearsStateButKeepsSurvey() {
        SimpleGameController controller = new SimpleGameController(3);
        controller.addPoints(10);
        controller.strike();
        controller.revealSlot(1);
        controller.setControllingTeam(TeamControl.BLUE);
        controller.resetRoundState();
        assertEquals(0, controller.roundPoints());
        assertEquals(0, controller.strikeCount());
        assertEquals(0, controller.revealedSlots().size());
        assertEquals(TeamControl.NONE, controller.controllingTeam());
    }

    @Test
    void providesHoverTextsFromActiveSurvey() {
        SimpleGameController controller = new SimpleGameController(3);
        Survey survey = new Survey(
            "sample",
            "Sample Survey",
            "Q",
            java.util.List.of(
                new AnswerOption("One", 10, java.util.List.of()),
                new AnswerOption("Two", 5, java.util.List.of())
            )
        );
        controller.setActiveSurvey(survey);
        java.util.List<String> hovers = controller.slotHoverTexts();
        assertEquals("Reveal (One: 10)", hovers.get(0));
        assertEquals("Reveal (Two: 5)", hovers.get(1));
        assertEquals("Reveal (AnswerName: Points)", hovers.get(2));
    }
}
