package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.util.Validation;
import java.util.function.Consumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class UiCommand {
    private final GameController controller;
    private final String hostPermission;
    private final Consumer<Player> bookRefresher;
    private final Consumer<Integer> revealCallback;

    public UiCommand(
            GameController controller,
            String hostPermission,
            Consumer<Player> bookRefresher,
            Consumer<Integer> revealCallback) {
        this.controller = controller;
        this.hostPermission = Validation.requireNonBlank(hostPermission, "host-permission");
        this.bookRefresher = bookRefresher;
        this.revealCallback = revealCallback;
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
            case "control" -> handleControl(sender, args);
            case "award" -> handleAward(sender);
            case "reset" -> handleReset(sender);
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
        if (revealCallback != null) {
            revealCallback.accept(slot);
        }
        refreshIfPlayer(sender);
    }

    private void handleStrike(CommandSender sender) {
        controller.strike();
        sender.sendMessage("Strike recorded (" + controller.strikeCount() + "/" + controller.maxStrikes() + ").");
        refreshIfPlayer(sender);
    }

    private void handleClearStrikes(CommandSender sender) {
        controller.clearStrikes();
        sender.sendMessage("Strikes cleared.");
        refreshIfPlayer(sender);
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
        refreshIfPlayer(sender);
    }

    private void handleControl(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud ui control <red|blue>");
            return;
        }
        TeamControl team = TeamControl.fromString(args[1]);
        if (team == TeamControl.NONE) {
            sender.sendMessage("Team must be red or blue.");
            return;
        }
        controller.setControllingTeam(team);
        sender.sendMessage("Control set to " + team.name());
        refreshIfPlayer(sender);
    }

    private void handleAward(CommandSender sender) {
        if (controller.controllingTeam() == TeamControl.NONE) {
            sender.sendMessage("Set a team in control before awarding points.");
            return;
        }
        int before = controller.roundPoints();
        controller.awardRoundPoints();
        sender.sendMessage("Awarded " + before + " points to "
                + controller.controllingTeam().name() + ".");
        refreshIfPlayer(sender);
    }

    private void handleReset(CommandSender sender) {
        controller.resetRoundState();
        sender.sendMessage("Round state reset.");
        refreshIfPlayer(sender);
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Usage: /feud ui <reveal, strike, clearstrikes, add, control, award, reset>");
    }

    private void refreshIfPlayer(CommandSender sender) {
        if (bookRefresher != null && sender instanceof Player player) {
            bookRefresher.accept(player);
        }
    }
}
