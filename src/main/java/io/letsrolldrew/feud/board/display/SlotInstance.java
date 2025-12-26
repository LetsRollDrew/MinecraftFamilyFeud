package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
import java.util.UUID;

public final class SlotInstance {
    private final DisplayKey backgroundKey;
    private final DisplayKey answerKey;
    private final DisplayKey pointsKey;

    private UUID backgroundId;
    private UUID answerId;
    private UUID pointsId;

    public SlotInstance(DisplayKey backgroundKey, DisplayKey answerKey, DisplayKey pointsKey) {
        this.backgroundKey = backgroundKey;
        this.answerKey = answerKey;
        this.pointsKey = pointsKey;
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

    public UUID backgroundId() {
        return backgroundId;
    }

    public UUID answerId() {
        return answerId;
    }

    public UUID pointsId() {
        return pointsId;
    }

    public void setBackgroundId(UUID backgroundId) {
        this.backgroundId = backgroundId;
    }

    public void setAnswerId(UUID answerId) {
        this.answerId = answerId;
    }

    public void setPointsId(UUID pointsId) {
        this.pointsId = pointsId;
    }
}
