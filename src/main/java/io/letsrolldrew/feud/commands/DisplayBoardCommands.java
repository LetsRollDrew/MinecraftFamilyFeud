package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.board.display.DynamicBoardLayoutBuilder;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionListener;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class DisplayBoardCommands {
    private final DisplayBoardPresenter presenter;
    private final String adminPermission;
    private final DisplayBoardSelectionListener selectionListener;
    private final DisplayBoardSelectionStore selectionStore;

    public DisplayBoardCommands(DisplayBoardPresenter presenter, String adminPermission, DisplayBoardSelectionListener selectionListener, DisplayBoardSelectionStore selectionStore) {
        this.presenter = presenter;
        this.adminPermission = adminPermission;
        this.selectionListener = selectionListener;
        this.selectionStore = selectionStore;
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
            case "dynamic" -> handleCreateDynamic(sender, args);
            case "list" -> handleList(sender);
            case "remove", "delete" -> handleRemove(sender, args);
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

    private void handleCreateDynamic(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can create a board");
            return;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board display dynamic <boardId>");
            return;
        }
        if (selectionStore == null) {
            sender.sendMessage("No selection available");
            return;
        }
        DisplayBoardSelection selection = selectionStore.get(player.getUniqueId());
        if (selection == null) {
            sender.sendMessage("No selection saved. Use the selector wand first.");
            return;
        }
        var result = DynamicBoardLayoutBuilder.build(selection);
        if (!result.success()) {
            sender.sendMessage("Selection invalid: " + result.error());
            return;
        }
        String boardId = args[1];
        if (presenter.createDynamicBoard(boardId, result.layout()) == null) {
            sender.sendMessage("Board id already exists or creation failed.");
            return;
        }
        sender.sendMessage("Dynamic board '" + boardId + "' created.");
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /feud board display remove <boardId>");
            return;
        }
        presenter.destroyBoard(args[1]);
        sender.sendMessage("Board '" + args[1] + "' removed.");
    }

    private void handleList(CommandSender sender) {
        var ids = presenter.listBoards();
        if (ids.isEmpty()) {
            sender.sendMessage("No boards active.");
            return;
        }
        sender.sendMessage("Boards: " + String.join(", ", ids));
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
        sender.sendMessage("Board commands: create/dynamic/list/remove/wand");
    }
}
