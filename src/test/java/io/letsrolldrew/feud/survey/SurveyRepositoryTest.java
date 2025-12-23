package io.letsrolldrew.feud.survey;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyRepositoryTest {

    @Test
    void loadsValidSurvey() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              sample:
                question: "Name a fruit"
                answers:
                  - text: "Apple"
                    points: 40
                    aliases: ["Apples"]
                  - text: "Banana"
                    points: 30
            """);

        SurveyRepository repo = SurveyRepository.load(config);
        Optional<Survey> survey = repo.findById("sample");
        assertTrue(survey.isPresent(), "survey should load");
        assertEquals("Name a fruit", survey.get().question());
        assertEquals(2, survey.get().answers().size());
        assertEquals("Apple", survey.get().answers().get(0).text());
        assertEquals(40, survey.get().answers().get(0).points());
        assertEquals("Banana", survey.get().answers().get(1).text());
    }

    @Test
    void rejectsDuplicateAnswerTexts() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              dup:
                question: "Q"
                answers:
                  - text: "Same"
                    points: 10
                  - text: "Same"
                    points: 5
            """);

        assertThrows(IllegalArgumentException.class, () -> SurveyRepository.load(config));
    }

    @Test
    void rejectsDuplicateAliases() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              dupalias:
                question: "Q"
                answers:
                  - text: "One"
                    points: 10
                    aliases: ["Tie"]
                  - text: "Two"
                    points: 5
                    aliases: ["Tie"]
            """);

        assertThrows(IllegalArgumentException.class, () -> SurveyRepository.load(config));
    }

    @Test
    void rejectsTooManyAnswers() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              too_many:
                question: "Q"
                answers:
                  - text: "A1"
                    points: 1
                  - text: "A2"
                    points: 1
                  - text: "A3"
                    points: 1
                  - text: "A4"
                    points: 1
                  - text: "A5"
                    points: 1
                  - text: "A6"
                    points: 1
                  - text: "A7"
                    points: 1
                  - text: "A8"
                    points: 1
                  - text: "A9"
                    points: 1
            """);

        assertThrows(IllegalArgumentException.class, () -> SurveyRepository.load(config));
    }

    @Test
    void rejectsMissingQuestion() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              missing_question:
                answers:
                  - text: "A1"
                    points: 1
            """);

        assertThrows(IllegalArgumentException.class, () -> SurveyRepository.load(config));
    }
}
