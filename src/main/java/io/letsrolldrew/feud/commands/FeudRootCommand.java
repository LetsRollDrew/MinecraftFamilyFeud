package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.MapWallBinder;
import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.SlotRevealPainter;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.commands.SurveyCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class FeudRootCommand implements CommandExecutor {
    private final Plugin plugin;
    private final SurveyRepository surveyRepository;
    private final String hostPermission;
    private final String adminPermission;
    private final GameController gameController;
    private final HostBookUiBuilder hostBookUiBuilder;
    private final HostBookUiBuilder displayHostBookUiBuilder;
    private final HostRemoteService hostRemoteService;
    private final BoardWandService boardWandService;
    private final BoardBindingStore boardBindingStore;
    private final MapIdStore mapIdStore;
    private final TileFramebufferStore framebufferStore;
    private final BoardRenderer boardRenderer;
    private final SlotRevealPainter slotRevealPainter;
    private final UiCommand uiCommand;
    private final HologramCommands hologramCommands;
    private final DisplayBoardCommands boardCommands;
    private final io.letsrolldrew.feud.effects.holo.HologramService hologramService;
    private final io.letsrolldrew.feud.board.display.DisplayBoardPresenter displayBoardPresenter;
    private final SurveyCommands surveyCommands;

    public FeudRootCommand(
        Plugin plugin,
        SurveyRepository surveyRepository,
        HostBookUiBuilder hostBookUiBuilder,
        HostBookUiBuilder displayHostBookUiBuilder,
        HostRemoteService hostRemoteService,
        String hostPermission,
        String adminPermission,
        GameController gameController,
        BoardWandService boardWandService,
        BoardBindingStore boardBindingStore,
        MapIdStore mapIdStore,
        TileFramebufferStore framebufferStore,
        BoardRenderer boardRenderer,
        SlotRevealPainter slotRevealPainter,
        HologramCommands hologramCommands,
        DisplayBoardCommands boardCommands,
        io.letsrolldrew.feud.effects.holo.HologramService hologramService,
        io.letsrolldrew.feud.board.display.DisplayBoardPresenter displayBoardPresenter,
        SurveyCommands surveyCommands
    ) {
        this.plugin = plugin;
        this.surveyRepository = surveyRepository;
        this.hostBookUiBuilder = hostBookUiBuilder;
        this.displayHostBookUiBuilder = displayHostBookUiBuilder;
        this.hostRemoteService = hostRemoteService;
        this.hostPermission = hostPermission;
        this.adminPermission = adminPermission;
        this.gameController = gameController;
        this.boardWandService = boardWandService;
        this.boardBindingStore = boardBindingStore;
        this.mapIdStore = mapIdStore;
        this.framebufferStore = framebufferStore;
        this.boardRenderer = boardRenderer;
        this.slotRevealPainter = slotRevealPainter;
        this.hologramCommands = hologramCommands;
        this.boardCommands = boardCommands;
        this.hologramService = hologramService;
        this.displayBoardPresenter = displayBoardPresenter;
        this.surveyCommands = surveyCommands;
        this.uiCommand = new UiCommand(gameController, hostPermission, player -> giveOrReplaceHostBook(player), this::renderReveal);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return handleVersion(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("help")) {
            return handleHelp(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("version")) {
            return handleVersion(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("holo")) {
            String[] remaining = tail(args, 1);
            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage("You need admin permission to manage holograms.");
                return true;
            }
            return hologramCommands.handle(sender, remaining);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("clear") && args[1].equalsIgnoreCase("all")) {
            return handleClearAll(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("ui")) {
            return uiCommand.handle(sender, tail(args, 1));
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("host") && args[1].equalsIgnoreCase("book")) {
            String flavor = args.length >= 3 ? args[2].toLowerCase() : "";
            return handleHostBook(sender, flavor);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("entity") && args[1].equalsIgnoreCase("book")) {
            return handleEntityBook(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("board")) {
            return handleBoard(sender, args);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("survey")) {
            return surveyCommands.handle(sender, tail(args, 1));
        }

        return handleHelp(sender);
    }

    private void renderReveal(int slot) {
        var survey = gameController.getActiveSurvey();
        if (survey == null) {
            return;
        }
        if (slot < 1 || slot > survey.answers().size()) {
            return;
        }
        var answer = survey.answers().get(slot - 1);
        slotRevealPainter.reveal(slot, answer.text(), answer.points());
    }

    private boolean handleBoard(CommandSender sender, String[] args) {
        if (args.length >= 2 && args[1].equalsIgnoreCase("map")) {
            if (args.length >= 3 && args[2].equalsIgnoreCase("wand")) {
                return handleBoardWand(sender);
            }
            if (args.length >= 3 && args[2].equalsIgnoreCase("initmaps")) {
                return handleBoardInitMaps(sender);
            }
            sender.sendMessage("Board map commands: /feud board map wand | /feud board map initmaps");
            return true;
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase("display")) {
            return boardCommands.handle(sender, tail(args, 2));
        }
        sender.sendMessage("Board commands: /feud board map ... | /feud board display ...");
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

    @SuppressWarnings("deprecation") // Plugin#getDescription is deprecated, fix later
    private boolean handleVersion(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        sender.sendMessage("FamilyFeud v" + version + " - game state: not started");
        sender.sendMessage("Use /feud help for commands.");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage("FamilyFeud commands:");
        sender.sendMessage("/feud - show version");
        sender.sendMessage("/feud help - this help");
        sender.sendMessage("/feud version - show version");
        sender.sendMessage("/feud survey ...");
        sender.sendMessage("/feud host book - give host remote");
        sender.sendMessage("/feud ui reveal <1-8> - reveal slot");
        sender.sendMessage("/feud ui strike - add a strike");
        sender.sendMessage("/feud ui clearstrikes - clear strikes");
        sender.sendMessage("/feud ui add <points> - add points to round");
        sender.sendMessage("/feud board wand - get board setup wand (admin)");
        sender.sendMessage("/feud board initmaps - assign maps to board frames (admin)");
        sender.sendMessage("/feud holo text spawn|set|move|remove ...");
        sender.sendMessage("/feud holo item spawn|move|remove ...");
        sender.sendMessage("/feud holo list");
        sender.sendMessage("/feud clear all - remove all display entities");
        sender.sendMessage("/feud entity book - dev book with entity commands");
        return true;
    }

    private boolean handleBoardWand(CommandSender sender) {

        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to set up the board.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the board wand.");
            return true;
        }
        boardWandService.giveWand(player);
        sender.sendMessage("Board wand given. Right-click the top-left frame, then right-click the bottom-right frame.");
        return true;
    }

    private boolean handleBoardInitMaps(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to set up the board.");
            return true;
        }
        var bindingOpt = boardBindingStore.load();
        if (bindingOpt.isEmpty()) {
            sender.sendMessage("No board binding found. Use /feud board wand first.");
            return true;
        }
        MapWallBinder binder = new MapWallBinder(bindingOpt.get(), mapIdStore, framebufferStore);
        boolean ok = binder.bind();
        if (ok) {
            sender.sendMessage("Board maps initialized.");
            boardRenderer.paintBase();
            boardRenderer.paintHiddenCovers();
            sender.sendMessage("Board base painted.");
        } else {
            sender.sendMessage("Board map init failed (binding missing or world unloaded).");
        }
        return true;
    }

    private boolean handleHostBook(CommandSender sender, String flavor) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the host book.");
            return true;
        }
        if (!player.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to use this.");
            return true;
        }
        switch (flavor) {
            case "map" -> giveMapBook(player);
            case "display" -> giveDisplayBook(player);
            default -> giveSelectorBook(player);
        }
        return true;
    }

    private void giveOrReplaceHostBook(Player player) {
        var fresh = hostBookUiBuilder.createBook(
            gameController.slotHoverTexts(),
            gameController.getActiveSurvey(),
            gameController.revealedSlots(),
            gameController.strikeCount(),
            gameController.maxStrikes(),
            gameController.roundPoints(),
            gameController.controllingTeam()
        );
        hostRemoteService.giveOrReplace(player, fresh);
    }

    private boolean handleEntityBook(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the entity book.");
            return true;
        }
        if (!player.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to use this.");
            return true;
        }
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Entity Remote");
        meta.setAuthor("FamilyFeud");
        var page1 = Component.text()
            .append(button("Board Create (demo)", "/feud board create demo"))
            .append(Component.newline())
            .append(button("Board Destroy (demo)", "/feud board destroy demo"))
            .append(Component.newline())
            .append(button("Board Wand", "/feud board wand"))
            .append(Component.newline())
            .append(button("Board InitMaps", "/feud board initmaps"))
            .build();
        var page2 = Component.text()
            .append(button("Holo Text Spawn", "/feud holo text spawn demo &fHELLO"))
            .append(Component.newline())
            .append(button("Holo Item Spawn", "/feud holo item spawn demo 9001"))
            .append(Component.newline())
            .append(button("Clear Displays", "/feud clear all"))
            .build();
        meta.pages(page1, page2);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage("Entity book given.");
        return true;
    }

    private Component button(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD)
            .clickEvent(ClickEvent.runCommand(command));
    }

    private Component buttonUnderlined(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD)
            .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
            .clickEvent(ClickEvent.runCommand(command));
    }

    private boolean handleClearAll(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("You need admin permission to clear display entities.");
            return true;
        }
        int removed = 0;
        for (var world : Bukkit.getWorlds()) {
            for (var entity : world.getEntities()) {
                if (entity instanceof ItemDisplay || entity instanceof TextDisplay) {
                    entity.remove();
                    removed++;
                }
            }
        }
        displayBoardPresenter.clearAll();
        hologramService.clearAll();
        sender.sendMessage("Cleared " + removed + " display entities.");
        return true;
    }

    private void giveSelectorBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Host Remote");
        meta.setAuthor("FamilyFeud");
        var page = Component.text()
            .append(Component.text("Select Board Remote:", NamedTextColor.GOLD))
            .append(Component.newline()).append(Component.newline())
            .append(buttonUnderlined("Map Board Remote", "/feud host book map"))
            .append(Component.newline()).append(Component.newline())
            .append(buttonUnderlined("Display Board Remote", "/feud host book display"))
            .build();
        meta.pages(page);
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage("Host remote selector given.");
    }

    private void giveMapBook(Player player) {
        var fresh = hostBookUiBuilder.createBook(
            gameController.slotHoverTexts(),
            gameController.getActiveSurvey(),
            gameController.revealedSlots(),
            gameController.strikeCount(),
            gameController.maxStrikes(),
            gameController.roundPoints(),
            gameController.controllingTeam()
        );
        if (fresh.getItemMeta() instanceof BookMeta meta) {
            meta.lore(java.util.List.of(Component.text("Map Based", NamedTextColor.GRAY)));
            fresh.setItemMeta(meta);
        }
        hostRemoteService.giveOrReplace(player, fresh);
        player.sendMessage("Map board remote given.");
    }

    private void giveDisplayBook(Player player) {
        var fresh = displayHostBookUiBuilder.createBook(
            gameController.slotHoverTexts(),
            gameController.getActiveSurvey(),
            gameController.revealedSlots(),
            gameController.strikeCount(),
            gameController.maxStrikes(),
            gameController.roundPoints(),
            gameController.controllingTeam()
        );
        if (fresh.getItemMeta() instanceof BookMeta meta) {
            meta.lore(java.util.List.of(Component.text("Display Based", NamedTextColor.GRAY)));
            fresh.setItemMeta(meta);
        }
        hostRemoteService.giveOrReplace(player, fresh);
        player.sendMessage("Display board remote given.");
    }
}
