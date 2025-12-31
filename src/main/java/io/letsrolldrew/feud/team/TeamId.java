package io.letsrolldrew.feud.team;

public enum TeamId {
    RED,
    BLUE;

    public static TeamId fromString(String value) {
        if (value == null) {
            return null;
        }
        return switch (value.toLowerCase()) {
            case "red" -> RED;
            case "blue" -> BLUE;
            default -> null;
        };
    }
}

