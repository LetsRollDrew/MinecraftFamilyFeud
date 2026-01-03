package io.letsrolldrew.feud.effects.buzz;

import static org.junit.jupiter.api.Assertions.*;

import io.letsrolldrew.feud.effects.anim.ScheduledTask;
import io.letsrolldrew.feud.effects.anim.Scheduler;
import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class BuzzerServiceTest {

    private FakeScheduler scheduler;
    private AtomicLong clock;
    private TeamService teamService;
    private BuzzerService buzzerService;
    private World world;

    @BeforeEach
    void setUp() {
        scheduler = new FakeScheduler();
        clock = new AtomicLong(0);
        teamService = new TeamService();
        buzzerService = new BuzzerService(scheduler, clock::get, teamService, 12_000, 1_000);
        world = Mockito.mock(World.class);
        Mockito.when(world.getUID()).thenReturn(UUID.randomUUID());
    }

    @Test
    void bindsAndBuzzesFirstTeam() {
        Location loc = new Location(world, 10.2, 64.0, -5.7);

        var player = mockPlayer("p1");
        buzzerService.beginBind(player, TeamId.RED);
        buzzerService.bindTo(player, loc);

        Optional<TeamId> result = buzzerService.tryBuzz(locToBlockRef(loc));

        assertEquals(Optional.of(TeamId.RED), result);
        assertTrue(isLocked());
    }

    @Test
    void lockBlocksSubsequentBuzzesUntilExpiry() {
        Location loc = new Location(world, 1, 65, 2);
        var player = mockPlayer("p1");
        buzzerService.beginBind(player, TeamId.BLUE);
        buzzerService.bindTo(player, loc);

        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
        // within lock
        clock.addAndGet(1000);
        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isEmpty());
        // advance past lock duration
        clock.addAndGet(12_000);
        scheduler.runAll();
        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
    }

    @Test
    void cooldownBlocksRapidPresses() {
        Location loc = new Location(world, 3, 70, 3);
        var player = mockPlayer("p1");
        buzzerService.beginBind(player, TeamId.RED);
        buzzerService.bindTo(player, loc);

        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
        buzzerService.resetLock(); // clear lock so we can evaluate cooldown alone
        clock.addAndGet(500); // half cooldown
        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isEmpty());
        clock.addAndGet(600); // beyond cooldown
        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
    }

    @Test
    void resetLockClearsWinner() {
        Location loc = new Location(world, 4, 71, 4);
        var player = mockPlayer("p1");
        buzzerService.beginBind(player, TeamId.RED);
        buzzerService.bindTo(player, loc);

        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
        buzzerService.resetLock();
        clock.addAndGet(1_100); // pass cooldown
        assertTrue(buzzerService.tryBuzz(locToBlockRef(loc)).isPresent());
    }

    private static org.bukkit.entity.Player mockPlayer(String name) {
        org.bukkit.entity.Player player = Mockito.mock(org.bukkit.entity.Player.class);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(player.getName()).thenReturn(name);
        Mockito.when(player.hasPermission(Mockito.anyString())).thenReturn(true);
        return player;
    }

    private static BlockRef locToBlockRef(Location loc) {
        return new BlockRef(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private boolean isLocked() {
        // internal lock state is implicit
        // so we can assert by retrying immediately
        Location loc = new Location(world, 10, 64, -6);
        var player = mockPlayer("p2");
        buzzerService.beginBind(player, TeamId.BLUE);
        buzzerService.bindTo(player, loc);
        return buzzerService.tryBuzz(locToBlockRef(loc)).isEmpty();
    }

    private static final class FakeScheduler implements Scheduler {
        private final Deque<Scheduled> queue = new ArrayDeque<>();

        @Override
        public ScheduledTask runTaskLater(Runnable action, long delayTicks) {
            Scheduled scheduled = new Scheduled(action);
            queue.addLast(scheduled);
            return scheduled;
        }

        void runAll() {
            while (!queue.isEmpty()) {
                Scheduled scheduled = queue.removeFirst();
                if (scheduled.canceled) {
                    continue;
                }
                scheduled.action.run();
            }
        }

        private static final class Scheduled implements ScheduledTask {
            private final Runnable action;
            private boolean canceled;

            Scheduled(Runnable action) {
                this.action = action;
            }

            @Override
            public void cancel() {
                canceled = true;
            }
        }
    }
}
