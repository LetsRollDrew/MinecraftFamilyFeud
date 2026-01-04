package io.letsrolldrew.feud.effects.fastmoney;

import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class FastMoneyPlayerBindListener implements Listener {
    private final FastMoneyPlayerBindService bindService;

    public FastMoneyPlayerBindListener(FastMoneyPlayerBindService bindService) {
        this.bindService = Objects.requireNonNull(bindService, "bindService");
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player host = event.getPlayer();
        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }
        if (!bindService.isArmed(host.getUniqueId())) {
            return;
        }

        boolean bound = bindService.bindIfArmed(host.getUniqueId(), target.getUniqueId(), target.getName());
        if (bound) {
            host.sendMessage("Fast Money: bound " + target.getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player host = event.getPlayer();
        if (!bindService.isArmed(host.getUniqueId())) {
            return;
        }

        Action action = event.getAction();
        boolean leftClick = action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
        if (!leftClick) {
            return;
        }

        boolean bound = bindService.bindIfArmed(host.getUniqueId(), host.getUniqueId(), host.getName());
        if (bound) {
            host.sendMessage("Fast Money: bound " + host.getName());
        }
    }
}
