package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BuzzerCommands {
    private final BuzzerService buzzerService;
    private final TeamService teamService;
    private final String hostPermission;
    private final String adminPermission;

    public BuzzerCommands(
            BuzzerService buzzerService, TeamService teamService, String hostPermission, String adminPermission) {
        this.buzzerService = Objects.requireNonNull(buzzerService, "buzzerService");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handleTeamBuzzer(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            sender.sendMessage("You must be the host to do that.");
            return true;
        }
        if (args == null || args.length < 2) {
            sender.sendMessage("Usage: /feud team buzzer <bind|clear|test> <red|blue>");
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        TeamId team = TeamId.fromString(args[1]);
        if (team == null) {
            sender.sendMessage("Team must be red or blue.");
            return true;
        }

        switch (action) {
            case "bind" -> handleBind(sender, team);
            case "clear" -> handleClear(sender, team);
            case "test" -> handleTest(sender, team);
            default -> sender.sendMessage("Usage: /feud team buzzer <bind|clear|test> <red|blue>");
        }
        return true;
    }

    public boolean handleBuzzReset(CommandSender sender) {
        if (!isAuthorized(sender)) {
            sender.sendMessage("You must be the host to do that.");
            return true;
        }
        buzzerService.resetLock();
        sender.sendMessage("Buzz lock reset.");
        return true;
    }

    private void handleBind(CommandSender sender, TeamId team) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can bind buzzers.");
            return;
        }
        buzzerService.beginBind(player, team);
        sender.sendMessage("Binding buzzer for " + team.name() + ". Right-click a block to bind.");
    }

    private void handleClear(CommandSender sender, TeamId team) {
        buzzerService.clearBind(team);
        sender.sendMessage("Cleared buzzer for " + team.name() + ".");
    }

    private void handleTest(CommandSender sender, TeamId team) {
        BlockRef ref = teamService.getBuzzer(team);
        if (ref == null) {
            sender.sendMessage("No buzzer bound for " + team.name() + ".");
            return;
        }
        sender.sendMessage("Buzzer for " + team.name() + " at "
                + ref.x() + "," + ref.y() + "," + ref.z() + ".");
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
}
