package io.letsrolldrew.feud.team;

import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;

public final class TeamCommands {
    private final TeamService teamService;
    private final String hostPermission;
    private final String adminPermission;

    public TeamCommands(TeamService teamService, String hostPermission, String adminPermission) {
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            sender.sendMessage("You must be the host to do that.");
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
            default -> help(sender);
        }
        return true;
    }

    private void handleInfo(CommandSender sender) {
        sender.sendMessage("Teams:");
        sender.sendMessage(formatTeamLine(TeamId.RED));
        sender.sendMessage(formatTeamLine(TeamId.BLUE));
    }

    private void handleReset(CommandSender sender) {
        teamService.reset();
        sender.sendMessage("Teams reset.");
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage("Usage: /feud team set <red|blue> name <new-name>");
            return;
        }

        TeamId team = TeamId.fromString(args[1]);
        if (team == null) {
            sender.sendMessage("Team must be red or blue.");
            return;
        }

        String field = args[2].toLowerCase(Locale.ROOT);
        if (!field.equals("name")) {
            sender.sendMessage("Usage: /feud team set <red|blue> name <new-name>");
            return;
        }

        String newName = joinArgs(args, 3);
        if (newName.isBlank()) {
            sender.sendMessage("Name must be non-blank.");
            return;
        }

        boolean changed = teamService.setName(team, newName);
        if (!changed) {
            sender.sendMessage("Name unchanged.");
            return;
        }

        sender.sendMessage("Team " + team.name() + " name set to '" + teamService.getName(team) + "'.");
    }

    private boolean help(CommandSender sender) {
        sender.sendMessage("Team commands:");
        sender.sendMessage("/feud team info");
        sender.sendMessage("/feud team reset");
        sender.sendMessage("/feud team set <red|blue> name <new-name>");
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

    private String formatTeamLine(TeamId team) {
        if (team == null) {
            return "(unknown team)";
        }

        String name = teamService.getName(team);
        int score = teamService.getScore(team);
        BlockRef buzzer = teamService.getBuzzer(team);

        String buzzerLabel = "unbound";
        if (buzzer != null) {
            buzzerLabel = formatBlockRef(buzzer);
        }

        return team.name() + ": " + name + " | score=" + score + " | buzzer=" + buzzerLabel;
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
