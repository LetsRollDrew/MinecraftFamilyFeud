package io.letsrolldrew.feud.effects.board.selection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// store for player display board selections

public final class DisplayBoardSelectionStore {

    private final Map<UUID, DisplayBoardSelection> selections = new ConcurrentHashMap<>();

    public void set(UUID playerId, DisplayBoardSelection selection) {
        if (playerId == null || selection == null) {
            return;
        }
        selections.put(playerId, selection);
    }

    public DisplayBoardSelection get(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return selections.get(playerId);
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }
        selections.remove(playerId);
    }

    public void clearAll() {
        selections.clear();
    }
}
