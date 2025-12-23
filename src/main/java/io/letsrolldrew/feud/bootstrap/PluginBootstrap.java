package io.letsrolldrew.feud.bootstrap;

import io.letsrolldrew.feud.commands.FeudRootCommand;
import io.letsrolldrew.feud.config.PluginConfig;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginBootstrap {
    private final JavaPlugin plugin;
    private PluginConfig config;

    public PluginBootstrap(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();
        this.config = PluginConfig.from(plugin.getConfig());
        registerCommands();
    }

    public void disable() {

    }

    public PluginConfig getConfig() {
        return config;
    }

    private void registerCommands() {
        PluginCommand feud = plugin.getCommand("feud");
        if (feud == null) {
            throw new IllegalStateException("Command 'feud' not defined in plugin.yml");
        }
        feud.setExecutor(new FeudRootCommand(plugin));
    }
}
