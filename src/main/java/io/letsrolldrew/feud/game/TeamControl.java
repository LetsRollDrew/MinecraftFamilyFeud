package io.letsrolldrew.feud.game;

public enum TeamControl {
    RED,
    BLUE,
    NONE;

    public static TeamControl fromString(String value) {
        if (value == null) {
            return NONE;
        }
        return switch (value.toLowerCase()) {
            case "red" -> RED;
            case "blue" -> BLUE;
            default -> NONE;
        };
    }
}
