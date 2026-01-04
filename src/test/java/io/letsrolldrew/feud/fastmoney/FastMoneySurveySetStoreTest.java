package io.letsrolldrew.feud.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.survey.SurveyRepository;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

final class FastMoneySurveySetStoreTest {

    @Test
    void loadsValidPacks() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString(validConfig());

        SurveyRepository repo = SurveyRepository.load(config);
        FastMoneySurveySetStore store = FastMoneySurveySetStore.load(config, repo);

        assertTrue(store.findById("s1").isPresent());
        FastMoneySurveySet set = store.findById("s1").orElseThrow();
        assertEquals(200, set.targetScore());
        assertEquals(20, set.player1Seconds());
        assertEquals(25, set.player2Seconds());
        assertEquals(5, set.surveyIds().size());
        assertEquals("q1", set.surveyIds().get(0));
        assertEquals("q5", set.surveyIds().get(4));
    }

    @Test
    void rejectsMissingFastMoneySection() {
        YamlConfiguration config = new YamlConfiguration();
        assertThrows(IllegalArgumentException.class, () -> FastMoneySurveySetStore.load(config, emptySurveyRepo()));
    }

    @Test
    void rejectsWrongSurveyCount() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              q1:
                question: "Q1"
                answers:
                  - text: "A1"
                    points: 1
            fastMoney:
              packs:
                s1:
                  targetScore: 200
                  player1Seconds: 20
                  player2Seconds: 25
                  surveys: ["q1", "q2", "q3", "q4"]
            """);

        SurveyRepository repo = SurveyRepository.load(config);
        assertThrows(IllegalArgumentException.class, () -> FastMoneySurveySetStore.load(config, repo));
    }

    @Test
    void rejectsMissingSurveyReference() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys:
              q1:
                question: "Q1"
                answers:
                  - text: "A1"
                    points: 1
            fastMoney:
              packs:
                s1:
                  targetScore: 200
                  player1Seconds: 20
                  player2Seconds: 25
                  surveys: ["q1", "missing", "q3", "q4", "q5"]
            """);

        SurveyRepository repo = SurveyRepository.load(config);
        assertThrows(IllegalArgumentException.class, () -> FastMoneySurveySetStore.load(config, repo));
    }

    private SurveyRepository emptySurveyRepo() throws Exception {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
            surveys: {}
            """);
        return SurveyRepository.load(config);
    }

    private String validConfig() {
        return """
            surveys:
              q1:
                question: "Q1"
                answers:
                  - text: "A1"
                    points: 1
              q2:
                question: "Q2"
                answers:
                  - text: "A2"
                    points: 1
              q3:
                question: "Q3"
                answers:
                  - text: "A3"
                    points: 1
              q4:
                question: "Q4"
                answers:
                  - text: "A4"
                    points: 1
              q5:
                question: "Q5"
                answers:
                  - text: "A5"
                    points: 1
            fastMoney:
              packs:
                s1:
                  targetScore: 200
                  player1Seconds: 20
                  player2Seconds: 25
                  surveys: ["q1", "q2", "q3", "q4", "q5"]
            """;
    }
}
