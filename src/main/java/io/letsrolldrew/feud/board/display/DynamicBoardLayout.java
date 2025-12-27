package io.letsrolldrew.feud.board.display;

import java.util.UUID;
import org.joml.Vector3d;

// Computed layout for a dynamic display board
// anchor is the top-left corner in board space for the given facing (way board is facing)

public record DynamicBoardLayout(
    UUID worldId,
    BoardFacing facing,
    Vector3d anchor,
    double totalWidth,
    double totalHeight,
    double cellWidth,
    double cellHeight,
    double padX,
    double padY,
    double gapX,
    double gapY,
    double forwardOffset,
    Vector3d minCorner,
    Vector3d maxCorner
) {
}
