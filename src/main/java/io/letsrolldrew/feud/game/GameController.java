package io.letsrolldrew.feud.game;

import java.util.Set;

//Core game control surface for host actions

public interface GameController {
    void revealSlot(int slotIndex);

    void strike();

    void clearStrikes();

    void addPoints(int points);

    int strikeCount();

    int maxStrikes();

    int roundPoints();

    Set<Integer> revealedSlots();
}
