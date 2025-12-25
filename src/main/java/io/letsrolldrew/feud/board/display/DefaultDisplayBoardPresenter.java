package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class DefaultDisplayBoardPresenter implements DisplayBoardPresenter {

    @Override
    public void createBoard(String boardId, Location anchor, Player facingReference) {

    }

    @Override
    public void destroyBoard(String boardId) {

    }

    @Override
    public void setSlot(String boardId, int slotIndex, String answer, Integer points, boolean revealed) {

    }

    @Override
    public void revealSlot(String boardId, int slotIndex, String answer, int points) {

    }

    @Override
    public void hideSlot(String boardId, int slotIndex) {

    }
}
