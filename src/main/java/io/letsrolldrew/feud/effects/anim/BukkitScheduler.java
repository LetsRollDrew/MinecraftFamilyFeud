package io.letsrolldrew.feud.effects.anim;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BukkitScheduler implements Scheduler {
    private final Plugin plugin;

    public BukkitScheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask runTaskLater(Runnable action, long delayTicks) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, action, delayTicks);
        return task::cancel;
    }
}
