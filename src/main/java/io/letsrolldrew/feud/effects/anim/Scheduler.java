package io.letsrolldrew.feud.effects.anim;

public interface Scheduler {
    ScheduledTask runTaskLater(Runnable action, long delayTicks);
}
