package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayRegistry;
import java.util.Objects;

public final class TimerPanelPresenter {
    private final DisplayRegistry displayRegistry;

    public TimerPanelPresenter(DisplayRegistry displayRegistry) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
    }

    public void spawnForBoard(String boardId, DynamicBoardLayout layout) {
        if (boardId == null || layout == null) {
            return;
        }
    }

    public void updateForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
    }

    public void removeForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
    }
}
