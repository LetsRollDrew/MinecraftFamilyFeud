package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DisplayBoardCommands {
    private final DisplayBoardPresenter presenter;
    private final String adminPermission;
    private final DisplayBoardSelectionListener selectionListener;

    public DisplayBoardCommands(DisplayBoardPresenter presenter, String adminPermission, DisplayBoardSelectionListener selectionListener) {
        this.presenter = presenter;
        this.adminPermission = adminPermission;
        this.selectionListener = selectionListener;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permissions");
            return true;
        }
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }
        String action = args[0].toLowerCase();
        switch (action) {
            case "create" -> handleCreate(sender, args);
            case "wand" -> handleWand(sender);
            case "destroy", "reveal", "hide", "demo" -> sendUsage(sender);
            default -> sendUsage(sender);
        }
        return true;
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create a board");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board create <boardId>");
            return;
        }
        String boardId = args[1];
        presenter.createBoard(boardId, player.getLocation(), player);
        sender.sendMessage("Board '" + boardId + "' created at your location.");
    }

    private void handleWand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the selector wand");
            return;
        }
        if (selectionListener == null) {
            sender.sendMessage("Selector wand is not available");
            return;
        }
        selectionListener.giveWand(player);
        sender.sendMessage("Display board selector given.");
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Board commands: create/wand");
    }
}
