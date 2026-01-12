package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HostBookCommandEntry {
    private final String hostPermission;
    private final HostBookService hostBookService;
    private final DisplayBoardService displayBoardPresenter;

    public HostBookCommandEntry(
            String hostPermission, HostBookService hostBookService, DisplayBoardService displayBoardPresenter) {
        this.hostPermission = hostPermission;
        this.hostBookService = hostBookService;
        this.displayBoardPresenter = displayBoardPresenter;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the host book");
            return true;
        }
        if (!player.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to use this");
            return true;
        }

        String flavor = args.length >= 1 ? CommandArgs.joinTail(args, 0) : "";
        String raw = flavor == null ? "" : flavor.trim();
        String head = raw.isBlank() ? "" : raw.split("\\s+", 2)[0].toLowerCase();
        String tail = raw.isBlank() ? "" : raw.replaceFirst("^\\S+\\s*", "");

        switch (head) {
            case "map" -> hostBookService.giveMapBook(player);
            case "display" -> {
                List<String> ids = new ArrayList<>(displayBoardPresenter.listBoards());
                hostBookService.giveDisplayBook(player, ids, tail);
            }
            case "cleanup" -> hostBookService.giveCleanupBook(player);
            default -> hostBookService.giveSelectorBook(player);
        }
        return true;
    }
}
