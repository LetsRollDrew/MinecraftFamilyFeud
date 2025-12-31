package io.letsrolldrew.feud.effects.timer;

import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class TimerCommands {
    private final TimerService timerService;
    private final String hostPermission;
    private final String adminPermission;

    public TimerCommands(TimerService timerService, String hostPermission, String adminPermission) {
        this.timerService = Objects.requireNonNull(timerService, "timerService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            sender.sendMessage("You must be the host to do that");
            return true;
        }
        if (args == null || args.length == 0) {
            return help(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender);
            case "reset" -> handleReset(sender, args);
            case "status" -> handleStatus(sender);
            default -> help(sender);
        }
        return true;
    }

    private void handleStart(CommandSender sender, String[] args) {
        Integer seconds = parseSeconds(args, 1);
        if (seconds != null && seconds < 0) {
            sender.sendMessage("Seconds can't be negative");
            return;
        }
        if (seconds == null) {
            timerService.start();
            sender.sendMessage("Timer started.");
        } else {
            timerService.start(seconds);
            sender.sendMessage("Timer started for " + seconds + "s.");
        }
    }

    private void handleStop(CommandSender sender) {
        timerService.stop();
        sender.sendMessage("Timer stopped.");
    }

    private void handleReset(CommandSender sender, String[] args) {
        Integer seconds = parseSeconds(args, 1);
        if (seconds != null && seconds < 0) {
            sender.sendMessage("Seconds can't be negative");
            return;
        }
        if (seconds == null) {
            timerService.reset();
            sender.sendMessage("Timer reset to default");
        } else {
            timerService.reset(seconds);
            sender.sendMessage("Timer reset to " + seconds + "s");
        }
    }

    private void handleStatus(CommandSender sender) {
        TimerService.TimerStatus status = timerService.status();
        String running = status.running() ? "running" : "stopped";
        sender.sendMessage("Timer " + running + " (" + status.remainingSeconds() + "s remaining)");
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage("Timer commands:");
        sender.sendMessage("/feud timer start [seconds]");
        sender.sendMessage("/feud timer stop");
        sender.sendMessage("/feud timer reset [seconds]");
        sender.sendMessage("/feud timer status");
        return true;
    }

    private boolean isAuthorized(CommandSender sender) {
        if (sender == null) {
            return false;
        }
        if (sender.hasPermission(hostPermission)) {
            return true;
        }
        return sender.hasPermission(adminPermission);
    }

    private static Integer parseSeconds(String[] args, int index) {
        if (args == null || index >= args.length) {
            return null;
        }
        try {
            return Integer.parseInt(args[index]);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
