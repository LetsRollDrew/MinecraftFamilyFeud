package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.fastmoney.FastMoneyService;
import io.letsrolldrew.feud.survey.SurveyRepository;

public record HostBookActionContext(
        HostBookAnchorStore hostBookAnchorStore,
        DisplayBoardSelectionStore selectionStore,
        FastMoneyService fastMoneyService,
        SurveyRepository surveyRepository,
        FastMoneyHoverResolver fastMoneyHoverResolver,
        HostBookUiBuilder hostBookUiBuilder) {}
