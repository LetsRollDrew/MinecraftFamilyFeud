package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;

public final class SlotInstance {
    private final DisplayKey backgroundKey;
    private final DisplayKey answerKey;
    private final DisplayKey pointsKey;
    private final DisplayKey namespaceKey;

    public SlotInstance(DisplayKey backgroundKey, DisplayKey answerKey, DisplayKey pointsKey) {
        this.backgroundKey = backgroundKey;
        this.answerKey = answerKey;
        this.pointsKey = pointsKey;
        this.namespaceKey = backgroundKey;
    }

    public DisplayKey backgroundKey() {
        return backgroundKey;
    }

    public DisplayKey answerKey() {
        return answerKey;
    }

    public DisplayKey pointsKey() {
        return pointsKey;
    }

    public DisplayKey namespaceKey() {
        return namespaceKey;
    }
}
