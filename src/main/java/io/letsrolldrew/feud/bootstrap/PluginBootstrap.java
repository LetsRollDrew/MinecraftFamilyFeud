package io.letsrolldrew.feud.bootstrap;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.letsrolldrew.feud.board.*;
import io.letsrolldrew.feud.board.display.*;
import io.letsrolldrew.feud.board.display.fastmoney.*;
import io.letsrolldrew.feud.board.display.panels.*;
import io.letsrolldrew.feud.board.render.*;
import io.letsrolldrew.feud.commands.*;
import io.letsrolldrew.feud.commands.brigadier.*;
import io.letsrolldrew.feud.commands.spec.*;
import io.letsrolldrew.feud.config.*;
import io.letsrolldrew.feud.display.*;
import io.letsrolldrew.feud.effects.anim.*;
import io.letsrolldrew.feud.effects.board.selection.*;
import io.letsrolldrew.feud.effects.buzz.*;
import io.letsrolldrew.feud.effects.fastmoney.*;
import io.letsrolldrew.feud.effects.holo.*;
import io.letsrolldrew.feud.effects.timer.*;
import io.letsrolldrew.feud.fastmoney.*;
import io.letsrolldrew.feud.game.*;
import io.letsrolldrew.feud.survey.*;
import io.letsrolldrew.feud.team.*;
import io.letsrolldrew.feud.ui.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
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
    private DisplayBoardService displayBoardPresenter;
    private DisplayBoardCommands boardCommands;
    private DisplayRegistry displayRegistry;
    private io.letsrolldrew.feud.effects.anim.AnimationService animationService;
    private FeudRootCommand feudRootCommand;
    private DisplayBoardSelectionStore displayBoardSelectionStore;
    private DisplayBoardSelectionListener displayBoardSelectionListener;
    private TeamService teamService;
    private TeamCommands teamCommands;
    private ScorePanelPresenter scorePanelPresenter;
    private TimerPanelPresenter timerPanelPresenter;
    private ScorePanelStore scorePanelStore;
    private TimerPanelStore timerPanelStore;
    private TimerService timerService;
    private TimerCommands timerCommands;
    private BuzzerService buzzerService;
    private BuzzerCommands buzzerCommands;
    private BuzzerListener buzzerListener;
    private FastMoneyPlayerBindService fastMoneyPlayerBindService;
    private FastMoneyService fastMoneyService;
    private FastMoneySurveySetStore fastMoneySurveySetStore;
    private FastMoneyCommands fastMoneyCommands;
    private FastMoneyBoardPlacement fastMoneyBoardPlacement;
    private FastMoneyBoardPresenter fastMoneyBoardPresenter;
    private FastMoneyBackdropPresenter fastMoneyBackdropPresenter;
    private io.letsrolldrew.feud.ui.HostBookAnchorStore hostBookAnchorStore;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        this.surveyRepository = SurveyRepository.load(plugin.getConfig());
        this.gameController = new SimpleGameController(config.maxStrikes());
        this.teamService = new TeamService();
        NamespacedKey hostKey = new NamespacedKey(plugin, "host_remote");
        this.displayBoardSelectionStore = new DisplayBoardSelectionStore();
        NamespacedKey displayWandKey = new NamespacedKey(plugin, "display_board_wand");
        this.hostBookAnchorStore = new io.letsrolldrew.feud.ui.HostBookAnchorStore();
        this.hostBookUiBuilder =
                new HostBookUiBuilder("/feud ui", surveyRepository, null, hostKey, displayBoardSelectionStore);
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
        File panelStoreFile = new File(plugin.getDataFolder(), "panels.yml");
        this.scorePanelStore = new ScorePanelStore(panelStoreFile);
        File timerPanelStoreFile = new File(plugin.getDataFolder(), "timer-panels.yml");
        this.timerPanelStore = new TimerPanelStore(timerPanelStoreFile);
        this.animationService = new io.letsrolldrew.feud.effects.anim.AnimationService(
                new io.letsrolldrew.feud.effects.anim.BukkitScheduler(plugin));
        this.timerService = new TimerService(
                new io.letsrolldrew.feud.effects.anim.BukkitScheduler(plugin), System::currentTimeMillis, 20);
        this.timerCommands = new TimerCommands(timerService, config.hostPermission(), "familyfeud.admin");
        this.buzzerService = new BuzzerService(
                new io.letsrolldrew.feud.effects.anim.BukkitScheduler(plugin),
                System::currentTimeMillis,
                teamService,
                12_000L,
                1_000L);
        this.buzzerCommands =
                new BuzzerCommands(buzzerService, teamService, config.hostPermission(), "familyfeud.admin");
        this.teamCommands = new TeamCommands(teamService, config.hostPermission(), "familyfeud.admin", buzzerCommands);
        this.buzzerListener = new BuzzerListener(buzzerService, teamService);
        this.scorePanelPresenter = new ScorePanelPresenter(displayRegistry, teamService);
        this.timerPanelPresenter = new TimerPanelPresenter(displayRegistry);
        this.timerService.setOnTick(seconds -> timerPanelPresenter.updateAll(seconds));
        this.boardRenderer = new BoardRenderer(framebufferStore, dirtyTracker);
        this.slotRevealPainter =
                new io.letsrolldrew.feud.board.render.SlotRevealPainter(framebufferStore, dirtyTracker, boardRenderer);
        File hologramStore = new File(plugin.getDataFolder(), "holograms.yml");
        this.hologramService = new HologramService(displayRegistry, hologramStore);
        this.hologramCommands = new HologramCommands(hologramService);
        this.surveyCommands = new SurveyCommands(surveyRepository, config.hostPermission(), gameController);
        this.fastMoneyService = new FastMoneyService();
        this.fastMoneyPlayerBindService = new FastMoneyPlayerBindService(fastMoneyService);
        this.hostBookUiBuilder.setFastMoneyService(fastMoneyService);
        this.hostBookUiBuilder.setHostBookAnchorStore(hostBookAnchorStore);
        File fastMoneyFile = new File(plugin.getDataFolder(), "fast-money.yml");
        ensureFastMoneyFile(fastMoneyFile);
        this.fastMoneySurveySetStore =
                FastMoneySurveySetStore.load(YamlConfiguration.loadConfiguration(fastMoneyFile), surveyRepository);
        File dynamicBoardsFile = new File(plugin.getDataFolder(), "dynamic-boards.yml");
        this.displayBoardPresenter = new DisplayBoardService(
                displayRegistry, animationService, dynamicBoardsFile, displayBoardSelectionStore);
        this.fastMoneyBoardPlacement = new FastMoneyBoardPlacement();
        this.fastMoneyBoardPresenter = new FastMoneyBoardPresenter(displayRegistry, fastMoneyBoardPlacement);
        this.fastMoneyBackdropPresenter = new FastMoneyBackdropPresenter(displayRegistry);
        this.fastMoneyCommands = new FastMoneyCommands(
                fastMoneyService,
                fastMoneySurveySetStore,
                fastMoneyPlayerBindService,
                displayBoardPresenter,
                fastMoneyBoardPresenter,
                fastMoneyBackdropPresenter,
                config.hostPermission(),
                "familyfeud.admin");
        this.displayBoardSelectionListener =
                new DisplayBoardSelectionListener(plugin, displayWandKey, displayBoardSelectionStore, player -> {
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
                });
        this.boardCommands = new DisplayBoardCommands(
                displayBoardPresenter,
                "familyfeud.admin",
                displayBoardSelectionListener,
                gameController,
                config.hostPermission(),
                hostRemoteService,
                surveyRepository,
                hostKey,
                teamService,
                scorePanelPresenter,
                timerPanelPresenter,
                scorePanelStore,
                timerPanelStore);
        plugin.getServer().getPluginManager().registerEvents(boardWandService, plugin);
        plugin.getServer().getPluginManager().registerEvents(displayBoardSelectionListener, plugin);
        plugin.getServer().getPluginManager().registerEvents(buzzerListener, plugin);
        plugin.getServer()
                .getPluginManager()
                .registerEvents(new FastMoneyPlayerBindListener(fastMoneyPlayerBindService), plugin);
        scorePanelPresenter.rehydrateStoredPanels(scorePanelStore);
        timerPanelPresenter.rehydrateStoredPanels(timerPanelStore);
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
                teamService,
                scorePanelPresenter,
                timerCommands,
                buzzerCommands,
                fastMoneyCommands,
                displayRegistry,
                scorePanelStore,
                timerPanelStore,
                hostBookAnchorStore);
        feud.setExecutor(feudRootCommand);
        registerBrigadier(feudRootCommand);
    }

    private void registerBrigadier(FeudRootCommand command) {
        try {
            LifecycleEventManager<?> lifecycle = plugin.getLifecycleManager();
            lifecycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
                CommandSpecificationNode spec =
                        factory.buildFullSpecification(config.hostPermission(), "familyfeud.admin");

                BrigadierAdapter adapter = new BrigadierAdapter();
                LiteralCommandNode<CommandSourceStack> root = adapter.buildWithExecution(
                        spec, (source, args) -> exec(command, source, args.toArray(new String[0])));

                event.registrar().register(root);
            });
        } catch (Throwable ex) {
            plugin.getLogger().fine("Skipping Brigadier registration: " + ex.getMessage());
        }
    }

    private int exec(FeudRootCommand command, CommandSourceStack source, String... args) {
        boolean handled = command.onCommand(source.getSender(), null, "feud", args);
        return handled ? 1 : 0;
    }

    private void ensureFastMoneyFile(File fastMoneyFile) {
        if (fastMoneyFile.exists()) {
            return;
        }
        try {
            fastMoneyFile.getParentFile().mkdirs();
            String contents = """
                    fastMoney:
                      packs:
                        s1:
                          targetScore: 200
                          player1Seconds: 20
                          player2Seconds: 25
                          surveys: ["example_animals", "example_breakfast", "block_taste", "suspicious_animal", "build_materials"]
                    """;
            Files.writeString(fastMoneyFile.toPath(), contents, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not create fast-money.yml: " + ex.getMessage());
        }
    }
}
