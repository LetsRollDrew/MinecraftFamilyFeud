package io.letsrolldrew.feud.effects.anim;

import io.letsrolldrew.feud.display.DisplayKey;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnimationService {
    private final Plugin plugin;
    private final Map<DisplayKey, List<BukkitTask>> tasksByKey = new HashMap<>();

    public AnimationService(Plugin plugin) {
        this.plugin = plugin;
    }

    public void schedule(DisplayKey key, List<AnimationStep> steps) {
        if (key == null || steps == null || steps.isEmpty()) {
            return;
        }
        cancel(key);

        List<BukkitTask> tasks = new ArrayList<>(steps.size());
        long delay = 0L;
        for (AnimationStep step : steps) {
            if (step == null || step.action() == null) {
                continue;
            }
            if (step.ticksDelay() < 0) {
                continue;
            }
            delay += step.ticksDelay();
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, step.action(), delay);
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
        List<BukkitTask> tasks = tasksByKey.remove(key);
        if (tasks == null) {
            return;
        }
        for (BukkitTask task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void cancelAll() {
        for (List<BukkitTask> tasks : tasksByKey.values()) {
            for (BukkitTask task : tasks) {
                if (task != null) {
                    task.cancel();
                }
            }
        }
        tasksByKey.clear();
    }
}
