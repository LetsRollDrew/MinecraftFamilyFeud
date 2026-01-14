package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.MapWallBinder;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BoardCommandEntry {
    private final Messages messages;
    private final DisplayBoardCommands boardCommands;
    private final String adminPermission;
    private final BoardWandService boardWandService;
    private final BoardBindingStore boardBindingStore;
    private final MapIdStore mapIdStore;
    private final TileFramebufferStore framebufferStore;
    private final BoardRenderer boardRenderer;

    public BoardCommandEntry(
            Messages messages,
            DisplayBoardCommands boardCommands,
            String adminPermission,
            BoardWandService boardWandService,
            BoardBindingStore boardBindingStore,
            MapIdStore mapIdStore,
            TileFramebufferStore framebufferStore,
            BoardRenderer boardRenderer) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.boardCommands = boardCommands;
        this.adminPermission = adminPermission;
        this.boardWandService = boardWandService;
        this.boardBindingStore = boardBindingStore;
        this.mapIdStore = mapIdStore;
        this.framebufferStore = framebufferStore;
        this.boardRenderer = boardRenderer;
    }

    public boolean handle(CommandSender sender, String[] args) {
        String[] tokens = args == null ? new String[0] : args;
        if (tokens.length == 0) {
            messages.usage(sender, Msg.BOARD_ENTRY_HELP);
            return true;
        }

        String head = tokens[0].toLowerCase();
        String[] tail = CommandArgs.tail(tokens, 1);

        switch (head) {
            case "map":
                return handleMap(sender, tail);
            case "display":
                return boardCommands.handle(sender, tail);
            default:
                messages.usage(sender, Msg.BOARD_ENTRY_HELP);
                return true;
        }
    }

    private boolean handleMap(CommandSender sender, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("wand")) {
            return handleBoardWand(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("initmaps")) {
            return handleBoardInitMaps(sender);
        }

        messages.usage(sender, Msg.BOARD_MAP_HELP);
        return true;
    }

    private boolean handleBoardWand(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            messages.error(sender, Msg.NEED_PERMISSION, Placeholder.of("permission", adminPermission));
            return true;
        }

        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return true;
        }
        boardWandService.giveWand(player);
        messages.success(sender, Msg.BOARD_WAND_GIVEN);
        return true;
    }

    private boolean handleBoardInitMaps(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            messages.error(sender, Msg.NEED_PERMISSION, Placeholder.of("permission", adminPermission));
            return true;
        }
        var bindingOpt = boardBindingStore.load();
        if (bindingOpt.isEmpty()) {
            messages.error(sender, Msg.BOARD_BINDING_MISSING);
            return true;
        }
        MapWallBinder binder = new MapWallBinder(bindingOpt.get(), mapIdStore, framebufferStore);
        boolean ok = binder.bind();
        if (ok) {
            messages.success(sender, Msg.BOARD_MAPS_INITIALIZED);
            boardRenderer.paintBase();
            boardRenderer.paintHiddenCovers();
            messages.success(sender, Msg.BOARD_BASE_PAINTED);
        } else {
            messages.error(sender, Msg.BOARD_MAP_INIT_FAILED);
        }
        return true;
    }
}
