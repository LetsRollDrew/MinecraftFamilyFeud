package io.letsrolldrew.feud.effects.holo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//hologram registry prototype
public final class HologramService {
    private final Map<String, HologramEntry> hologramsById = new HashMap<>();

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
        hologramsById.put(id, new HologramEntry(display.getUniqueId(), HologramType.TEXT_DISPLAY));
    }

    public void setText(String id, Component text) {
        if (id == null || id.isBlank() || text == null) {
            return;
        }
        resolveText(id).ifPresent(display -> display.text(text));
    }

    public void moveToPlayer(String id, Player player) {
        if (id == null || id.isBlank() || player == null) {
            return;
        }
        resolveText(id).ifPresent(display -> {
            Location loc = player.getLocation().clone().add(0, 1.8, 0);
            display.teleport(loc);
            display.setBillboard(Display.Billboard.VERTICAL);
        });
    }

    public void remove(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        HologramEntry entry = hologramsById.get(id);
        if (entry == null) {
            return;
        }
        UUID uuid = entry.uuid();
        if (uuid != null) {
            var entity = Bukkit.getEntity(uuid);
            if (entity instanceof TextDisplay display) {
                display.remove();
            } else if (entity instanceof ItemDisplay display) {
                display.remove();
            }
        }
        hologramsById.remove(id);
    }

    public void spawnItem(String id, Player player, Material material, int customModelData) {
        if (id == null || id.isBlank() || player == null) {
            throw new IllegalArgumentException("id/player required");
        }
        if (hologramsById.containsKey(id)) {
            throw new IllegalArgumentException("Hologram id already exists: " + id);
        }
        Material mat = material == null ? Material.ECHO_SHARD : material;
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of((float) customModelData));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);

        Location loc = player.getLocation().clone().add(0, 1.8, 0);
        ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.VERTICAL);
            try {
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f(2.0f, 2.0f, 2.0f),
                    new AxisAngle4f(0, 0, 0, 0)
                ));
            } catch (Throwable ignored) {
            }
        });
        hologramsById.put(id, new HologramEntry(display.getUniqueId(), HologramType.ITEM_DISPLAY));
    }

    public void moveItemToPlayer(String id, Player player) {
        // move wiring comes next; stub left intentionally simple
    }

    public void removeItem(String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        resolveItem(id).ifPresent(ItemDisplay::remove);
        hologramsById.remove(id);
    }

    public int size() {
        return hologramsById.size();
    }

    private Optional<TextDisplay> resolveText(String id) {
        return resolveEntry(id, HologramType.TEXT_DISPLAY)
            .map(entry -> Bukkit.getEntity(entry.uuid()))
            .filter(e -> e instanceof TextDisplay)
            .map(e -> (TextDisplay) e);
    }

    private Optional<ItemDisplay> resolveItem(String id) {
        return resolveEntry(id, HologramType.ITEM_DISPLAY)
            .map(entry -> Bukkit.getEntity(entry.uuid()))
            .filter(e -> e instanceof ItemDisplay)
            .map(e -> (ItemDisplay) e);
    }

    private Optional<HologramEntry> resolveEntry(String id, HologramType expectedType) {
        HologramEntry entry = hologramsById.get(id);
        if (entry == null || entry.type() != expectedType) {
            if (entry != null && entry.type() != expectedType) {
                hologramsById.remove(id);
            }
            return Optional.empty();
        }
        UUID uuid = entry.uuid();
        if (uuid == null) {
            hologramsById.remove(id);
            return Optional.empty();
        }
        if (Bukkit.getEntity(uuid) == null) {
            hologramsById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry);
    }
}
