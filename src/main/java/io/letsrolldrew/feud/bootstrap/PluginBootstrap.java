package io.letsrolldrew.feud.bootstrap;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.DirtyTracker;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.commands.DisplayBoardCommands;
import io.letsrolldrew.feud.commands.FeudRootCommand;
import io.letsrolldrew.feud.commands.SurveyCommands;
import io.letsrolldrew.feud.config.PluginConfig;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionListener;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.effects.holo.HologramService;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.SimpleGameController;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.team.TeamCommands;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.File;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginBootstrap {
    private final JavaPlugin plugin;
    private PluginConfig config;
    private SurveyRepository surveyRepository;
    private GameController gameController;
    private HostBookUiBuilder hostBookUiBuilder;
    private HostBookUiBuilder displayHostBookUiBuilder;
    private HostRemoteService hostRemoteService;
    private BoardWandService boardWandService;
    private BoardBindingStore boardBindingStore;
    private TileFramebufferStore framebufferStore;
    private MapIdStore mapIdStore;
    private DirtyTracker dirtyTracker;
    private BoardRenderer boardRenderer;
    private io.letsrolldrew.feud.board.render.SlotRevealPainter slotRevealPainter;
    private HologramService hologramService;
    private HologramCommands hologramCommands;
    private SurveyCommands surveyCommands;
    private DisplayBoardPresenter displayBoardPresenter;
    private DisplayBoardCommands boardCommands;
    private DisplayRegistry displayRegistry;
    private io.letsrolldrew.feud.effects.anim.AnimationService animationService;
    private FeudRootCommand feudRootCommand;
    private DisplayBoardSelectionStore displayBoardSelectionStore;
    private DisplayBoardSelectionListener displayBoardSelectionListener;
    private TeamService teamService;
    private TeamCommands teamCommands;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        this.surveyRepository = SurveyRepository.load(plugin.getConfig());
        this.gameController = new SimpleGameController(config.maxStrikes());
        this.teamService = new TeamService();
        this.teamCommands = new TeamCommands(teamService, config.hostPermission(), "familyfeud.admin");
        NamespacedKey hostKey = new NamespacedKey(plugin, "host_remote");
        this.hostBookUiBuilder = new HostBookUiBuilder("/feud ui", surveyRepository, null, hostKey);
        this.displayHostBookUiBuilder = new HostBookUiBuilder("/feud board display", surveyRepository, null, hostKey);
        this.hostRemoteService = new HostRemoteService(plugin, hostKey, false);
        NamespacedKey wandKey = new NamespacedKey(plugin, "board_wand");
        this.boardBindingStore = new BoardBindingStore(plugin);
        this.boardWandService = new BoardWandService(plugin, wandKey, boardBindingStore);
        this.framebufferStore = new TileFramebufferStore();
        this.mapIdStore = new MapIdStore(new java.io.File(plugin.getDataFolder(), "map-ids.yml"));
        this.dirtyTracker = new DirtyTracker();
        File displayStore = new File(plugin.getDataFolder(), "displays.yml");
        this.displayRegistry =
                new DisplayRegistry(new io.letsrolldrew.feud.display.lookup.BukkitEntityLookup(), displayStore);
        this.animationService = new io.letsrolldrew.feud.effects.anim.AnimationService(
                new io.letsrolldrew.feud.effects.anim.BukkitScheduler(plugin));
        this.boardRenderer = new BoardRenderer(framebufferStore, dirtyTracker);
        this.slotRevealPainter =
                new io.letsrolldrew.feud.board.render.SlotRevealPainter(framebufferStore, dirtyTracker, boardRenderer);
        File hologramStore = new File(plugin.getDataFolder(), "holograms.yml");
        this.hologramService = new HologramService(displayRegistry, hologramStore);
        this.hologramCommands = new HologramCommands(hologramService);
        this.surveyCommands = new SurveyCommands(surveyRepository, config.hostPermission(), gameController);
        File dynamicBoardsFile = new File(plugin.getDataFolder(), "dynamic-boards.yml");
        this.displayBoardPresenter = new DisplayBoardService(displayRegistry, animationService, dynamicBoardsFile);
        this.displayBoardSelectionStore = new DisplayBoardSelectionStore();
        NamespacedKey displayWandKey = new NamespacedKey(plugin, "display_board_wand");
        this.displayBoardSelectionListener =
                new DisplayBoardSelectionListener(plugin, displayWandKey, displayBoardSelectionStore);
        this.boardCommands = new DisplayBoardCommands(
                displayBoardPresenter,
                "familyfeud.admin",
                displayBoardSelectionListener,
                displayBoardSelectionStore,
                gameController,
                config.hostPermission(),
                hostRemoteService,
                surveyRepository,
                hostKey);
        plugin.getServer().getPluginManager().registerEvents(boardWandService, plugin);
        plugin.getServer().getPluginManager().registerEvents(displayBoardSelectionListener, plugin);
        registerCommands();
    }

    public void disable() {}

    public PluginConfig getConfig() {
        return config;
    }

    public SurveyRepository getSurveyRepository() {
        return surveyRepository;
    }

    public GameController getGameController() {
        return gameController;
    }

    private void registerCommands() {
        PluginCommand feud = plugin.getCommand("feud");
        if (feud == null) {
            throw new IllegalStateException("Command 'feud' not defined in plugin.yml");
        }
        feudRootCommand = new FeudRootCommand(
                plugin,
                surveyRepository,
                hostBookUiBuilder,
                displayHostBookUiBuilder,
                hostRemoteService,
                config.hostPermission(),
                "familyfeud.admin",
                gameController,
                boardWandService,
                boardBindingStore,
                mapIdStore,
                framebufferStore,
                boardRenderer,
                slotRevealPainter,
                hologramCommands,
                boardCommands,
                hologramService,
                displayBoardPresenter,
                surveyCommands,
                teamCommands,
                displayRegistry);
        feud.setExecutor(feudRootCommand);
        registerBrigadier(feudRootCommand);
    }

    private void registerBrigadier(FeudRootCommand command) {
        try {
            LifecycleEventManager<?> lifecycle = plugin.getLifecycleManager();
            lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                final String adminPermission = "familyfeud.admin";
                LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.<CommandSourceStack>literal(
                                "feud")
                        .executes(ctx -> exec(command, ctx.getSource()))
                        .then(literal("help").executes(ctx -> exec(command, ctx.getSource(), "help")))
                        .then(literal("version").executes(ctx -> exec(command, ctx.getSource(), "version")))
                        .then(literal("ui")
                                .then(literal("reveal")
                                        .then(wordArg("slot")
                                                .executes(ctx -> exec(
                                                        command,
                                                        ctx.getSource(),
                                                        "ui",
                                                        "reveal",
                                                        StringArgumentType.getString(ctx, "slot"))))
                                        .executes(ctx -> exec(command, ctx.getSource(), "ui", "reveal")))
                                .then(literal("strike").executes(ctx -> exec(command, ctx.getSource(), "ui", "strike")))
                                .then(literal("clearstrikes")
                                        .executes(ctx -> exec(command, ctx.getSource(), "ui", "clearstrikes")))
                                .then(literal("add")
                                        .then(wordArg("points")
                                                .executes(ctx -> exec(
                                                        command,
                                                        ctx.getSource(),
                                                        "ui",
                                                        "add",
                                                        StringArgumentType.getString(ctx, "points"))))
                                        .executes(ctx -> exec(command, ctx.getSource(), "ui", "add")))
                                .then(literal("control")
                                        .then(wordArg("team")
                                                .executes(ctx -> exec(
                                                        command,
                                                        ctx.getSource(),
                                                        "ui",
                                                        "control",
                                                        StringArgumentType.getString(ctx, "team"))))
                                        .executes(ctx -> exec(command, ctx.getSource(), "ui", "control")))
                                .then(literal("award").executes(ctx -> exec(command, ctx.getSource(), "ui", "award")))
                                .then(literal("reset").executes(ctx -> exec(command, ctx.getSource(), "ui", "reset")))
                                .executes(ctx -> exec(command, ctx.getSource(), "ui")))
                        .then(literal("holo")
                                .requires(src -> src.getSender().hasPermission(adminPermission))
                                .then(literal("text")
                                        .then(greedyArgs("rest", command, "holo", "text"))
                                        .executes(ctx -> exec(command, ctx.getSource(), "holo", "text")))
                                .then(literal("item")
                                        .then(greedyArgs("rest", command, "holo", "item"))
                                        .executes(ctx -> exec(command, ctx.getSource(), "holo", "item")))
                                .then(literal("list").executes(ctx -> exec(command, ctx.getSource(), "holo", "list")))
                                .executes(ctx -> exec(command, ctx.getSource(), "holo")))
                        .then(literal("board")
                                .requires(src -> src.getSender().hasPermission(adminPermission))
                                .then(literal("map")
                                        .then(literal("wand")
                                                .executes(
                                                        ctx -> exec(command, ctx.getSource(), "board", "map", "wand")))
                                        .then(literal("initmaps")
                                                .executes(ctx ->
                                                        exec(command, ctx.getSource(), "board", "map", "initmaps")))
                                        .executes(ctx -> exec(command, ctx.getSource(), "board", "map")))
                                .then(literal("display")
                                        .then(greedyArgs("rest", command, "board", "display"))
                                        .executes(ctx -> exec(command, ctx.getSource(), "board", "display")))
                                .executes(ctx -> exec(command, ctx.getSource(), "board")))
                        .then(literal("survey")
                                .then(literal("list").executes(ctx -> exec(command, ctx.getSource(), "survey", "list")))
                                .then(literal("load")
                                        .then(wordArg("id")
                                                .executes(ctx -> exec(
                                                        command,
                                                        ctx.getSource(),
                                                        "survey",
                                                        "load",
                                                        StringArgumentType.getString(ctx, "id"))))
                                        .executes(ctx -> exec(command, ctx.getSource(), "survey", "load")))
                                .executes(ctx -> exec(command, ctx.getSource(), "survey")))
                        .then(literal("team")
                                .then(greedyArgs("rest", command, "team"))
                                .executes(ctx -> exec(command, ctx.getSource(), "team")))
                        .then(literal("host")
                                .then(literal("book")
                                        .requires(src -> src.getSender().hasPermission(config.hostPermission()))
                                        .then(literal("map")
                                                .executes(ctx -> exec(command, ctx.getSource(), "host", "book", "map")))
                                        .then(literal("display")
                                                .then(wordArg("id")
                                                        .executes(ctx -> exec(
                                                                command,
                                                                ctx.getSource(),
                                                                "host",
                                                                "book",
                                                                "display",
                                                                StringArgumentType.getString(ctx, "id"))))
                                                .executes(ctx ->
                                                        exec(command, ctx.getSource(), "host", "book", "display")))
                                        .then(literal("cleanup")
                                                .executes(ctx ->
                                                        exec(command, ctx.getSource(), "host", "book", "cleanup")))
                                        .executes(ctx -> exec(command, ctx.getSource(), "host", "book"))))
                        .then(literal("clear")
                                .requires(src -> src.getSender().hasPermission(adminPermission))
                                .then(literal("all").executes(ctx -> exec(command, ctx.getSource(), "clear", "all"))));

                event.registrar().register(root.build());
            });
        } catch (Throwable ex) {
            plugin.getLogger().fine("Skipping Brigadier registration: " + ex.getMessage());
        }
    }

    private LiteralArgumentBuilder<CommandSourceStack> literal(String name) {
        return LiteralArgumentBuilder.<CommandSourceStack>literal(name);
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> wordArg(String name) {
        return RequiredArgumentBuilder.<CommandSourceStack, String>argument(name, StringArgumentType.word());
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> greedyStringArg(String name) {
        return RequiredArgumentBuilder.<CommandSourceStack, String>argument(name, StringArgumentType.greedyString());
    }

    private RequiredArgumentBuilder<CommandSourceStack, String> greedyArgs(
            String name, FeudRootCommand command, String... head) {
        return greedyStringArg(name).executes(ctx -> {
            String raw = StringArgumentType.getString(ctx, name);
            String[] args = mergeArgs(raw, head);
            return exec(command, ctx.getSource(), args);
        });
    }

    private int exec(FeudRootCommand command, CommandSourceStack source, String... args) {
        boolean handled = command.onCommand(source.getSender(), null, "feud", args);
        return handled ? 1 : 0;
    }

    private String[] mergeArgs(String raw, String... head) {
        if (raw == null || raw.isBlank()) {
            return head;
        }
        String[] tail = raw.split(" ");
        String[] combined = new String[head.length + tail.length];
        System.arraycopy(head, 0, combined, 0, head.length);
        System.arraycopy(tail, 0, combined, head.length, tail.length);
        return combined;
    }
}
