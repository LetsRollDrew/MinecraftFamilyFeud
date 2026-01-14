package io.letsrolldrew.feud.effects.timer;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class TimerCommands {
    private final Messages messages;
    private final TimerService timerService;
    private final String hostPermission;
    private final String adminPermission;

    public TimerCommands(Messages messages, TimerService timerService, String hostPermission, String adminPermission) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.timerService = Objects.requireNonNull(timerService, "timerService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
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
            messages.error(sender, Msg.SECONDS_CANNOT_BE_NEGATIVE);
            return;
        }
        if (seconds == null) {
            timerService.start();
            messages.success(sender, Msg.TIMER_STARTED);
        } else {
            timerService.start(seconds);
            messages.success(sender, Msg.TIMER_STARTED_FOR, Placeholder.of("seconds", seconds));
        }
    }

    private void handleStop(CommandSender sender) {
        timerService.stop();
        messages.success(sender, Msg.TIMER_STOPPED);
    }

    private void handleReset(CommandSender sender, String[] args) {
        Integer seconds = parseSeconds(args, 1);
        if (seconds != null && seconds < 0) {
            messages.error(sender, Msg.SECONDS_CANNOT_BE_NEGATIVE);
            return;
        }
        if (seconds == null) {
            timerService.reset();
            messages.success(sender, Msg.TIMER_RESET_DEFAULT);
        } else {
            timerService.reset(seconds);
            messages.success(sender, Msg.TIMER_RESET_TO, Placeholder.of("seconds", seconds));
        }
    }

    private void handleStatus(CommandSender sender) {
        TimerService.TimerStatus status = timerService.status();
        String running = status.running() ? "running" : "stopped";
        messages.info(
                sender,
                Msg.TIMER_STATUS,
                Placeholder.of("state", running),
                Placeholder.of("seconds", status.remainingSeconds()));
    }

    private boolean help(CommandSender sender) {
        messages.usage(sender, Msg.TIMER_HELP);
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
