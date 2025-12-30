package io.letsrolldrew.feud.effects.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.display.DisplayKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

final class AnimationServiceTest {

    @Test
    void schedulesInOrderAndCancelsExisting() {
        FakeScheduler scheduler = new FakeScheduler();
        AnimationService service = new AnimationService(scheduler);
        DisplayKey key = new DisplayKey("ns", "g", "id", "part");

        AtomicBoolean firstCalled = new AtomicBoolean(false);
        AtomicBoolean secondCalled = new AtomicBoolean(false);
        List<AnimationStep> steps = List.of(
                new AnimationStep(1, () -> firstCalled.set(true)), new AnimationStep(2, () -> secondCalled.set(true)));

        service.schedule(key, steps);

        // replace with another schedule to ensure prior tasks cancelled
        AtomicBoolean newCalled = new AtomicBoolean(false);
        service.schedule(key, List.of(new AnimationStep(0, () -> newCalled.set(true))));

        // latest schedule should be at delay 0, earlier tasks marked cancelled
        FakeScheduler.Task lastTask = scheduler.tasks.get(scheduler.tasks.size() - 1);
        assertEquals(0, lastTask.delay);
        lastTask.run();
        assertTrue(newCalled.get());
        assertFalse(firstCalled.get());
        assertFalse(secondCalled.get());
        assertTrue(scheduler.cancelled);
    }

    @Test
    void cancelAllStopsAllTasks() {
        FakeScheduler scheduler = new FakeScheduler();
        AnimationService service = new AnimationService(scheduler);
        DisplayKey key1 = new DisplayKey("ns", "g", "id1", "part");
        DisplayKey key2 = new DisplayKey("ns", "g", "id2", "part");

        service.schedule(key1, List.of(new AnimationStep(1, () -> {})));
        service.schedule(key2, List.of(new AnimationStep(1, () -> {})));

        service.cancelAll();

        assertTrue(scheduler.cancelled);
    }

    private static final class FakeScheduler implements Scheduler {
        final List<Task> tasks = new ArrayList<>();
        boolean cancelled = false;

        @Override
        public ScheduledTask runTaskLater(Runnable action, long delayTicks) {
            Task task = new Task(action, delayTicks);
            tasks.add(task);
            return () -> cancelled = true;
        }

        final class Task {
            final Runnable action;
            final long delay;

            Task(Runnable action, long delay) {
                this.action = action;
                this.delay = delay;
            }

            void run() {
                action.run();
            }
        }
    }
}
