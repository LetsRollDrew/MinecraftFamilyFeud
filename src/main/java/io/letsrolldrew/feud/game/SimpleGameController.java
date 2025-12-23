package io.letsrolldrew.feud.game;

import io.letsrolldrew.feud.util.Validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

//Minimal controller to support host actions for now

public final class SimpleGameController implements GameController {
    private final int maxStrikes;
    private final Set<Integer> revealedSlots = new LinkedHashSet<>();
    private int strikeCount;
    private int roundPoints;

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
}
