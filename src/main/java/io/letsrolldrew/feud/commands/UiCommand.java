package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.util.Validation;
import org.bukkit.command.CommandSender;

public final class UiCommand {
    private final GameController controller;
    private final String hostPermission;

    public UiCommand(GameController controller, String hostPermission) {
        this.controller = controller;
        this.hostPermission = Validation.requireNonBlank(hostPermission, "host-permission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to use these controls.");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String action = args[0].toLowerCase();
        switch (action) {
            case "reveal" -> handleReveal(sender, args);
            case "strike" -> handleStrike(sender);
            case "clearstrikes" -> handleClearStrikes(sender);
            case "add" -> handleAddPoints(sender, args);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleReveal(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud ui reveal <1-8>");
            return;
        }
        int slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Slot must be a number between 1 and 8.");
            return;
        }
        if (slot < 1 || slot > 8) {
            sender.sendMessage("Slot must be between 1 and 8.");
            return;
        }
        controller.revealSlot(slot);
        sender.sendMessage("Revealed slot " + slot + ".");
    }

    private void handleStrike(CommandSender sender) {
        controller.strike();
        sender.sendMessage("Strike recorded (" + controller.strikeCount() + "/" + controller.maxStrikes() + ").");
    }

    private void handleClearStrikes(CommandSender sender) {
        controller.clearStrikes();
        sender.sendMessage("Strikes cleared.");
    }

    private void handleAddPoints(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud ui add <points>");
            return;
        }
        int points;
        try {
            points = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Points must be a positive number.");
            return;
        }
        if (points <= 0) {
            sender.sendMessage("Points must be positive.");
            return;
        }
        controller.addPoints(points);
        sender.sendMessage("Added " + points + " points. Round total: " + controller.roundPoints());
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud ui <reveal, strike, clearstrikes, add>");
    }
}
