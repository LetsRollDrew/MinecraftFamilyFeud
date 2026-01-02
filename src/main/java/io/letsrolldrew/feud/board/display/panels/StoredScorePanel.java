package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.team.TeamId;

public record StoredScorePanel(String id, TeamId team, DynamicBoardLayout layout) {}
