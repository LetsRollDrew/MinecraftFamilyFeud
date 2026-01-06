package io.letsrolldrew.feud.ui.pages;

import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.survey.Survey;
import java.util.List;
import java.util.Set;

// bundled these values so page builders stay dumb, i.e. they only read this shape and decide
// which buttons to show without needing to refetch a state

public record ControlPageModel(
        List<String> hovers,
        Survey activeSurvey,
        Set<Integer> revealedSlots,
        int strikeCount,
        int maxStrikes,
        int roundPoints,
        TeamControl controllingTeam) {}
