package io.letsrolldrew.feud.effects.holo;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Handles /feud holo subcommands
public final class HologramCommands {
    private final Messages messages;
    private final HologramService service;
    private final String adminPermission = "familyfeud.admin";

    public HologramCommands(Messages messages, HologramService service) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.service = service;
    }

    // return true if handled, false to signal help to caller.
    public boolean handle(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sendUsage(sender);
            return true;
        }
        if (!sender.hasPermission(adminPermission)) {
            messages.error(sender, Msg.NEED_PERMISSION, Placeholder.of("permission", adminPermission));
            return true;
        }
        if ("list".equalsIgnoreCase(args[0])) {
            handleList(sender);
            return true;
        }
        String category = args[0].toLowerCase();
        if ("text".equals(category)) {
            handleText(sender, sliceArgs(args));
        } else if ("item".equals(category)) {
            handleItem(sender, sliceArgs(args));
        } else {
            sendUsage(sender);
        }
        return true;
    }

    private void handleText(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendTextUsage(sender);
            return;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "spawn" -> handleSpawn(sender, args);
            case "set" -> handleSet(sender, args);
            case "move" -> handleMove(sender, args);
            case "remove" -> handleRemove(sender, args);
            default -> sendTextUsage(sender);
        }
    }

    private void handleItem(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendItemUsage(sender);
            return;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "spawn" -> handleItemSpawn(sender, args);
            case "move" -> handleItemMove(sender, args);
            case "remove" -> handleItemRemove(sender, args);
            default -> sendItemUsage(sender);
        }
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud holo text spawn <id> <text>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        if (service.exists(id)) {
            messages.error(sender, Msg.HOLOGRAM_ID_EXISTS, Placeholder.of("id", id));
            return;
        }
        String textRaw = joinArgs(args, 2);
        Component text = colored(textRaw);
        service.spawn(id, player, text);
        messages.success(sender, Msg.HOLOGRAM_SPAWNED, Placeholder.of("id", id));
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud holo text set <id> <text>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        if (!service.exists(id)) {
            messages.error(sender, Msg.HOLOGRAM_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        String textRaw = joinArgs(args, 2);
        service.setText(id, colored(textRaw));
        messages.success(sender, Msg.HOLOGRAM_UPDATED, Placeholder.of("id", id));
    }

    private void handleMove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud holo text move <id>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        if (!service.exists(id)) {
            messages.error(sender, Msg.HOLOGRAM_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        service.moveToPlayer(id, player);
        messages.success(sender, Msg.HOLOGRAM_MOVED_TO_YOU, Placeholder.of("id", id));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud holo text remove <id>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        if (!service.exists(id)) {
            messages.error(sender, Msg.HOLOGRAM_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        service.remove(id);
        messages.success(sender, Msg.HOLOGRAM_REMOVED, Placeholder.of("id", id));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud holo text <spawn|set|move|remove> ...");
        sender.sendMessage("       /feud holo item <spawn|move|remove> ...");
        sender.sendMessage("       /feud holo list");
    }

    private void sendTextUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud holo text spawn <id> <text> | set <id> <text> | move <id> | remove <id>");
    }

    private void sendItemUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud holo item spawn <id> [material] <customModelData> | move <id> | remove <id>");
    }

    private String[] sliceArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] out = new String[args.length - 1];
        System.arraycopy(args, 1, out, 0, args.length - 1);
        return out;
    }

    private boolean isValidId(String id) {
        return id != null && id.matches("[A-Za-z0-9_-]+");
    }

    private String joinArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        return sb.toString();
    }

    private Component colored(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    private void handleItemSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 3) {
            sendItemUsage(sender);
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        Material material = Material.ECHO_SHARD; // default material justincase for testing
        // args: spawn <id> <material> <cmd>
        try {
            int cmd = Integer.parseInt(args[2]);
            service.spawnItem(id, player, material, cmd);
            messages.success(
                    sender, Msg.ITEM_HOLOGRAM_SPAWNED_WITH_CMD, Placeholder.of("id", id), Placeholder.of("cmd", cmd));
            return;
        } catch (NumberFormatException ignore) {
            // treat args[2] as material
        }
        if (args.length < 4) {
            sendItemUsage(sender);
            return;
        }
        try {
            material = Material.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException ex) {
            messages.error(sender, Msg.UNKNOWN_MATERIAL, Placeholder.of("material", args[2]));
            return;
        }
        try {
            int cmd = Integer.parseInt(args[3]);
            service.spawnItem(id, player, material, cmd);
            messages.success(
                    sender,
                    Msg.ITEM_HOLOGRAM_SPAWNED_WITH_MATERIAL_CMD,
                    Placeholder.of("id", id),
                    Placeholder.of("material", material),
                    Placeholder.of("cmd", cmd));
        } catch (NumberFormatException ex) {
            sender.sendMessage("CustomModelData must be a number.");
        }
    }

    private void handleItemMove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return;
        }
        if (args.length < 2) {
            sendItemUsage(sender);
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        if (!service.exists(id)) {
            messages.error(sender, Msg.HOLOGRAM_NOT_FOUND, Placeholder.of("id", id));
            return;
        }
        service.moveItemToPlayer(id, player);
        messages.success(sender, Msg.ITEM_HOLOGRAM_MOVED_TO_YOU, Placeholder.of("id", id));
    }

    private void handleItemRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendItemUsage(sender);
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            messages.error(sender, Msg.INVALID_ID_HOLO);
            return;
        }
        service.removeItem(id);
        messages.success(sender, Msg.ITEM_HOLOGRAM_REMOVED, Placeholder.of("id", id));
    }

    private void handleList(CommandSender sender) {
        var entries = service.entriesSnapshot();
        if (entries.isEmpty()) {
            sender.sendMessage("No holograms are active.");
            return;
        }
        int textCount = 0;
        int itemCount = 0;
        sender.sendMessage("Holograms:");
        for (Map.Entry<String, HologramService.HologramEntry> e : entries.entrySet()) {
            String id = e.getKey();
            HologramType type = e.getValue().type();
            if (type == HologramType.TEXT_DISPLAY) {
                textCount++;
            } else if (type == HologramType.ITEM_DISPLAY) {
                itemCount++;
            }
            sender.sendMessage("- " + id + " (" + type.name().toLowerCase() + ")");
        }
        sender.sendMessage("Totals: text=" + textCount + " item=" + itemCount);
    }
}
