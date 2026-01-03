package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.effects.fastmoney.FastMoneyPlayerBindService;
import io.letsrolldrew.feud.util.Validation;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class FastMoneyCommands {
    private final FastMoneyService service;
    private final FastMoneySurveySetStore surveySetStore;
    private final FastMoneyPlayerBindService bindService;
    private final String hostPermission;
    private final String adminPermission;

    public FastMoneyCommands(
            FastMoneyService service,
            FastMoneySurveySetStore surveySetStore,
            FastMoneyPlayerBindService bindService,
            String hostPermission,
            String adminPermission) {
        this.service = Objects.requireNonNull(service, "service");
        this.surveySetStore = Objects.requireNonNull(surveySetStore, "surveySetStore");
        this.bindService = Objects.requireNonNull(bindService, "bindService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isHost(sender)) {
            sender.sendMessage("You must be the host to run Fast Money commands");
            return true;
        }

        if (args == null || args.length == 0) {
            return usage(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "set" -> set(sender, args);
            case "start" -> start(sender);
            case "stop" -> stop(sender);
            case "status" -> status(sender);
            case "bind" -> bind(sender, args);
            case "answer" -> answer(sender, args);
            default -> usage(sender);
        };
    }

    private boolean set(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud fastmoney set <setId>");
            return true;
        }

        String setId = args[1];
        Optional<FastMoneySurveySet> setOpt = surveySetStore.findById(setId);
        if (setOpt.isEmpty()) {
            sender.sendMessage("Fast Money set not found: " + setId);
            return true;
        }

        FastMoneySurveySet set = setOpt.get();
        service.loadSurveySet(set.id(), set.surveyIds());
        sender.sendMessage("Fast Money set loaded: " + set.id());

        return true;
    }

    private boolean start(CommandSender sender) {
        try {
            service.startRound();
            sender.sendMessage("Fast Money: Player 1 turn started");
        } catch (IllegalStateException ex) {
            sender.sendMessage(ex.getMessage());
        }

        return true;
    }

    private boolean stop(CommandSender sender) {
        service.stop();
        sender.sendMessage("Fast Money stopped");

        return true;
    }

    private boolean status(CommandSender sender) {
        FastMoneyRoundState state = service.state();
        sender.sendMessage("Fast Money status: phase=" + state.phase()
                + " set=" + state.surveySetId()
                + " q=" + state.activeQuestionIndex());
        return true;
    }

    private boolean bind(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud fastmoney bind <p1|p2|clear>");
            return true;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        if (target.equals("clear")) {
            bindService.clear();
            sender.sendMessage("Fast Money bindings cleared");
            return true;
        }

        if (!(sender instanceof Player host)) {
            sender.sendMessage("Only players can arm binding");
            return true;
        }

        if (target.equals("p1")) {
            bindService.armPlayer1(host.getUniqueId());
            sender.sendMessage("Fast Money: bind P1 armed. Right-click a player.");
            return true;
        }

        if (target.equals("p2")) {
            bindService.armPlayer2(host.getUniqueId());
            sender.sendMessage("Fast Money: bind P2 armed. Right-click a player.");
            return true;
        }

        sender.sendMessage("Usage: /feud fastmoney bind <p1|p2|clear>");
        return true;
    }

    private boolean answer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can submit answers");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /feud fastmoney answer <text...>");
            return true;
        }

        String answer =
                String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        try {
            service.submitAnswer(player.getUniqueId(), answer);
            sender.sendMessage("Answer recorded");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            sender.sendMessage(ex.getMessage());
        }

        return true;
    }

    private boolean usage(CommandSender sender) {
        sender.sendMessage("Fast Money: set|start|stop|status|bind|answer");
        return true;
    }

    private boolean isHost(CommandSender sender) {
        if (sender == null) {
            return false;
        }

        if (sender.hasPermission(hostPermission)) {
            return true;
        }

        return sender.hasPermission(adminPermission);
    }
}
