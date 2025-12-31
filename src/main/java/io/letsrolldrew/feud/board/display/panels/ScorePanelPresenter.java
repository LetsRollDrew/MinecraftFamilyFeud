package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.team.TeamService;
import java.util.Objects;

public final class ScorePanelPresenter {
    private final DisplayRegistry displayRegistry;
    private final TeamService teamService;

    public ScorePanelPresenter(DisplayRegistry displayRegistry, TeamService teamService) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
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
