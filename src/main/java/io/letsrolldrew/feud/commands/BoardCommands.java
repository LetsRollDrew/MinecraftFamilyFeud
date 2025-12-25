package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import org.bukkit.command.CommandSender;

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
        sender.sendMessage("Board commands: create/destroy/reveal/hide/demo");
        return true;
    }
}
