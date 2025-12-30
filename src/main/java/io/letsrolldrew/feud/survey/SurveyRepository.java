package io.letsrolldrew.feud.survey;

import io.letsrolldrew.feud.util.Validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class SurveyRepository {
    private final Map<String, Survey> surveysById;

    private SurveyRepository(Map<String, Survey> surveysById) {
        this.surveysById = surveysById;
    }

    public static SurveyRepository load(FileConfiguration config) {
        ConfigurationSection surveysSection = config.getConfigurationSection("surveys");
        if (surveysSection == null) {
            return new SurveyRepository(Collections.emptyMap());
        }

        Map<String, Survey> loaded = new LinkedHashMap<>();
        for (String id : surveysSection.getKeys(false)) {
            Survey survey = parseSurvey(id, surveysSection.getConfigurationSection(id));
            if (loaded.containsKey(survey.id())) {
                throw new IllegalArgumentException("Duplicate survey id: " + survey.id());
            }
            loaded.put(survey.id(), survey);
        }
        return new SurveyRepository(Collections.unmodifiableMap(loaded));
    }

    private static Survey parseSurvey(String id, ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("Survey section missing for id: " + id);
        }
        String surveyId = Validation.requireNonBlank(id, "survey.id");
        String question = Validation.requireNonBlank(section.getString("question"), "survey.question");
        String displayName = section.getString("display");

        List<Map<?, ?>> answersRaw = section.getMapList("answers");
        if (answersRaw == null || answersRaw.isEmpty()) {
            throw new IllegalArgumentException("Survey " + id + " must have answers");
        }
        if (answersRaw.size() > 8) {
            throw new IllegalArgumentException("Survey " + id + " has more than 8 answers");
        }

        List<AnswerOption> answers = new ArrayList<>();
        Set<String> answerTexts = new HashSet<>();
        Set<String> aliasSet = new HashSet<>();

        for (Map<?, ?> raw : answersRaw) {
            String text = Validation.requireNonBlank(asString(raw.get("text")), "answer.text");
            int points = Validation.requirePositive(asInt(raw.get("points")), "answer.points");
            List<String> aliases = raw.get("aliases") instanceof List<?> list
                    ? list.stream().map(Object::toString).toList()
                    : List.of();

            if (!answerTexts.add(text.toLowerCase())) {
                throw new IllegalArgumentException("Duplicate answer text in survey " + id + ": " + text);
            }
            for (String alias : aliases) {
                String normalized = alias.toLowerCase();
                if (!aliasSet.add(normalized)) {
                    throw new IllegalArgumentException("Duplicate alias in survey " + id + ": " + alias);
                }
            }

            answers.add(new AnswerOption(text, points, aliases));
        }

        return new Survey(surveyId, displayName, question, answers);
    }

    private static String asString(Object obj) {
        return obj == null ? null : obj.toString();
    }

    private static int asInt(Object obj) {
        if (obj instanceof Number num) {
            return num.intValue();
        }
        if (obj instanceof String str) {
            return Integer.parseInt(str);
        }
        throw new IllegalArgumentException("Expected number but got: " + obj);
    }

    public Optional<Survey> findById(String id) {
        return Optional.ofNullable(surveysById.get(id));
    }

    public List<Survey> listAll() {
        return List.copyOf(surveysById.values());
    }
}
