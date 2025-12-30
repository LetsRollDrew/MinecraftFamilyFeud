package io.letsrolldrew.feud.effects.anim;

import io.letsrolldrew.feud.display.DisplayKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationService {
    private final Scheduler scheduler;
    private final Map<DisplayKey, List<ScheduledTask>> tasksByKey = new HashMap<>();

    public AnimationService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void schedule(DisplayKey key, List<AnimationStep> steps) {
        if (key == null || steps == null || steps.isEmpty()) {
            return;
        }
        cancel(key);

        List<ScheduledTask> tasks = new ArrayList<>(steps.size());
        long delay = 0L;
        for (AnimationStep step : steps) {
            if (step == null || step.action() == null) {
                continue;
            }
            if (step.ticksDelay() < 0) {
                continue;
            }
            delay += step.ticksDelay();
            ScheduledTask task = scheduler.runTaskLater(step.action(), delay);
            tasks.add(task);
        }
        if (!tasks.isEmpty()) {
            tasksByKey.put(key, tasks);
        }
    }

    public void cancel(DisplayKey key) {
        if (key == null) {
            return;
        }
        List<ScheduledTask> tasks = tasksByKey.remove(key);
        if (tasks == null) {
            return;
        }
        for (ScheduledTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void cancelAll() {
        for (List<ScheduledTask> tasks : tasksByKey.values()) {
            for (ScheduledTask task : tasks) {
                if (task != null) {
                    task.cancel();
                }
            }
        }
        tasksByKey.clear();
    }
}
