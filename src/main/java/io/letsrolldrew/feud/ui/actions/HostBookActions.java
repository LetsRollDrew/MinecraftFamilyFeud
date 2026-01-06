package io.letsrolldrew.feud.ui.actions;

import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class HostBookActions {
    private HostBookActions() {}

    public static void registerControlActions(HostBookActionRegistry registry) {
        Objects.requireNonNull(registry, "registry");

        for (int slot = 1; slot <= 8; slot++) {
            String revealId = ActionIds.controlReveal(slot);
            int captureSlot = slot;
            register(registry, revealId, player -> dispatch(player, "feud ui reveal " + captureSlot));
        }

        // bridge to existing command behaviors until actions invoke services directly
        register(registry, ActionIds.controlStrike(), player -> dispatch(player, "feud ui strike"));
        register(registry, ActionIds.controlClearStrikes(), player -> dispatch(player, "feud ui clearstrikes"));
        register(registry, ActionIds.controlControlRed(), player -> dispatch(player, "feud ui control red"));
        register(registry, ActionIds.controlControlBlue(), player -> dispatch(player, "feud ui control blue"));
        register(registry, ActionIds.controlAward(), player -> dispatch(player, "feud ui award"));
        register(registry, ActionIds.controlReset(), player -> dispatch(player, "feud ui reset"));
    }

    private static void register(
            HostBookActionRegistry registry, String actionId, java.util.function.Consumer<Player> task) {
        HostBookActionId id = HostBookActionId.of(actionId);
        registry.register(id, (player, context) -> task.accept(player));
    }

    private static void dispatch(Player player, String command) {
        if (player == null) {
            return;
        }
        Bukkit.dispatchCommand(player, command);
    }
}
