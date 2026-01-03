package io.letsrolldrew.feud.team;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public final class TeamService {
    private static final TeamState DEFAULT_RED = new TeamState("Red", 0, null);
    private static final TeamState DEFAULT_BLUE = new TeamState("Blue", 0, null);

    private final Map<TeamId, TeamState> states = new EnumMap<>(TeamId.class);

    public TeamService() {
        reset();
    }

    public TeamState get(TeamId team) {
        if (team == null) {
            return null;
        }

        return states.get(team);
    }

    public String getName(TeamId team) {
        TeamState state = get(team);

        return state == null ? null : state.displayName();
    }

    public boolean setName(TeamId team, String newName) {
        if (team == null || newName == null) {
            return false;
        }

        String trimmed = newName.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        TeamState current = get(team);
        if (current == null) {
            return false;
        }
        if (trimmed.equals(current.displayName())) {
            return false;
        }

        states.put(team, new TeamState(trimmed, current.score(), current.buzzer()));
        return true;
    }

    public int getScore(TeamId team) {
        TeamState state = get(team);
        return state == null ? 0 : state.score();
    }

    public boolean addScore(TeamId team, int delta) {
        if (team == null || delta == 0) {
            return false;
        }

        TeamState current = get(team);
        if (current == null) {
            return false;
        }

        int next = clampNonNegative((long) current.score() + delta);
        if (next == current.score()) {
            return false;
        }

        states.put(team, new TeamState(current.displayName(), next, current.buzzer()));
        return true;
    }

    public BlockRef getBuzzer(TeamId team) {
        TeamState state = get(team);
        return state == null ? null : state.buzzer();
    }

    public boolean setBuzzer(TeamId team, BlockRef buzzer) {
        if (team == null || buzzer == null) {
            return false;
        }

        TeamState current = get(team);
        if (current == null) {
            return false;
        }
        if (Objects.equals(buzzer, current.buzzer())) {
            return false;
        }

        states.put(team, new TeamState(current.displayName(), current.score(), buzzer));
        return true;
    }

    public boolean clearBuzzer(TeamId team) {
        if (team == null) {
            return false;
        }

        TeamState current = get(team);
        if (current == null || current.buzzer() == null) {
            return false;
        }

        states.put(team, new TeamState(current.displayName(), current.score(), null));
        return true;
    }

    public void reset() {
        states.put(TeamId.RED, DEFAULT_RED);
        states.put(TeamId.BLUE, DEFAULT_BLUE);
    }

    private static int clampNonNegative(long value) {
        if (value <= 0L) {
            return 0;
        }
        if (value >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) value;
    }
}
