package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class BuzzerListener implements Listener {
    private final Messages messages;
    private final BuzzerService buzzerService;
    private final TeamService teamService;

    public BuzzerListener(Messages messages, BuzzerService buzzerService, TeamService teamService) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.buzzerService = Objects.requireNonNull(buzzerService, "buzzerService");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Location loc =
                event.getClickedBlock() == null ? null : event.getClickedBlock().getLocation();
        if (loc == null) {
            return;
        }

        if (buzzerService.isBinding(event.getPlayer())) {
            buzzerService.bindTo(event.getPlayer(), loc);
            messages.success(event.getPlayer(), Msg.BUZZER_BOUND);
            event.setCancelled(true);
            return;
        }

        Optional<TeamId> winner = buzzerService.tryBuzz(event.getPlayer(), loc);
        if (winner.isEmpty()) {
            return;
        }

        String name = teamService.getName(winner.get());
        // just a block note for now
        // use family feud buzzer sound later
        event.getPlayer().getWorld().playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        messages.success(event.getPlayer(), Msg.BUZZ_ACCEPTED, Placeholder.of("team", name));
        event.setCancelled(true);
    }
}
