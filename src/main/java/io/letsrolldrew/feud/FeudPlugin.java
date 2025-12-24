package io.letsrolldrew.feud;

import io.letsrolldrew.feud.bootstrap.PluginBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public final class FeudPlugin extends JavaPlugin {
    private PluginBootstrap bootstrap;

    @Override
    @SuppressWarnings("deprecation") // getDescription is deprecated in API, fix later
    public void onEnable() {
        getLogger().info(() -> "Enabling FamilyFeud v" + getDescription().getVersion());
        bootstrap = new PluginBootstrap(this);
        try {
            bootstrap.enable();
        } catch (Exception ex) {
            getLogger().severe("Failed to enable FamilyFeud: " + ex.getMessage());
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (bootstrap != null) {
            try {
                bootstrap.disable();
            } catch (Exception ex) {
                getLogger().severe("Error during shutdown: " + ex.getMessage());
                ex.printStackTrace();
            } finally {
                bootstrap = null;
            }
        }
        getLogger().info("Disabled FamilyFeud");
    }
}
