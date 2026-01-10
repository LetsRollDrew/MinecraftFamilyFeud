package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.MapWallBinder;
import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.board.display.panels.TimerPanelStore;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.SlotRevealPainter;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.SpecificationDispatcher;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.effects.buzz.BuzzerCommands;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.effects.timer.TimerCommands;
import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.team.TeamCommands;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.ui.BookFactory;
import io.letsrolldrew.feud.ui.DisplayHostRemoteBookBuilder;
import io.letsrolldrew.feud.ui.HostBookAnchorStore;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

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
    private final DisplayBoardService displayBoardPresenter;
    private final SurveyCommands surveyCommands;
    private final TeamCommands teamCommands;
    private final TeamService teamService;
    private final ScorePanelPresenter scorePanelPresenter;
    private final TimerCommands timerCommands;
    private final BuzzerCommands buzzerCommands;
    private final FastMoneyCommands fastMoneyCommands;
    private final DisplayRegistry displayRegistry;
    private final ScorePanelStore scorePanelStore;
    private final TimerPanelStore timerPanelStore;
    private final HostBookAnchorStore hostBookAnchorStore;
    private final SpecificationDispatcher dispatcher;
    private final CommandSpecificationNode commandSpec;

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
            CommandModules commandModules,
            CommandSpecificationNode commandSpec,
            io.letsrolldrew.feud.effects.holo.HologramService hologramService,
            DisplayBoardService displayBoardPresenter,
            TeamService teamService,
            ScorePanelPresenter scorePanelPresenter,
            DisplayRegistry displayRegistry,
            ScorePanelStore scorePanelStore,
            TimerPanelStore timerPanelStore,
            HostBookAnchorStore hostBookAnchorStore) {
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
        this.hologramCommands = commandModules.hologramCommands();
        this.boardCommands = commandModules.displayBoardCommands();
        this.hologramService = hologramService;
        this.displayBoardPresenter = displayBoardPresenter;
        this.surveyCommands = commandModules.surveyCommands();
        this.teamCommands = commandModules.teamCommands();
        this.teamService = teamService;
        this.scorePanelPresenter = scorePanelPresenter;
        this.timerCommands = commandModules.timerCommands();
        this.buzzerCommands = commandModules.buzzerCommands();
        this.fastMoneyCommands = commandModules.fastMoneyCommands();
        this.displayRegistry = displayRegistry;
        this.scorePanelStore = scorePanelStore;
        this.timerPanelStore = timerPanelStore;
        this.hostBookAnchorStore = hostBookAnchorStore;
        this.commandSpec = commandSpec;
        this.dispatcher = new SpecificationDispatcher(commandSpec);
        this.uiCommand = new UiCommand(
                gameController,
                hostPermission,
                player -> giveOrReplaceHostBook(player),
                this::renderReveal,
                teamService,
                scorePanelPresenter,
                scorePanelStore,
                displayBoardPresenter);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SpecificationDispatcher.DispatchResult result = dispatcher.dispatch(sender, label, args);
        if (result.requirementHandled()) {
            return true;
        }
        if (!result.matched()) {
            return handleHelp(sender);
        }

        if (args == null || args.length == 0) {
            return handleVersion(sender);
        }

        String head = args[0].toLowerCase();
        String[] tail = tail(args, 1);

        switch (head) {
            case "help":
                return handleHelp(sender);
            case "version":
                return handleVersion(sender);
            case "ui":
                return uiCommand.handle(sender, tail);
            case "holo":
                return hologramCommands.handle(sender, tail);
            case "board":
                return handleBoard(sender, args);
            case "survey":
                return surveyCommands.handle(sender, tail);
            case "team":
                return teamCommands.handle(sender, tail);
            case "buzz":
                return buzzerCommands.handleBuzzReset(sender);
            case "timer":
                return timerCommands.handle(sender, tail);
            case "fastmoney":
                return fastMoneyCommands.handle(sender, tail);
            case "host":
                return handleHostArgs(sender, args);
            case "clear":
                return handleClearArgs(sender, args);
            default:
                return handleHelp(sender);
        }
    }

    public int dispatchFromBrigadier(CommandSourceStack source, java.util.List<String> args) {
        CommandSender sender = source.getSender();
        boolean handled = onCommand(sender, null, "feud", args.toArray(new String[0]));
        return handled ? 1 : 0;
    }

    private boolean handleClearArgs(CommandSender sender, String[] args) {
        if (args.length >= 2 && "all".equalsIgnoreCase(args[1])) {
            return handleClearAll(sender);
        }
        sender.sendMessage("Usage: /feud clear all");
        return true;
    }

    private boolean handleHostArgs(CommandSender sender, String[] args) {
        if (args.length >= 2 && "book".equalsIgnoreCase(args[1])) {
            String flavor = args.length >= 3 ? joinTail(args, 2) : "";
            return handleHostBook(sender, flavor);
        }
        sender.sendMessage("Usage: /feud host book [map|display|cleanup]");
        return true;
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
        sender.sendMessage("/feud team info - show teams");
        sender.sendMessage("/feud team reset - reset teams");
        sender.sendMessage("/feud team set <red|blue> name <new-name...>");
        sender.sendMessage("/feud team buzzer bind|clear|test <red|blue>");
        sender.sendMessage("/feud buzz reset");
        sender.sendMessage("/feud host book - give host remote");
        sender.sendMessage("/feud ui reveal <1-8> - reveal slot");
        sender.sendMessage("/feud ui strike - add a strike");
        sender.sendMessage("/feud ui clearstrikes - clear strikes");
        sender.sendMessage("/feud ui add <points> - add points to round");
        sender.sendMessage("/feud board wand - get Display Selector (admin)");
        sender.sendMessage("/feud board initmaps - assign maps to board frames (admin)");
        sender.sendMessage("/feud holo text spawn|set|move|remove ...");
        sender.sendMessage("/feud holo item spawn|move|remove ...");
        sender.sendMessage("/feud holo list");
        sender.sendMessage("/feud clear all - remove all display entities");
        sender.sendMessage("/feud host book cleanup - cleanup remote");
        sender.sendMessage("/feud timer start|stop|reset|status");
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
        sender.sendMessage(
                "Board wand given. Right-click the top-left frame, then right-click the bottom-right frame.");
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
        String raw = flavor == null ? "" : flavor.trim();
        String head = raw.isBlank() ? "" : raw.split("\\s+", 2)[0].toLowerCase();
        String tail = raw.isBlank() ? "" : raw.replaceFirst("^\\S+\\s*", "");

        switch (head) {
            case "map" -> giveMapBook(player);
            case "display" -> giveDisplayBook(player, tail);
            case "cleanup" -> giveCleanupBook(player);
            default -> giveSelectorBook(player);
        }
        return true;
    }

    private void giveOrReplaceHostBook(Player player) {
        var fresh = hostBookUiBuilder.createBookFor(
                player,
                gameController.slotHoverTexts(),
                gameController.getActiveSurvey(),
                gameController.revealedSlots(),
                gameController.strikeCount(),
                gameController.maxStrikes(),
                gameController.roundPoints(),
                gameController.controllingTeam());
        hostRemoteService.giveOrReplace(player, fresh);
    }

    private boolean handleEntityBook(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the entity book.");
            return true;
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        try {
            meta.title(Component.text("Cleanup Remote", NamedTextColor.GRAY));
            meta.author(Component.text("FamilyFeud", NamedTextColor.GRAY));
        } catch (Throwable ignored) {
            meta.setTitle("Cleanup Remote");
            meta.setAuthor("FamilyFeud");
        }
        Component page1 = Component.text()
                .append(button("Board Create (demo)", "/feud board create demo"))
                .append(Component.newline())
                .append(button("Board Destroy (demo)", "/feud board destroy demo"))
                .append(Component.newline())
                .append(button("Board Wand", "/feud board wand"))
                .append(Component.newline())
                .append(button("Board InitMaps", "/feud board initmaps"))
                .build();
        Component page2 = Component.text()
                .append(button("Holo Text Spawn", "/feud holo text spawn demo &fHELLO"))
                .append(Component.newline())
                .append(button("Holo Item Spawn", "/feud holo item spawn demo 9001"))
                .append(Component.newline())
                .append(button("Clear Displays", "/feud clear all"))
                .build();
        Book adventureBook = BookFactory.create(
                Component.text("Cleanup Remote"), Component.text("FamilyFeud"), java.util.List.of(page1, page2));
        meta.pages(adventureBook.pages());
        if (hostBookUiBuilder != null) {
            io.letsrolldrew.feud.ui.BookTagger.tagHostRemote(meta, hostBookUiBuilder.getHostKey());
        }
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage("Cleanup book given.");
        return true;
    }

    private void giveCleanupBook(Player player) {
        handleEntityBook(player);
    }

    private Component button(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD).clickEvent(ClickEvent.runCommand(command));
    }

    private Component buttonUnderlined(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD)
                .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(command));
    }

    private boolean handleClearAll(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("Admin only");
            return true;
        }

        int removed = displayRegistry.removeAll();
        removed += removeTaggedDisplays("board");

        displayBoardPresenter.clearAll();
        hologramService.clearAll();
        if (scorePanelStore != null) {
            scorePanelStore.clear();
        }
        if (timerPanelStore != null) {
            timerPanelStore.clear();
        }

        sender.sendMessage("Cleared " + removed + " displays");
        return true;
    }

    private int removeTaggedDisplays(String kind) {
        int removed = 0;
        for (var world : plugin.getServer().getWorlds()) {
            for (org.bukkit.entity.Display display : world.getEntitiesByClass(org.bukkit.entity.Display.class)) {
                if (DisplayTags.isManaged(display, kind)) {
                    display.remove();
                    removed++;
                }
            }
        }
        return removed;
    }

    private void giveSelectorBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Remote Selector");
        meta.setAuthor("FamilyFeud");
        Component page = Component.text()
                .append(Component.text("Select Board Remote:", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Map Board Remote", "/feud host book map"))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Display Board Remote", "/feud host book display"))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Cleanup Remote", "/feud host book cleanup"))
                .build();
        Book adventureBook = BookFactory.create(
                Component.text("Host Remote"), Component.text("FamilyFeud"), java.util.List.of(page));
        meta.pages(adventureBook.pages());
        if (hostBookUiBuilder != null) {
            io.letsrolldrew.feud.ui.BookTagger.tagHostRemote(meta, hostBookUiBuilder.getHostKey());
        }
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
        player.sendMessage("Host remote selector given.");
    }

    private void giveMapBook(Player player) {
        var fresh = hostBookUiBuilder.createBookFor(
                player,
                gameController.slotHoverTexts(),
                gameController.getActiveSurvey(),
                gameController.revealedSlots(),
                gameController.strikeCount(),
                gameController.maxStrikes(),
                gameController.roundPoints(),
                gameController.controllingTeam());
        if (fresh.getItemMeta() instanceof BookMeta meta) {
            meta.lore(java.util.List.of(Component.text("Map Based", NamedTextColor.GRAY)));
            try {
                meta.title(Component.text("Feud Host Book", NamedTextColor.GOLD));
                meta.author(Component.text("Family Feud", NamedTextColor.GOLD));
            } catch (Throwable ignored) {
                meta.setTitle("Feud Host Book");
                meta.setAuthor("Family Feud");
            }
            fresh.setItemMeta(meta);
        }
        hostRemoteService.giveOrReplace(player, fresh);
        player.sendMessage("Map board remote given.");
    }

    private void giveDisplayBook(Player player) {
        giveDisplayBook(player, "");
    }

    private void giveDisplayBook(Player player, String boardId) {
        java.util.List<String> ids = new java.util.ArrayList<>(displayBoardPresenter.listBoards());
        java.util.Collections.sort(ids);

        String target = boardId == null ? "" : boardId.trim();
        if (target.isBlank() && !ids.isEmpty()) {
            target = ids.get(0);
        }

        ItemStack fresh = DisplayHostRemoteBookBuilder.create(
                target, ids, surveyRepository, hostBookUiBuilder.getHostKey(), gameController);
        hostRemoteService.giveOrReplace(player, fresh);
        player.sendMessage(ids.isEmpty() ? "Display remote (no boards yet)" : "Display remote: " + target);
    }

    private static String[] tail(String[] args, int start) {
        if (start >= args.length) {
            return new String[0];
        }
        String[] out = new String[args.length - start];
        System.arraycopy(args, start, out, 0, args.length - start);
        return out;
    }

    private static String joinTail(String[] args, int start) {
        if (args == null || start >= args.length) {
            return "";
        }
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }
}
