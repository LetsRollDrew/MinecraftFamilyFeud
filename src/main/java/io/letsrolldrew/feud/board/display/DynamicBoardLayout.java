package io.letsrolldrew.feud.board.display;

import java.util.UUID;
import org.joml.Vector3d;

// Computed layout for a dynamic display board
// anchor is the top-left corner in board space for the given facing (way board is facing)

// Might have to change this to be "DynamicLayout" later
// kinda functions as such so naming it after a board is a bit misleading
// since I do use it for my score panels right now too
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
        Vector3d maxCorner) {}
