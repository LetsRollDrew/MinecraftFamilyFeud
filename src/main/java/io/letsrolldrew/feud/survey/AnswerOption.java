package io.letsrolldrew.feud.survey;

import io.letsrolldrew.feud.util.Validation;
import java.util.Collections;
import java.util.List;

public final class AnswerOption {
    private final String text;
    private final int points;
    private final List<String> aliases;

    public AnswerOption(String text, int points, List<String> aliases) {
        this.text = Validation.requireNonBlank(text, "answer.text");
        this.points = Validation.requirePositive(points, "answer.points");
        this.aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }

    public String text() {
        return text;
    }

    public int points() {
        return points;
    }

    public List<String> aliases() {
        return Collections.unmodifiableList(aliases);
    }
}
