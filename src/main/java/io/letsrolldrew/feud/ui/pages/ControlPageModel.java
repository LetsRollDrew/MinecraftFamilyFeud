package io.letsrolldrew.feud.ui.pages;

import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.survey.Survey;
import java.util.List;
import java.util.Set;

public record ControlPageModel(
        List<String> hovers,
        Survey activeSurvey,
        Set<Integer> revealedSlots,
        int strikeCount,
        int maxStrikes,
        int roundPoints,
        TeamControl controllingTeam) {}
