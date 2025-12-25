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
import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.board.display.DefaultDisplayBoardPresenter;
import io.letsrolldrew.feud.commands.BoardCommands;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;

public final class FeudRootCommand implements CommandExecutor {
    private final Plugin plugin;
    private final SurveyRepository surveyRepository;
    private final String hostPermission;
    private final String adminPermission;
    private final GameController gameController;
    private final HostBookUiBuilder hostBookUiBuilder;
    private final HostRemoteService hostRemoteService;
    private final BoardWandService boardWandService;
    private final BoardBindingStore boardBindingStore;
    private final MapIdStore mapIdStore;
    private final TileFramebufferStore framebufferStore;
    private final BoardRenderer boardRenderer;
    private final SlotRevealPainter slotRevealPainter;
    private final UiCommand uiCommand;
    private final HologramCommands hologramCommands;
    private final BoardCommands boardCommands;

    public FeudRootCommand(
        Plugin plugin,
        SurveyRepository surveyRepository,
        HostBookUiBuilder hostBookUiBuilder,
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
        BoardCommands boardCommands
    ) {
        this.plugin = plugin;
        this.surveyRepository = surveyRepository;
        this.hostBookUiBuilder = hostBookUiBuilder;
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
            String[] remaining = new String[Math.max(0, args.length - 1)];
            if (args.length > 1) {
                System.arraycopy(args, 1, remaining, 0, args.length - 1);
            }
            if (!sender.hasPermission(adminPermission)) {
                sender.sendMessage("You need admin permission to manage holograms.");
                return true;
            }
            return hologramCommands.handle(sender, remaining);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("board")) {
            String[] remaining = new String[Math.max(0, args.length - 1)];
            if (args.length > 1) {
                System.arraycopy(args, 1, remaining, 0, args.length - 1);
            }
            return boardCommands.handle(sender, remaining);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("clear") && args[1].equalsIgnoreCase("all")) {
            return handleClearAll(sender);
        }

        if (args.length >= 1 && args[0].equalsIgnoreCase("ui")) {
            String[] remaining = new String[Math.max(0, args.length - 1)];
            if (args.length > 1) {
                System.arraycopy(args, 1, remaining, 0, args.length - 1);
            }
            return uiCommand.handle(sender, remaining);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("board") && args[1].equalsIgnoreCase("wand")) {
            return handleBoardWand(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("host") && args[1].equalsIgnoreCase("book")) {
            return handleHostBook(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("board") && args[1].equalsIgnoreCase("initmaps")) {
            return handleBoardInitMaps(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("list")) {
            return handleSurveyList(sender);
        }

        if (args.length >= 2 && args[0].equalsIgnoreCase("survey") && args[1].equalsIgnoreCase("load")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /feud survey load <id>");
                return true;
            }
            return handleSurveyLoad(sender, args[2]);
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
        sender.sendMessage("/feud survey list - list loaded surveys");
        sender.sendMessage("/feud survey load <id> - set active survey");
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
        return true;
    }

    private boolean handleSurveyList(CommandSender sender) {
        if (surveyRepository == null) {
            sender.sendMessage("Surveys not loaded.");
            return true;
        }
        if (surveyRepository.listAll().isEmpty()) {
            sender.sendMessage("No surveys loaded.");
            return true;
        }
        sender.sendMessage("Loaded surveys:");
        for (Survey survey : surveyRepository.listAll()) {
            sender.sendMessage("- " + survey.id() + ": " + survey.question());
        }
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

    private boolean handleSurveyLoad(CommandSender sender, String surveyId) {

        if (!sender.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to do that.");
            return true;
        }

        if (surveyRepository == null) {
            sender.sendMessage("Surveys not loaded.");
            return true;
        }

        Survey survey = surveyRepository.findById(surveyId).orElse(null);

        if (survey == null) {
            sender.sendMessage("Survey not found: " + surveyId);
            return true;
        }

        gameController.setActiveSurvey(survey);
        sender.sendMessage("Active survey set to " + survey.displayName() + ": " + survey.question());

        if (sender instanceof Player player) {
            giveOrReplaceHostBook(player);
        }

        return true;
    }

    private boolean handleHostBook(CommandSender sender) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the host book.");
            return true;
        }

        if (!player.hasPermission(hostPermission)) {
            sender.sendMessage("You must be the host to use this.");
            return true;
        }

        giveOrReplaceHostBook(player);
        player.sendMessage("Host remote given.");
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
        sender.sendMessage("Cleared " + removed + " display entities.");
        return true;
    }
}
