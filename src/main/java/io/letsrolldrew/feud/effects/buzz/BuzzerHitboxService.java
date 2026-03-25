package io.letsrolldrew.feud.effects.buzz;

import io.letsrolldrew.feud.team.BlockRef;
import io.letsrolldrew.feud.team.TeamId;
import io.letsrolldrew.feud.team.TeamService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.FaceAttachable;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class BuzzerHitboxService {
    private static final float DEFAULT_WIDTH = 1.25f;
    private static final float DEFAULT_HEIGHT = 1.25f;

    private final Plugin plugin;
    private final TeamService teamService;
    private final NamespacedKey buzzerTeamKey;
    private final Map<TeamId, UUID> interactionByTeam = new EnumMap<>(TeamId.class);
    private final Map<TeamId, BukkitTask> releaseTasks = new EnumMap<>(TeamId.class);

    public BuzzerHitboxService(Plugin plugin, TeamService teamService, NamespacedKey buzzerTeamKey) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.teamService = Objects.requireNonNull(teamService, "teamService");
        this.buzzerTeamKey = Objects.requireNonNull(buzzerTeamKey, "buzzerTeamKey");
    }

    public void syncAll() {
        removeAllManagedInteractions();
        interactionByTeam.clear();

        for (TeamId team : TeamId.values()) {
            spawnForTeam(team);
        }
    }

    public void shutdown() {
        removeAllManagedInteractions();
        interactionByTeam.clear();
        for (BukkitTask task : releaseTasks.values()) {
            if (task != null) {
                task.cancel();
            }
        }
        releaseTasks.clear();
    }

    public Optional<TeamId> teamFor(Entity entity) {
        if (!(entity instanceof Interaction interaction)) {
            return Optional.empty();
        }
        String teamName = interaction.getPersistentDataContainer().get(buzzerTeamKey, PersistentDataType.STRING);
        if (teamName == null || teamName.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(TeamId.fromString(teamName));
    }

    public void animatePress(TeamId team) {
        Block block = resolveTeamBlock(team);
        if (!isSupportedButton(block) || !(block.getBlockData() instanceof Switch buttonData)) {
            return;
        }

        Switch pressed = (Switch) buttonData.clone();
        pressed.setPowered(true);
        block.setBlockData(pressed);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.0f);

        BukkitTask existing = releaseTasks.remove(team);
        if (existing != null) {
            existing.cancel();
        }
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> release(team), 20L);
        releaseTasks.put(team, task);
    }

    private void release(TeamId team) {
        releaseTasks.remove(team);
        Block block = resolveTeamBlock(team);
        if (!isSupportedButton(block) || !(block.getBlockData() instanceof Switch buttonData)) {
            return;
        }

        Switch released = (Switch) buttonData.clone();
        released.setPowered(false);
        block.setBlockData(released);
        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 1.0f, 1.0f);
    }

    private void spawnForTeam(TeamId team) {
        Block block = resolveTeamBlock(team);
        if (!isSupportedButton(block) || block.getWorld() == null) {
            return;
        }

        Interaction interaction = block.getWorld().spawn(interactionLocation(block), Interaction.class, entity -> {
            entity.setPersistent(true);
            entity.setInteractionWidth(DEFAULT_WIDTH);
            entity.setInteractionHeight(DEFAULT_HEIGHT);
            entity.setResponsive(true);
            entity.getPersistentDataContainer().set(buzzerTeamKey, PersistentDataType.STRING, team.name());
        });
        interactionByTeam.put(team, interaction.getUniqueId());
    }

    private void removeAllManagedInteractions() {
        List<Entity> managed = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            for (Interaction interaction : world.getEntitiesByClass(Interaction.class)) {
                if (interaction.getPersistentDataContainer().has(buzzerTeamKey, PersistentDataType.STRING)) {
                    managed.add(interaction);
                }
            }
        }
        for (Entity entity : managed) {
            entity.remove();
        }
    }

    private Block resolveTeamBlock(TeamId team) {
        BlockRef ref = teamService.getBuzzer(team);
        if (ref == null) {
            return null;
        }
        World world = Bukkit.getWorld(ref.worldId());
        if (world == null) {
            return null;
        }
        return world.getBlockAt(ref.x(), ref.y(), ref.z());
    }

    private static boolean isSupportedButton(Block block) {
        if (block == null) {
            return false;
        }
        Material material = block.getType();
        return material == Material.BIRCH_BUTTON || material == Material.JUNGLE_BUTTON;
    }

    private static Location interactionLocation(Block block) {
        Location location = block.getLocation().add(0.5d, 0.5d, 0.5d);
        if (!(block.getBlockData() instanceof Switch switchData)) {
            return location;
        }

        if (switchData.getAttachedFace() == FaceAttachable.AttachedFace.WALL) {
            BlockFace facing = switchData.getFacing();
            return location.add(facing.getModX() * 0.35d, facing.getModY() * 0.35d, facing.getModZ() * 0.35d);
        }
        if (switchData.getAttachedFace() == FaceAttachable.AttachedFace.CEILING) {
            return location.add(0.0d, -0.25d, 0.0d);
        }
        return location.add(0.0d, 0.25d, 0.0d);
    }
}
