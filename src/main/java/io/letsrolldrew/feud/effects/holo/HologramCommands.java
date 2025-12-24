package io.letsrolldrew.feud.effects.holo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
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
        if (args.length < 1) {
            sendUsage(sender);
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
            sender.sendMessage("Only players can spawn holograms.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud holo text spawn <id> <text>");
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

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /feud holo text set <id> <text>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        if (!service.exists(id)) {
            sender.sendMessage("Hologram not found: " + id);
            return;
        }
        String textRaw = joinArgs(args, 2);
        service.setText(id, colored(textRaw));
        sender.sendMessage("Updated hologram '" + id + "'.");
    }

    private void handleMove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can move holograms.");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud holo text move <id>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        if (!service.exists(id)) {
            sender.sendMessage("Hologram not found: " + id);
            return;
        }
        service.moveToPlayer(id, player);
        sender.sendMessage("Moved hologram '" + id + "' to your location.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud holo text remove <id>");
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        if (!service.exists(id)) {
            sender.sendMessage("Hologram not found: " + id);
            return;
        }
        service.remove(id);
        sender.sendMessage("Removed hologram '" + id + "'.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud holo text <spawn|set|move|remove> ...");
        sender.sendMessage("       /feud holo item <spawn|move|remove> ...");
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
            sender.sendMessage("Only players can spawn item holograms.");
            return;
        }
        if (args.length < 3) {
            sendItemUsage(sender);
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        Material material = Material.ECHO_SHARD; // default material justincase for testing
        // args: spawn <id> <material> <cmd>
        try {
            int cmd = Integer.parseInt(args[2]);
            service.spawnItem(id, player, material, cmd);
            sender.sendMessage("Spawned item hologram '" + id + "' with CMD " + cmd + ".");
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
            sender.sendMessage("Unknown material: " + args[2]);
            return;
        }
        try {
            int cmd = Integer.parseInt(args[3]);
            service.spawnItem(id, player, material, cmd);
            sender.sendMessage("Spawned item hologram '" + id + "' (" + material + ", CMD " + cmd + ").");
        } catch (NumberFormatException ex) {
            sender.sendMessage("CustomModelData must be a number.");
        }
    }

    private void handleItemMove(CommandSender sender, String[] args) {
        sender.sendMessage("Item move is not available yet.");
    }

    private void handleItemRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendItemUsage(sender);
            return;
        }
        String id = args[1];
        if (!isValidId(id)) {
            sender.sendMessage("Invalid id. Use letters, numbers, _ or -.");
            return;
        }
        service.removeItem(id);
        sender.sendMessage("Removed item hologram '" + id + "'.");
    }
}
