package io.letsrolldrew.feud.effects.timer;

import io.letsrolldrew.feud.effects.anim.ScheduledTask;
import io.letsrolldrew.feud.effects.anim.Scheduler;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

// countdown timer, (in seconds), with injected scheduler/clock for testability.
// runs ticks on the main thread through Scheduler

public final class TimerService {
    private final Scheduler scheduler;
    private final LongSupplier clockMillis;
    private final int defaultSeconds;

    private Consumer<Integer> onTick;
    private Consumer<TimerStatus> onStatus;
    private ScheduledTask pending;
    private int remainingSeconds;
    private boolean running;

    public TimerService(Scheduler scheduler, LongSupplier clockMillis, int defaultSeconds) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.clockMillis = Objects.requireNonNull(clockMillis, "clockMillis");
        this.defaultSeconds = Math.max(0, defaultSeconds);
        this.remainingSeconds = this.defaultSeconds;
    }

    public void start() {
        start(defaultSeconds);
    }

    public void start(int seconds) {
        int normalized = Math.max(0, seconds);

        stopScheduledTick();
        remainingSeconds = normalized;
        running = remainingSeconds > 0;
        notifyObservers();

        if (running) {
            scheduleNextTick();
        }
    }

    public void stop() {
        running = false;
        stopScheduledTick();
        notifyObservers();
    }

    public void reset() {
        reset(defaultSeconds);
    }

    public void reset(int seconds) {
        int normalized = Math.max(0, seconds);

        stopScheduledTick();
        running = false;
        remainingSeconds = normalized;
        notifyObservers();
    }

    public TimerStatus status() {
        return new TimerStatus(running, remainingSeconds);
    }

    public void setOnTick(Consumer<Integer> onTick) {
        this.onTick = onTick;
    }

    public void setOnStatus(Consumer<TimerStatus> onStatus) {
        this.onStatus = onStatus;
    }

    private void scheduleNextTick() {
        stopScheduledTick();
        pending = scheduler.runTaskLater(this::handleTick, 20L);
    }

    private void handleTick() {
        if (!running) {
            return;
        }
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        if (remainingSeconds == 0) {
            running = false;
        }
        notifyObservers();

        if (running) {
            scheduleNextTick();
        }
    }

    private void notifyObservers() {
        if (onTick != null) {
            onTick.accept(remainingSeconds);
        }
        if (onStatus != null) {
            onStatus.accept(new TimerStatus(running, remainingSeconds));
        }
    }

    private void stopScheduledTick() {
        if (pending != null) {
            pending.cancel();
            pending = null;
        }
    }

    public record TimerStatus(boolean running, int remainingSeconds) {}
}
