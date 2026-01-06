package io.letsrolldrew.feud.ui.actions;

import io.letsrolldrew.feud.ui.HostBookActionContext;
import org.bukkit.entity.Player;

public interface HostBookAction {
    void execute(Player player, HostBookActionContext context);
}
