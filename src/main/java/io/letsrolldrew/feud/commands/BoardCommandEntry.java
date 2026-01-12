package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.MapWallBinder;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BoardCommandEntry {
    private final DisplayBoardCommands boardCommands;
    private final String adminPermission;
    private final BoardWandService boardWandService;
    private final BoardBindingStore boardBindingStore;
    private final MapIdStore mapIdStore;
    private final TileFramebufferStore framebufferStore;
    private final BoardRenderer boardRenderer;

    public BoardCommandEntry(
            DisplayBoardCommands boardCommands,
            String adminPermission,
            BoardWandService boardWandService,
            BoardBindingStore boardBindingStore,
            MapIdStore mapIdStore,
            TileFramebufferStore framebufferStore,
            BoardRenderer boardRenderer) {
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
            sender.sendMessage("Board commands: map | display ...");
            return true;
        }

        String head = tokens[0].toLowerCase();
        String[] tail = tail(tokens, 1);

        switch (head) {
            case "map":
                return handleMap(sender, tail);
            case "display":
                return boardCommands.handle(sender, tail);
            default:
                sender.sendMessage("Board commands: map | display ...");
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

        sender.sendMessage("Board map commands: /feud board map wand | /feud board map initmaps");
        return true;
    }

    private boolean handleBoardWand(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to set up the board");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the board wand");
            return true;
        }
        boardWandService.giveWand(player);
        sender.sendMessage(
                "Board wand given. Right-click the top-left frame, then right-click the bottom-right frame");
        return true;
    }

    private boolean handleBoardInitMaps(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to set up the board");
            return true;
        }
        var bindingOpt = boardBindingStore.load();
        if (bindingOpt.isEmpty()) {
            sender.sendMessage("No board binding found. Use /feud board map wand first");
            return true;
        }
        MapWallBinder binder = new MapWallBinder(bindingOpt.get(), mapIdStore, framebufferStore);
        boolean ok = binder.bind();
        if (ok) {
            sender.sendMessage("Board maps initialized");
            boardRenderer.paintBase();
            boardRenderer.paintHiddenCovers();
            sender.sendMessage("Board base painted");
        } else {
            sender.sendMessage("Board map init failed (binding missing or world unloaded)");
        }
        return true;
    }

    private static String[] tail(String[] args, int start) {
        if (start >= args.length) {
            return new String[0];
        }
        String[] out = new String[args.length - start];
        System.arraycopy(args, start, out, 0, args.length - start);
        return out;
    }
}
