package io.letsrolldrew.feud.bootstrap;

import io.letsrolldrew.feud.commands.FeudRootCommand;
import io.letsrolldrew.feud.config.PluginConfig;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.SimpleGameController;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.DirtyTracker;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.effects.holo.HologramService;
import io.letsrolldrew.feud.board.display.DefaultDisplayBoardPresenter;
import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.commands.BoardCommands;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginBootstrap {
    private final JavaPlugin plugin;
    private PluginConfig config;
    private SurveyRepository surveyRepository;
    private GameController gameController;
    private HostBookUiBuilder hostBookUiBuilder;
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
    private DisplayBoardPresenter displayBoardPresenter;
    private BoardCommands boardCommands;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        this.surveyRepository = SurveyRepository.load(plugin.getConfig());
        this.gameController = new SimpleGameController(config.maxStrikes());
        // refresher wired later via FeudRootCommand
        NamespacedKey hostKey = new NamespacedKey(plugin, "host_remote");
        this.hostBookUiBuilder = new HostBookUiBuilder("/feud ui", surveyRepository, null, hostKey);
        this.hostRemoteService = new HostRemoteService(plugin, hostKey, false);
        NamespacedKey wandKey = new NamespacedKey(plugin, "board_wand");
        this.boardBindingStore = new BoardBindingStore(plugin);
        this.boardWandService = new BoardWandService(plugin, wandKey, boardBindingStore);
        this.framebufferStore = new TileFramebufferStore();
        this.mapIdStore = new MapIdStore(new java.io.File(plugin.getDataFolder(), "map-ids.yml"));
        this.dirtyTracker = new DirtyTracker();
        this.boardRenderer = new BoardRenderer(framebufferStore, dirtyTracker);
        this.slotRevealPainter = new io.letsrolldrew.feud.board.render.SlotRevealPainter(framebufferStore, dirtyTracker, boardRenderer);
        this.hologramService = new HologramService();
        this.hologramCommands = new HologramCommands(hologramService);
        this.displayBoardPresenter = new DefaultDisplayBoardPresenter();
        this.boardCommands = new BoardCommands(displayBoardPresenter, "familyfeud.admin");
        plugin.getServer().getPluginManager().registerEvents(boardWandService, plugin);
        registerCommands();
    }

    public void disable() {

    }

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
        feud.setExecutor(new FeudRootCommand(
            plugin,
            surveyRepository,
            hostBookUiBuilder,
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
            boardCommands
        ));
    }
}
