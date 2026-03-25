package io.letsrolldrew.feud.team;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class TeamCommands {
    private final Messages messages;
    private final TeamService teamService;
    private final String hostPermission;
    private final String adminPermission;
    private final io.letsrolldrew.feud.effects.buzz.BuzzerCommands buzzerCommands;

    public TeamCommands(Messages messages, TeamService teamService, String hostPermission, String adminPermission) {
        this(messages, teamService, hostPermission, adminPermission, null);
    }

    public TeamCommands(
            Messages messages,
            TeamService teamService,
            String hostPermission,
            String adminPermission,
            io.letsrolldrew.feud.effects.buzz.BuzzerCommands buzzerCommands) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
        this.buzzerCommands = buzzerCommands;
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
            case "info" -> handleInfo(sender);
            case "reset" -> handleReset(sender);
            case "set" -> handleSet(sender, args);
            case "buzzer" -> handleBuzzer(sender, args);
            default -> help(sender);
        }
        return true;
    }

    private void handleInfo(CommandSender sender) {
        messages.info(sender, Msg.TEAM_INFO_HEADER);
        sendTeamLine(sender, TeamId.RED);
        sendTeamLine(sender, TeamId.BLUE);
    }

    private void handleReset(CommandSender sender) {
        teamService.reset();
        if (buzzerCommands != null) {
            buzzerCommands.handleTeamReset();
        }
        messages.success(sender, Msg.TEAMS_RESET);
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            messages.usage(sender, Msg.USAGE_TEAM_SET_NAME);
            return;
        }

        TeamId team = TeamId.fromString(args[1]);
        if (team == null) {
            messages.error(sender, Msg.TEAM_MUST_BE_RED_BLUE);
            return;
        }

        String field = args[2].toLowerCase(Locale.ROOT);
        if (!field.equals("name")) {
            messages.usage(sender, Msg.USAGE_TEAM_SET_NAME);
            return;
        }

        String newName = joinArgs(args, 3);
        if (newName.isBlank()) {
            messages.error(sender, Msg.TEAM_NAME_MUST_BE_NON_BLANK);
            return;
        }

        boolean changed = teamService.setName(team, newName);
        if (!changed) {
            messages.info(sender, Msg.TEAM_NAME_UNCHANGED);
            return;
        }

        messages.success(
                sender,
                Msg.TEAM_NAME_SET,
                Placeholder.of("team", team.name()),
                Placeholder.of("name", teamService.getName(team)));
    }

    private boolean help(CommandSender sender) {
        messages.usage(sender, Msg.TEAM_HELP);
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

    private void handleBuzzer(CommandSender sender, String[] args) {
        if (buzzerCommands == null) {
            messages.error(sender, Msg.BUZZER_COMMANDS_NOT_AVAILABLE);
            return;
        }
        String[] tail = tail(args, 1);
        buzzerCommands.handleTeamBuzzer(sender, tail);
    }

    private String[] tail(String[] args, int start) {
        if (args == null || start >= args.length) {
            return new String[0];
        }
        String[] out = new String[args.length - start];
        System.arraycopy(args, start, out, 0, args.length - start);
        return out;
    }

    private void sendTeamLine(CommandSender sender, TeamId team) {
        if (sender == null || team == null) {
            return;
        }

        String name = teamService.getName(team);
        int score = teamService.getScore(team);
        BlockRef buzzer = teamService.getBuzzer(team);

        String buzzerLabel = buzzer == null ? "unbound" : formatBlockRef(buzzer);

        messages.info(
                sender,
                Msg.TEAM_INFO_LINE,
                Placeholder.of("team", team.name()),
                Placeholder.of("name", name),
                Placeholder.of("score", score),
                Placeholder.of("buzzer", buzzerLabel));
    }

    private static String formatBlockRef(BlockRef ref) {
        if (ref == null) {
            return "unbound";
        }
        return ref.x() + "," + ref.y() + "," + ref.z();
    }

    private static String joinArgs(String[] args, int startIndex) {
        if (args == null || startIndex >= args.length) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            String part = args[i];
            if (part == null || part.isBlank()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(part);
        }

        return sb.toString().trim();
    }
}
