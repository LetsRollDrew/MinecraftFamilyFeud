package io.letsrolldrew.feud.team;

import java.util.Objects;

public record TeamState(String displayName, int score, BlockRef buzzer) {
    public TeamState {
        Objects.requireNonNull(displayName, "displayName");
        displayName = displayName.trim();

        // validate exists
        if (displayName.isEmpty()) {
            throw new IllegalArgumentException("displayName cant be blank");
        }
        if (score < 0) {
            throw new IllegalArgumentException("score must be >= 0");
        }
    }
}
