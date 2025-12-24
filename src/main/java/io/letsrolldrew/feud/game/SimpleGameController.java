package io.letsrolldrew.feud.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.util.Validation;

//Minimal controller to support host actions for now

public final class SimpleGameController implements GameController {
    private final int maxStrikes;
    private final Set<Integer> revealedSlots = new LinkedHashSet<>();
    private int strikeCount;
    private int roundPoints;
    private int redScore;
    private int blueScore;
    private TeamControl controllingTeam = TeamControl.NONE;
    private Survey activeSurvey;

    public SimpleGameController(int maxStrikes) {
        this.maxStrikes = Validation.requirePositive(maxStrikes, "maxStrikes");
    }

    @Override
    public void revealSlot(int slotIndex) {
        Validation.requireInRange(slotIndex, 1, 8, "slotIndex");
        boolean added = revealedSlots.add(slotIndex);
        if (added && activeSurvey != null && slotIndex - 1 < activeSurvey.answers().size()) {
            AnswerOption option = activeSurvey.answers().get(slotIndex - 1);
            roundPoints += option.points();
        }
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
        this.controllingTeam = TeamControl.NONE;
    }

    @Override
    public Survey getActiveSurvey() {
        return activeSurvey;
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
    public int redScore() {
        return redScore;
    }

    @Override
    public int blueScore() {
        return blueScore;
    }

    @Override
    public TeamControl controllingTeam() {
        return controllingTeam;
    }

    @Override
    public void setControllingTeam(TeamControl team) {
        this.controllingTeam = team == null ? TeamControl.NONE : team;
    }

    @Override
    public void awardRoundPoints() {
        if (roundPoints <= 0) {
            return;
        }
        switch (controllingTeam) {
            case RED -> redScore += roundPoints;
            case BLUE -> blueScore += roundPoints;
            case NONE -> {
                // no controlling team; do nothing
                return;
            }
        }
        roundPoints = 0;
        strikeCount = 0;
        revealedSlots.clear();
    }

    @Override
    public void resetRoundState() {
        roundPoints = 0;
        strikeCount = 0;
        revealedSlots.clear();
        controllingTeam = TeamControl.NONE;
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
