package io.letsrolldrew.feud.board.display;

public record BoardLayout(
    int slotRows,
    int slotCols,
    double slotWidth,
    double slotHeight,
    double columnGap,
    double rowGap,
    float backgroundScaleX,
    float backgroundScaleY,
    float backgroundScaleZ,
    float textScale,
    double textZOffset
) {
    public static BoardLayout defaultLayout() {
        return new BoardLayout(
            4,
            2,
            3.8,
            0.85,
            0.45,
            0.25,
            3.2f,
            1.0f,
            1.0f,
            0.7f,
            0.02
        );
    }
}
