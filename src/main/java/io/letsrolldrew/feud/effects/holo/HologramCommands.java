package io.letsrolldrew.feud.effects.holo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//Handles /feud holo subcommands
public final class HologramCommands {
    private final HologramService service;

    public HologramCommands(HologramService service) {
        this.service = service;
    }

    // return true if handled, false to signal help to caller.
    public boolean handle(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String action = args[0].toLowerCase();
        if ("spawn".equals(action)) {
            handleSpawn(sender, args);
        } else {
            sender.sendMessage("Only spawn is implemented in this step.");
        }
        return true;
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can spawn holograms.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud holo spawn <id> <text>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        if (service.exists(id)) {
            sender.sendMessage("Hologram id already exists: " + id);
            return;
        }
        String textRaw = joinArgs(args, 2);
        Component text = colored(textRaw);
        service.spawn(id, player, text);
        sender.sendMessage("Spawned hologram '" + id + "'.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud holo spawn <id> <text> (other subcommands coming in later steps)");
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
}
