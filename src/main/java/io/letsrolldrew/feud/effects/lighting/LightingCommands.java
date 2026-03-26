package io.letsrolldrew.feud.effects.lighting;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.util.Validation;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class LightingCommands {
    private final Messages messages;
    private final StageLightingService stageLightingService;
    private final String hostPermission;
    private final String adminPermission;

    public LightingCommands(
            Messages messages,
            StageLightingService stageLightingService,
            String hostPermission,
            String adminPermission) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.stageLightingService = Objects.requireNonNull(stageLightingService, "stageLightingService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isAuthorized(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }
        if (args == null || args.length == 0) {
            messages.usage(sender, Msg.LIGHTING_HELP);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "mode" -> handleMode(sender, args);
            case "animation" -> handleAnimation(sender, args);
            case "jingle" -> handleJingle(sender, args);
            case "stop" -> handleStop(sender);
            case "status" -> handleStatus(sender);
            case "column" -> handleColumn(sender, args);
            case "center" -> handleCenter(sender, args);
            case "scan" -> handleScan(sender, args);
            default -> messages.usage(sender, Msg.LIGHTING_HELP);
        }
        return true;
    }

    private void handleMode(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_LIGHTING_MODE);
            return;
        }
        String id = args[1].trim();
        if (!stageLightingService.applyMode(id)) {
            messages.error(sender, Msg.LIGHTING_MODE_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        messages.success(sender, Msg.LIGHTING_MODE_APPLIED, Placeholder.of("id", id));
    }

    private void handleAnimation(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_LIGHTING_ANIMATION);
            return;
        }
        String id = args[1].trim();
        if (!stageLightingService.playAnimation(id)) {
            messages.error(sender, Msg.LIGHTING_ANIMATION_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        messages.success(sender, Msg.LIGHTING_ANIMATION_STARTED, Placeholder.of("id", id));
    }

    private void handleJingle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_LIGHTING_JINGLE);
            return;
        }
        String id = args[1].trim();
        if (!stageLightingService.triggerJingle(id)) {
            messages.error(sender, Msg.LIGHTING_JINGLE_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        messages.success(sender, Msg.LIGHTING_JINGLE_TRIGGERED, Placeholder.of("id", id));
    }

    private void handleStop(CommandSender sender) {
        stageLightingService.stopAnimation();
        messages.success(sender, Msg.LIGHTING_STOPPED);
    }

    private void handleStatus(CommandSender sender) {
        StageLightingService.LightingStatus status = stageLightingService.status();
        messages.info(
                sender,
                Msg.LIGHTING_STATUS,
                Placeholder.of("mode", status.activeModeOrNone()),
                Placeholder.of("animation", status.activeAnimationOrNone()),
                Placeholder.of("center", status.centerOrNone()),
                Placeholder.of("axis", status.axis()),
                Placeholder.of("columns", status.columns()),
                Placeholder.of("palettes", status.palettes()),
                Placeholder.of("modes", status.modes()),
                Placeholder.of("animations", status.animations()),
                Placeholder.of("jingles", status.jingles()));
    }

    private void handleColumn(CommandSender sender, String[] args) {
        if (args.length < 2 || !"list".equalsIgnoreCase(args[1])) {
            messages.usage(sender, Msg.USAGE_LIGHTING_COLUMN);
            return;
        }
        List<String> ids = stageLightingService.columnIds();
        if (ids.isEmpty()) {
            messages.info(sender, Msg.LIGHTING_COLUMNS_EMPTY);
            return;
        }
        messages.info(sender, Msg.LIGHTING_COLUMNS, Placeholder.of("columns", String.join(", ", ids)));
    }

    private void handleCenter(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_LIGHTING_CENTER);
            return;
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        switch (action) {
            case "bind" -> {
                if (!(sender instanceof Player player)) {
                    messages.error(sender, Msg.PLAYER_ONLY);
                    return;
                }
                if (!stageLightingService.bindCenter(player.getLocation())) {
                    messages.error(sender, Msg.NOT_READY);
                    return;
                }
                var arena = stageLightingService.arena();
                messages.success(
                        sender,
                        Msg.LIGHTING_CENTER_BOUND,
                        Placeholder.of("world", arena.world()),
                        Placeholder.of("x", arena.centerX()),
                        Placeholder.of("y", arena.centerY()),
                        Placeholder.of("z", arena.centerZ()));
            }
            case "clear" -> {
                stageLightingService.clearCenter();
                messages.success(sender, Msg.LIGHTING_CENTER_CLEARED);
            }
            case "status" -> sendCenterStatus(sender);
            default -> messages.usage(sender, Msg.USAGE_LIGHTING_CENTER);
        }
    }

    private void handleScan(CommandSender sender, String[] args) {
        if (!stageLightingService.hasBoundCenter()) {
            messages.error(sender, Msg.LIGHTING_CENTER_NOT_BOUND);
            return;
        }

        StageLightingConfig.Arena arena = stageLightingService.arena();
        int index = 1;
        StageLightingConfig.ScanAxis axis = arena.axis();
        if (args.length > index) {
            StageLightingConfig.ScanAxis parsedAxis = StageLightingConfig.ScanAxis.fromString(args[index]);
            if (parsedAxis != null) {
                axis = parsedAxis;
                index++;
            }
        }

        Integer radiusX = parseOptionalInt(args, index, arena.radiusX());
        Integer radiusZ = parseOptionalInt(args, index + 1, arena.radiusZ());
        Integer yDown = parseOptionalInt(args, index + 2, arena.yDown());
        Integer yUp = parseOptionalInt(args, index + 3, arena.yUp());
        if (radiusX == null || radiusZ == null || yDown == null || yUp == null) {
            messages.usage(sender, Msg.USAGE_LIGHTING_SCAN);
            return;
        }

        int columns = stageLightingService.scanColumns(axis, radiusX, radiusZ, yDown, yUp);
        if (columns < 0) {
            messages.error(sender, Msg.LIGHTING_CENTER_NOT_BOUND);
            return;
        }
        messages.success(
                sender,
                Msg.LIGHTING_SCAN_COMPLETE,
                Placeholder.of("columns", columns),
                Placeholder.of("axis", axis.name().toLowerCase(Locale.ROOT)),
                Placeholder.of("radiusX", radiusX),
                Placeholder.of("radiusZ", radiusZ),
                Placeholder.of("yDown", yDown),
                Placeholder.of("yUp", yUp));
    }

    private void sendCenterStatus(CommandSender sender) {
        if (!stageLightingService.hasBoundCenter()) {
            messages.info(sender, Msg.LIGHTING_CENTER_NOT_BOUND);
            return;
        }
        StageLightingConfig.Arena arena = stageLightingService.arena();
        messages.info(
                sender,
                Msg.LIGHTING_CENTER_STATUS,
                Placeholder.of("world", arena.world()),
                Placeholder.of("x", arena.centerX()),
                Placeholder.of("y", arena.centerY()),
                Placeholder.of("z", arena.centerZ()),
                Placeholder.of("axis", arena.axis().name().toLowerCase(Locale.ROOT)),
                Placeholder.of("radiusX", arena.radiusX()),
                Placeholder.of("radiusZ", arena.radiusZ()),
                Placeholder.of("yDown", arena.yDown()),
                Placeholder.of("yUp", arena.yUp()),
                Placeholder.of("columns", stageLightingService.columnIds().size()));
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

    private static Integer parseOptionalInt(String[] args, int index, int fallback) {
        if (args == null || index >= args.length) {
            return fallback;
        }
        try {
            int value = Integer.parseInt(args[index]);
            return value < 0 ? null : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
