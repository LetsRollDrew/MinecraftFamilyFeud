package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.fastmoney.FastMoneyQuestionState;
import io.letsrolldrew.feud.fastmoney.FastMoneyService;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import java.util.Objects;

public final class FastMoneyHoverResolver {
    private final FastMoneyService fastMoneyService;
    private final SurveyRepository surveyRepository;

    public FastMoneyHoverResolver(FastMoneyService fastMoneyService, SurveyRepository surveyRepository) {
        this.fastMoneyService = Objects.requireNonNull(fastMoneyService, "fastMoneyService");
        this.surveyRepository = Objects.requireNonNull(surveyRepository, "surveyRepository");
    }

    public String hoverFor(int questionIndex, int slot) {
        String fallback = "Answer for slot " + slot;

        var state = fastMoneyService.state();
        if (state.questions() == null
                || questionIndex < 1
                || questionIndex > state.questions().size()) {
            return fallback;
        }

        FastMoneyQuestionState question = state.questions().get(questionIndex - 1);
        var surveyOpt = surveyRepository.findById(question.surveyId());
        if (surveyOpt.isEmpty()) {
            return fallback;
        }

        Survey survey = surveyOpt.get();
        if (slot < 1 || slot > survey.answers().size()) {
            return fallback;
        }

        AnswerOption ans = survey.answers().get(slot - 1);
        return ans.text() + " (" + ans.points() + ")";
    }
}
