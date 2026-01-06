package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.survey.SurveyRepository;

public record HostBookContext(
        BookButtonFactory buttons, FastMoneyHoverResolver fastMoneyHoverResolver, SurveyRepository surveyRepository) {}
