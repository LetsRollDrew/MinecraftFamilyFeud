package io.letsrolldrew.feud.config;

import io.letsrolldrew.feud.util.Validation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public final class PluginConfig {
    private final String hostPermission;
    private final boolean allowChatGuesses;
    private final int maxStrikes;

    private PluginConfig(String hostPermission, boolean allowChatGuesses, int maxStrikes) {
        this.hostPermission = hostPermission;
        this.allowChatGuesses = allowChatGuesses;
        this.maxStrikes = maxStrikes;
    }

    public static PluginConfig from(FileConfiguration config) {
        return fromSection(config);
    }

    public static PluginConfig fromSection(ConfigurationSection section) {
        String hostPermission = Validation.requireNonBlank(
            section.getString("host-permission", "familyfeud.host"),
            "host-permission"
        );
        boolean allowChatGuesses = section.getBoolean("allow-chat-guesses", false);
        int maxStrikes = Validation.requirePositive(section.getInt("max-strikes", 3), "max-strikes");
        return new PluginConfig(hostPermission, allowChatGuesses, maxStrikes);
    }

    public String hostPermission() {
        return hostPermission;
    }

    public boolean allowChatGuesses() {
        return allowChatGuesses;
    }

    public int maxStrikes() {
        return maxStrikes;
    }
}
