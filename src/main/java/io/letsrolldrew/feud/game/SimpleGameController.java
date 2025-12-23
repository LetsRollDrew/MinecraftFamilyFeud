package io.letsrolldrew.feud.game;

import io.letsrolldrew.feud.util.Validation;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.AnswerOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

//Minimal controller to support host actions for now

public final class SimpleGameController implements GameController {
    private final int maxStrikes;
    private final Set<Integer> revealedSlots = new LinkedHashSet<>();
    private int strikeCount;
    private int roundPoints;
    private Survey activeSurvey;

    public SimpleGameController(int maxStrikes) {
        this.maxStrikes = Validation.requirePositive(maxStrikes, "maxStrikes");
    }

    @Override
    public void revealSlot(int slotIndex) {
        Validation.requireInRange(slotIndex, 1, 8, "slotIndex");
        revealedSlots.add(slotIndex);
    }

    @Override
    public void strike() {
        if (strikeCount < maxStrikes) {
            strikeCount++;
        }
    }

    @Override
    public void clearStrikes() {
        strikeCount = 0;
    }

    @Override
    public void addPoints(int points) {
        Validation.requirePositive(points, "points");
        roundPoints += points;
    }

    @Override
    public void setActiveSurvey(Survey survey) {
        this.activeSurvey = survey;
        this.revealedSlots.clear();
        this.strikeCount = 0;
        this.roundPoints = 0;
    }

    @Override
    public int strikeCount() {
        return strikeCount;
    }

    @Override
    public int maxStrikes() {
        return maxStrikes;
    }

    @Override
    public int roundPoints() {
        return roundPoints;
    }

    @Override
    public Set<Integer> revealedSlots() {
        return Collections.unmodifiableSet(revealedSlots);
    }

    @Override
    public List<String> slotHoverTexts() {
        List<String> hovers = new ArrayList<>(8);
        if (activeSurvey == null) {
            for (int i = 0; i < 8; i++) {
                hovers.add("Reveal (AnswerName: Points)");
            }
            return hovers;
        }
        List<AnswerOption> answers = activeSurvey.answers();
        for (int i = 0; i < 8; i++) {
            if (i < answers.size()) {
                AnswerOption option = answers.get(i);
                hovers.add("Reveal (" + option.text() + ": " + option.points() + ")");
            } else {
                hovers.add("Reveal (AnswerName: Points)");
            }
        }
        return hovers;
    }
}
