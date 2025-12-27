package io.letsrolldrew.feud.effects.board.selection;

import io.letsrolldrew.feud.board.display.BoardFacing;
import java.util.Objects;
import java.util.UUID;
import org.joml.Vector3d;


// Immutable selection of a rectangular surface for spawning a display board
// just a snapshot of the board data like we do in map bindings

public record DisplayBoardSelection(
    UUID worldId,
    Vector3d cornerA,
    Vector3d cornerB,
    BoardFacing facing,
    Vector3d surfaceNormal
)
{
    public boolean isValid() {
        return invalidReason() == null;
    }

    public String invalidReason() {

        if (worldId == null) {
            return "worldId missing";
        }

        if (cornerA == null || cornerB == null) {
            return "both corners required";
        }

        if (Objects.equals(cornerA, cornerB)) {
            return "corners must differ";
        }

        if (facing == null) {
            return "facing missing";
        }

        //
        if (surfaceNormal == null || surfaceNormal.lengthSquared() == 0.0) {
            return "surface normal missing";
        }

        return null;
    }

    public boolean isInvalid() {
        return !isValid();
    }
}
