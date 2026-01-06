package io.letsrolldrew.feud.ui.actions;

import io.letsrolldrew.feud.ui.HostBookActionContext;
import org.bukkit.entity.Player;

// command object executes for a host book action id
// receives the clicking player and a shared context to apply changes

public interface HostBookAction {
    void execute(Player player, HostBookActionContext context);
}
