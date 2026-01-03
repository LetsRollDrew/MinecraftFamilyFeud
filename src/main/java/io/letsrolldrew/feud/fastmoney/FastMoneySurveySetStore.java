package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.util.Validation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class FastMoneySurveySetStore {
    private final Map<String, FastMoneySurveySet> setsById;

    private FastMoneySurveySetStore(Map<String, FastMoneySurveySet> setsById) {
        this.setsById = setsById;
    }

    public static FastMoneySurveySetStore load(FileConfiguration config, SurveyRepository surveyRepository) {
        Objects.requireNonNull(config, "config");
        Objects.requireNonNull(surveyRepository, "surveyRepository");

        ConfigurationSection fastMoneySection = config.getConfigurationSection("fastMoney");
        if (fastMoneySection == null) {
            throw new IllegalArgumentException("fastMoney section is missing");
        }

        ConfigurationSection packsSection = fastMoneySection.getConfigurationSection("packs");
        if (packsSection == null) {
            throw new IllegalArgumentException("fastMoney.packs section is missing");
        }

        Map<String, FastMoneySurveySet> loaded = new LinkedHashMap<>();
        for (String packId : packsSection.getKeys(false)) {
            ConfigurationSection packSection = packsSection.getConfigurationSection(packId);

            if (packSection == null) {
                throw new IllegalArgumentException("Pack section missing for id: " + packId);
            }

            FastMoneySurveySet set = parseSet(packId, packSection, surveyRepository);
            if (loaded.containsKey(set.id())) {
                throw new IllegalArgumentException("Duplicate FastMoney pack id: " + set.id());
            }

            loaded.put(set.id(), set);
        }

        if (loaded.isEmpty()) {
            throw new IllegalArgumentException("fastMoney.packs must define at least one pack");
        }

        return new FastMoneySurveySetStore(Map.copyOf(loaded));
    }

    public Optional<FastMoneySurveySet> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(setsById.get(id));
    }

    private static FastMoneySurveySet parseSet(
            String id, ConfigurationSection packSection, SurveyRepository surveyRepository) {
        String packId = Validation.requireNonBlank(id, "pack id");
        int targetScore = requirePositiveInt(packSection, "targetScore");
        int player1Seconds = requirePositiveInt(packSection, "player1Seconds");
        int player2Seconds = requirePositiveInt(packSection, "player2Seconds");

        List<String> surveys = packSection.getStringList("surveys");
        if (surveys == null || surveys.size() != 5) {
            throw new IllegalArgumentException("Pack " + packId + " must define exactly 5 survey ids");
        }

        for (String surveyId : surveys) {
            String validatedSurveyId = Validation.requireNonBlank(surveyId, "surveyId");
            if (surveyRepository.findById(validatedSurveyId).isEmpty()) {
                throw new IllegalArgumentException(
                        "Pack " + packId + " references missing survey id: " + validatedSurveyId);
            }
        }

        return new FastMoneySurveySet(packId, targetScore, player1Seconds, player2Seconds, surveys);
    }

    private static int requirePositiveInt(ConfigurationSection section, String path) {
        if (!section.isInt(path)) {
            throw new IllegalArgumentException("Missing or invalid integer: " + path);
        }

        int value = section.getInt(path);
        return Validation.requirePositive(value, path);
    }
}
