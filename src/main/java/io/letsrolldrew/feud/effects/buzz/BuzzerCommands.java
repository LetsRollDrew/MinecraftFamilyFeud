package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.util.Validation;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BuzzerCommands {
    private final Messages messages;
    private final BuzzerService buzzerService;
    private final TeamService teamService;
    private final BuzzerHitboxService buzzerHitboxService;
    private final BuzzerBindingStore buzzerBindingStore;
    private final String hostPermission;
    private final String adminPermission;

    public BuzzerCommands(
            Messages messages,
            BuzzerService buzzerService,
            TeamService teamService,
            BuzzerHitboxService buzzerHitboxService,
            BuzzerBindingStore buzzerBindingStore,
            String hostPermission,
            String adminPermission) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.buzzerService = Objects.requireNonNull(buzzerService, "buzzerService");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.buzzerHitboxService = Objects.requireNonNull(buzzerHitboxService, "buzzerHitboxService");
        this.buzzerBindingStore = Objects.requireNonNull(buzzerBindingStore, "buzzerBindingStore");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handleTeamBuzzer(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }
        if (args == null || args.length < 2) {
            messages.usage(sender, Msg.USAGE_TEAM_BUZZER);
            return true;
        }
        String action = args[0].toLowerCase(Locale.ROOT);
        TeamId team = TeamId.fromString(args[1]);
        if (team == null) {
            messages.error(sender, Msg.TEAM_MUST_BE_RED_BLUE);
            return true;
        }

        switch (action) {
            case "bind" -> handleBind(sender, team);
            case "clear" -> handleClear(sender, team);
            case "test" -> handleTest(sender, team);
            default -> messages.usage(sender, Msg.USAGE_TEAM_BUZZER);
        }
        return true;
    }

    public boolean handleBuzzReset(CommandSender sender) {
        if (!isAuthorized(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }
        buzzerService.resetLock();
        messages.success(sender, Msg.BUZZ_LOCK_RESET);
        return true;
    }

    public void handleTeamReset() {
        buzzerBindingStore.clearAll();
        buzzerHitboxService.syncAll();
    }

    private void handleBind(CommandSender sender, TeamId team) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        buzzerService.beginBind(player, team);
        messages.info(sender, Msg.BUZZER_BIND_PROMPT, Placeholder.of("team", team.name()));
    }

    private void handleClear(CommandSender sender, TeamId team) {
        buzzerService.clearBind(team);
        buzzerBindingStore.clear(team);
        buzzerHitboxService.syncAll();
        messages.success(sender, Msg.BUZZER_CLEARED, Placeholder.of("team", team.name()));
    }

    private void handleTest(CommandSender sender, TeamId team) {
        BlockRef ref = teamService.getBuzzer(team);
        if (ref == null) {
            messages.error(sender, Msg.BUZZER_NOT_BOUND, Placeholder.of("team", team.name()));
            return;
        }
        messages.info(
                sender,
                Msg.BUZZER_LOCATION,
                Placeholder.of("team", team.name()),
                Placeholder.of("x", ref.x()),
                Placeholder.of("y", ref.y()),
                Placeholder.of("z", ref.z()));
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
