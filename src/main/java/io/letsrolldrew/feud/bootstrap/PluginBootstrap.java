package io.letsrolldrew.feud.bootstrap;

import io.letsrolldrew.feud.commands.FeudRootCommand;
import io.letsrolldrew.feud.commands.UiCommand;
import io.letsrolldrew.feud.config.PluginConfig;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.game.SimpleGameController;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginBootstrap {
    private final JavaPlugin plugin;
    private PluginConfig config;
    private SurveyRepository surveyRepository;
    private GameController gameController;
    private UiCommand uiCommand;
    private HostBookUiBuilder hostBookUiBuilder;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        this.surveyRepository = SurveyRepository.load(plugin.getConfig());
        this.gameController = new SimpleGameController(config.maxStrikes());
        this.uiCommand = new UiCommand(gameController, config.hostPermission());
        this.hostBookUiBuilder = new HostBookUiBuilder("/feud ui", surveyRepository);
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
        feud.setExecutor(new FeudRootCommand(plugin, surveyRepository, uiCommand, hostBookUiBuilder, config.hostPermission()));
    }
}
