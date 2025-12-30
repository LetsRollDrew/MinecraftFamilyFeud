package io.letsrolldrew.feud.ui;

import net.kyori.adventure.text.Component;

// Centralizing text/layout helpers for the host book.

public final class BookTextFormatter {
    public static final char NBSP = '\u00A0';

    // Keep this or the client will wrap
    public static final Component COL_GAP = Component.text(" ");

    private static final int REVEALED_TOKEN_MAX = 5;

    private BookTextFormatter() {}

    public static String unrevealedLabel(int slot) {
        return "Reveal" + NBSP + slot;
    }

    // Compact revealed button label: TOKEN + points
    public static String formatRevealedLabel(String answer, int points) {
        String token = pickToken(answer);
        if (token.length() > REVEALED_TOKEN_MAX) {
            token = token.substring(0, REVEALED_TOKEN_MAX);
        }

        String pts = formatPoints2(points);
        return token + NBSP + pts;
    }

    public static String abbreviate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        text = text.trim().replaceAll("\\s+", " ");
        if (text.length() <= maxLen) {
            return text;
        }
        if (maxLen <= 1) {
            return text.substring(0, 1);
        }
        return text.substring(0, Math.max(0, maxLen - 1)) + "…";
    }

    public static String toNoBreak(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(' ', NBSP);
    }

    public static String strikeLine(int strikeCount, int maxStrikes) {
        if (maxStrikes <= 0) {
            maxStrikes = 3;
        }
        if (strikeCount < 0) {
            strikeCount = 0;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxStrikes; i++) {
            if (i < strikeCount) {
                sb.append("X ");
            } else {
                sb.append("_ ");
            }
        }
        return sb.toString().trim();
    }

    private static String formatPoints2(int points) {
        if (points < 0) {
            points = 0;
        }
        if (points < 10) {
            return String.valueOf(NBSP) + points;
        }
        return String.valueOf(points);
    }

    private static String pickToken(String answer) {
        if (answer == null) {
            return "unknown";
        }

        String normalized = answer.trim().replaceAll("\\s+", " ").toUpperCase();
        if (normalized.isEmpty()) {
            return "unknown";
        }

        String[] parts = normalized.split(" ");
        for (String p : parts) {
            if (p.isBlank()) {
                continue;
            }
            return scrubToken(p);
        }

        return scrubToken(parts[0]);
    }

    private static String scrubToken(String token) {
        String cleaned = token.replaceAll("[^A-Z0-9]", "");
        return cleaned.isEmpty() ? "unknown" : cleaned;
    }
}
