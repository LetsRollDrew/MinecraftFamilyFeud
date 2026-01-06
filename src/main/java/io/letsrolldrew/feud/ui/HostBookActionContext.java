package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.fastmoney.FastMoneyService;
import io.letsrolldrew.feud.survey.SurveyRepository;

// shared services passed to action handlers so clicks can do work without reaching back into
// plugin singletons, purely focused on the dependencies actions actually need to their job

public record HostBookActionContext(
        HostBookAnchorStore hostBookAnchorStore,
        DisplayBoardSelectionStore selectionStore,
        FastMoneyService fastMoneyService,
        SurveyRepository surveyRepository,
        FastMoneyHoverResolver fastMoneyHoverResolver,
        HostBookUiBuilder hostBookUiBuilder) {}
