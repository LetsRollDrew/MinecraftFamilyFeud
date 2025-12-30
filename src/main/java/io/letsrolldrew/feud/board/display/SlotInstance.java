package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;

public final class SlotInstance {
    private final DisplayKey backgroundKey;
    private final DisplayKey answerTopKey;
    private final DisplayKey answerBottomKey;
    private final DisplayKey pointsKey;
    private final DisplayKey namespaceKey;

    public SlotInstance(DisplayKey backgroundKey, DisplayKey answerTopKey, DisplayKey answerBottomKey, DisplayKey pointsKey) {
        this.backgroundKey = backgroundKey;
        this.answerTopKey = answerTopKey;
        this.answerBottomKey = answerBottomKey;
        this.pointsKey = pointsKey;
        this.namespaceKey = backgroundKey;
    }

    public DisplayKey backgroundKey() {
        return backgroundKey;
    }

    public DisplayKey answerTopKey() {
        return answerTopKey;
    }

    public DisplayKey answerBottomKey() {
        return answerBottomKey;
    }

    // keep for compatibility: "answerKey" means top line
    public DisplayKey answerKey() {
        return answerTopKey;
    }

    public DisplayKey pointsKey() {
        return pointsKey;
    }

    public DisplayKey namespaceKey() {
        return namespaceKey;
    }
}
