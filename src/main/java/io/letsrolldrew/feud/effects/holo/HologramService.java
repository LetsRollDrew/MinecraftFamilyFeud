package io.letsrolldrew.feud.effects.holo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//hologram registry prototype
public final class HologramService {
    private final Map<String, UUID> hologramsById = new HashMap<>();

    public boolean exists(String id) {
        return hologramsById.containsKey(id);
    }

    public void spawn(String id, Player player, Component text) {
        if (id == null || id.isBlank() || player == null || text == null) {
            throw new IllegalArgumentException("id/player/text required");
        }
        Location loc = player.getLocation().clone().add(0, 1.8, 0);
        TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, entity -> {
            entity.text(text);
            entity.setBillboard(Display.Billboard.VERTICAL);
            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }
            try {
                entity.setShadowed(false);
            } catch (Throwable ignored) {
            }
            try {
                entity.setSeeThrough(true);
            } catch (Throwable ignored) {
            }
            try {
                entity.setLineWidth(200);
            } catch (Throwable ignored) {
            }
            try {
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f(0.6f, 0.6f, 0.6f),
                    new AxisAngle4f(0, 0, 0, 0)
                ));
            } catch (Throwable ignored) {
            }
        });
        // replace existing entry if present
        hologramsById.put(id, display.getUniqueId());
    }

    public void setText(String id, Component text) {
        if (id == null || id.isBlank() || text == null) {
            return;
        }
        resolve(id).ifPresent(display -> display.text(text));
    }

    public void moveToPlayer(String id, Player player) {
        if (id == null || id.isBlank() || player == null) {
            return;
        }
        resolve(id).ifPresent(display -> {
            Location loc = player.getLocation().clone().add(0, 1.8, 0);
            display.teleport(loc);
            display.setBillboard(Display.Billboard.VERTICAL);
        });
    }

    public void remove(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        UUID uuid = hologramsById.get(id);
        if (uuid == null) {
            return;
        }
        var entity = Bukkit.getEntity(uuid);
        if (entity instanceof TextDisplay display) {
            display.remove();
        }
        hologramsById.remove(id);
    }

    public int size() {
        return hologramsById.size();
    }

    private Optional<TextDisplay> resolve(String id) {
        UUID uuid = hologramsById.get(id);
        if (uuid == null) {
            return Optional.empty();
        }
        var entity = Bukkit.getEntity(uuid);
        if (entity instanceof TextDisplay display) {
            return Optional.of(display);
        }
        hologramsById.remove(id);
        return Optional.empty();
    }
}
