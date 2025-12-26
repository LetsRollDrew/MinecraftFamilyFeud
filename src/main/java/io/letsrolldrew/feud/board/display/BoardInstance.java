package io.letsrolldrew.feud.board.display;

import java.util.List;
import org.bukkit.Location;

public final class BoardInstance {
    private final String boardId;
    private final Location anchor;
    private final float yaw;
    private final List<SlotInstance> slots;

    public BoardInstance(String boardId, Location anchor, float yaw, List<SlotInstance> slots) {
        this.boardId = boardId;
        this.anchor = anchor;
        this.yaw = yaw;
        this.slots = slots;
    }

    public String boardId() {
        return boardId;
    }

    public Location anchor() {
        return anchor;
    }

    public float yaw() {
        return yaw;
    }

    public List<SlotInstance> slots() {
        return slots;
    }
}
