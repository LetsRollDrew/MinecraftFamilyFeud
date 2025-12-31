package io.letsrolldrew.feud.effects.timer;

import static org.junit.jupiter.api.Assertions.*;

import io.letsrolldrew.feud.effects.anim.ScheduledTask;
import io.letsrolldrew.feud.effects.anim.Scheduler;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class TimerServiceTest {
    private FakeScheduler scheduler;
    private List<Integer> ticks;
    private TimerService timerService;

    @BeforeEach
    void setUp() {
        scheduler = new FakeScheduler();
        ticks = new ArrayList<>();
        LongSupplier clock = () -> 0L;
        timerService = new TimerService(scheduler, clock, 3);
        timerService.setOnTick(ticks::add);
    }

    @Test
    void countsDownAndStops() {
        timerService.start();
        assertTrue(timerService.status().running());
        assertEquals(3, timerService.status().remainingSeconds());

        scheduler.runNext();
        assertEquals(2, timerService.status().remainingSeconds());

        scheduler.runNext();
        assertEquals(1, timerService.status().remainingSeconds());

        scheduler.runNext();
        assertFalse(timerService.status().running());
        assertEquals(0, timerService.status().remainingSeconds());

        assertEquals(List.of(3, 2, 1, 0), ticks);
    }

    @Test
    void stopCancelsFutureTicks() {
        timerService.start(5);
        scheduler.runNext(); // 4
        timerService.stop();

        scheduler.runNext(); // should be canceled/no-op
        assertFalse(timerService.status().running());
        assertEquals(4, timerService.status().remainingSeconds());

        assertEquals(List.of(5, 4), ticks);
    }

    @Test
    void resetStopsAndSetsRemaining() {
        timerService.start(4);
        scheduler.runNext(); // 3

        timerService.reset(2);
        assertFalse(timerService.status().running());
        assertEquals(2, timerService.status().remainingSeconds());
        assertEquals(List.of(4, 3, 2), ticks);

        scheduler.runNext(); // no task should run after reset
        assertEquals(2, timerService.status().remainingSeconds());
    }

    private static final class FakeScheduler implements Scheduler {
        private final Deque<Scheduled> queue = new ArrayDeque<>();

        @Override
        public ScheduledTask runTaskLater(Runnable action, long delayTicks) {
            Scheduled scheduled = new Scheduled(action);
            queue.addLast(scheduled);
            return scheduled;
        }

        void runNext() {
            while (!queue.isEmpty()) {
                Scheduled scheduled = queue.removeFirst();
                if (scheduled.canceled) {
                    continue;
                }
                scheduled.action.run();
                break;
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
