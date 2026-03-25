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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class BuzzerListener implements Listener {
    private final Messages messages;
    private final BuzzerService buzzerService;
    private final TeamService teamService;
    private final BuzzerHitboxService buzzerHitboxService;
    private final BuzzerBindingStore buzzerBindingStore;

    public BuzzerListener(
            Messages messages,
            BuzzerService buzzerService,
            TeamService teamService,
            BuzzerHitboxService buzzerHitboxService,
            BuzzerBindingStore buzzerBindingStore) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.buzzerService = Objects.requireNonNull(buzzerService, "buzzerService");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.buzzerHitboxService = Objects.requireNonNull(buzzerHitboxService, "buzzerHitboxService");
        this.buzzerBindingStore = Objects.requireNonNull(buzzerBindingStore, "buzzerBindingStore");
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
            TeamId team = buzzerService.bindTo(event.getPlayer(), loc);
            if (team != null) {
                var ref = teamService.getBuzzer(team);
                if (ref != null) {
                    buzzerBindingStore.save(team, ref);
                }
            }
            buzzerHitboxService.syncAll();
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

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Optional<TeamId> maybeTeam = buzzerHitboxService.teamFor(event.getRightClicked());
        if (maybeTeam.isEmpty()) {
            return;
        }

        TeamId team = maybeTeam.get();
        buzzerHitboxService.animatePress(team);

        var bound = teamService.getBuzzer(team);
        if (bound == null) {
            event.setCancelled(true);
            return;
        }

        Optional<TeamId> winner = buzzerService.tryBuzz(bound);
        if (winner.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        String name = teamService.getName(winner.get());
        Location loc = event.getRightClicked().getLocation();
        event.getPlayer().getWorld().playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        messages.success(event.getPlayer(), Msg.BUZZ_ACCEPTED, Placeholder.of("team", name));
        event.setCancelled(true);
    }
}
