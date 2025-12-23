package io.letsrolldrew.feud.survey;

import io.letsrolldrew.feud.util.Validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Survey {
    private final String id;
    private final String question;
    private final List<AnswerOption> answers;

    public Survey(String id, String question, List<AnswerOption> answers) {
        this.id = Validation.requireNonBlank(id, "survey.id");
        this.question = Validation.requireNonBlank(question, "survey.question");
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("survey.answers must not be empty");
        }
        if (answers.size() > 8) {
            throw new IllegalArgumentException("survey.answers must not exceed 8 entries");
        }
        this.answers = List.copyOf(answers);
    }

    public String id() {
        return id;
    }

    public String question() {
        return question;
    }

    public List<AnswerOption> answers() {
        return Collections.unmodifiableList(answers);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Survey survey)) return false;
        return id.equals(survey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
