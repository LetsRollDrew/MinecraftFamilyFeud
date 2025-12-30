package io.letsrolldrew.feud.util;

// Small utility helpers used when parsing config/survey yaml files
public final class Validation {
    private Validation() {}

    // Check if a string is present, trim and throws if blank/null.

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " must be provided");
        }
        return value.trim();
    }

    public static int requirePositive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    public static int requireInRange(int value, int minInclusive, int maxInclusive, String field) {
        if (value < minInclusive || value > maxInclusive) {
            throw new IllegalArgumentException(
                    field + " must be between " + minInclusive + " and " + maxInclusive + " (got " + value + ")");
        }
        return value;
    }
}
