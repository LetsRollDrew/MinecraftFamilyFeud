package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HostBookCommandEntry {
    private final Messages messages;
    private final String hostPermission;
    private final HostBookService hostBookService;
    private final DisplayBoardService displayBoardPresenter;

    public HostBookCommandEntry(
            Messages messages,
            String hostPermission,
            HostBookService hostBookService,
            DisplayBoardService displayBoardPresenter) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.hostPermission = hostPermission;
        this.hostBookService = hostBookService;
        this.displayBoardPresenter = displayBoardPresenter;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return true;
        }
        if (!player.hasPermission(hostPermission)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }

        String[] tokens = args == null ? new String[0] : args;
        int index = 0;
        if (tokens.length > 0 && "book".equalsIgnoreCase(tokens[0])) {
            index = 1;
        }

        String head =
                index < tokens.length ? (tokens[index] == null ? "" : tokens[index].toLowerCase(Locale.ROOT)) : "";
        String tail = index + 1 < tokens.length
                ? CommandArgs.joinTail(tokens, index + 1).trim()
                : "";

        switch (head) {
            case "map" -> hostBookService.giveMapBook(player);
            case "display" -> {
                if (displayBoardPresenter == null) {
                    messages.error(sender, Msg.NOT_READY);
                    return true;
                }
                List<String> ids = new ArrayList<>(displayBoardPresenter.listBoards());
                hostBookService.giveDisplayBook(player, ids, tail);
            }
            case "lighting" -> hostBookService.giveLightingBook(player);
            case "cleanup" -> hostBookService.giveCleanupBook(player);
            default -> hostBookService.giveSelectorBook(player);
        }
        return true;
    }
}
