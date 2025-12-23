package io.letsrolldrew.feud.game;

import io.letsrolldrew.feud.survey.Survey;

import java.util.List;
import java.util.Set;

//Core game control surface for host actions

public interface GameController {
    void revealSlot(int slotIndex);

    void strike();

    void clearStrikes();

    void addPoints(int points);

    void setActiveSurvey(Survey survey);

    Survey getActiveSurvey();

    int strikeCount();

    int maxStrikes();

    int roundPoints();

    Set<Integer> revealedSlots();

    List<String> slotHoverTexts();
}
