package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BoardCommands {
    private final DisplayBoardPresenter presenter;
    private final String adminPermission;

    public BoardCommands(DisplayBoardPresenter presenter, String adminPermission) {
        this.presenter = presenter;
        this.adminPermission = adminPermission;
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

    private void sendUsage(CommandSender sender) {
        sender.sendMessage("Board commands: create/destroy/reveal/hide/demo");
    }
}
