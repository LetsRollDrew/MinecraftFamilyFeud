package io.letsrolldrew.feud.effects.fastmoney;

import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class FastMoneyChatListener implements Listener {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private final Plugin plugin;
    private final FastMoneyCommands fastMoneyCommands;

    public FastMoneyChatListener(Plugin plugin, FastMoneyCommands fastMoneyCommands) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.fastMoneyCommands = Objects.requireNonNull(fastMoneyCommands, "fastMoneyCommands");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAsyncChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (!fastMoneyCommands.acceptsResponseFrom(playerId)) {
            return;
        }

        String answer = PLAIN.serialize(event.message());
        event.setCancelled(true);

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player onlinePlayer = Bukkit.getPlayer(playerId);
            if (onlinePlayer == null) {
                return;
            }
            fastMoneyCommands.captureChatAnswer(onlinePlayer, answer);
        });
    }
}
