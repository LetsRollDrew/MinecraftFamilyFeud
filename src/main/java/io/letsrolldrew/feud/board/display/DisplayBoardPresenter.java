package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface DisplayBoardPresenter {
    void createBoard(String boardId, Location anchor, Player facingReference);

    void destroyBoard(String boardId);

    void setSlot(String boardId, int slotIndex, String answer, Integer points, boolean revealed);

    void revealSlot(String boardId, int slotIndex, String answer, int points);

    void hideSlot(String boardId, int slotIndex);

    void clearAll();
}
