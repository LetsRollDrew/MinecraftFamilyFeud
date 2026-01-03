package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.effects.anim.ScheduledTask;
import io.letsrolldrew.feud.effects.anim.Scheduler;
import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.LongSupplier;
import org.bukkit.Location;
import org.bukkit.entity.Player;

// Manages buzzer bindings, lock window, and per-location cooldown
// all timing is driven by injecting clock + scheduler to test

public final class BuzzerService {
    private final Scheduler scheduler;
    private final LongSupplier clockMillis;
    private final TeamService teamService;
    private final long lockMillis;
    private final long cooldownMillis;

    private final Map<UUID, TeamId> pendingBind = new HashMap<>();
    private final Map<BlockRef, Long> lastPressMillis = new HashMap<>();

    private TeamId lockedWinner;
    private long lockExpiresAtMillis;
    private ScheduledTask unlockTask;

    public BuzzerService(
            Scheduler scheduler,
            LongSupplier clockMillis,
            TeamService teamService,
            long lockMillis,
            long cooldownMillis) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.clockMillis = Objects.requireNonNull(clockMillis, "clockMillis");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.lockMillis = Math.max(0L, lockMillis);
        this.cooldownMillis = Math.max(0L, cooldownMillis);
    }

    public void beginBind(Player player, TeamId team) {
        if (player == null || team == null) {
            return;
        }
        pendingBind.put(player.getUniqueId(), team);
    }

    public boolean isBinding(Player player) {
        if (player == null) {
            return false;
        }
        return pendingBind.containsKey(player.getUniqueId());
    }

    public void bindTo(Player player, Location loc) {
        if (player == null || loc == null || loc.getWorld() == null) {
            return;
        }
        TeamId team = pendingBind.remove(player.getUniqueId());
        if (team == null) {
            return;
        }
        BlockRef ref = toBlockRef(loc);
        teamService.setBuzzer(team, ref);
    }

    public void clearBind(TeamId team) {
        if (team == null) {
            return;
        }
        teamService.clearBuzzer(team);
    }

    public Optional<TeamId> tryBuzz(Player presser, Location loc) {
        if (loc == null) {
            return Optional.empty();
        }
        return tryBuzz(toBlockRef(loc));
    }

    public Optional<TeamId> tryBuzz(BlockRef ref) {
        if (ref == null) {
            return Optional.empty();
        }

        long now = clockMillis.getAsLong();
        if (lockedWinner != null && now < lockExpiresAtMillis) {
            return Optional.empty();
        }
        if (lockedWinner != null && now >= lockExpiresAtMillis) {
            clearLock();
        }

        Long last = lastPressMillis.get(ref);
        if (last != null && (now - last) < cooldownMillis) {
            return Optional.empty();
        }

        TeamId winner = findTeamByRef(ref);
        if (winner == null) {
            lastPressMillis.put(ref, now);
            return Optional.empty();
        }

        lockedWinner = winner;
        lockExpiresAtMillis = now + lockMillis;
        scheduleUnlock();
        lastPressMillis.put(ref, now);
        return Optional.of(winner);
    }

    public void resetLock() {
        clearLock();
    }

    private TeamId findTeamByRef(BlockRef ref) {
        for (TeamId team : TeamId.values()) {
            BlockRef bound = teamService.getBuzzer(team);
            if (ref.equals(bound)) {
                return team;
            }
        }
        return null;
    }

    private void scheduleUnlock() {
        if (unlockTask != null) {
            unlockTask.cancel();
        }
        long ticks = Math.max(1L, lockMillis / 50L);
        unlockTask = scheduler.runTaskLater(this::clearLock, ticks);
    }

    private void clearLock() {
        lockedWinner = null;
        lockExpiresAtMillis = 0L;
        if (unlockTask != null) {
            unlockTask.cancel();
            unlockTask = null;
        }
    }

    private static BlockRef toBlockRef(Location loc) {
        return new BlockRef(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
