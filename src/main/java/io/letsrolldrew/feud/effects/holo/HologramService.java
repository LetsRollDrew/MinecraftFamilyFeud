package io.letsrolldrew.feud.effects.holo;

import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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

public final class HologramService {
    private static final String NAMESPACE = "holo";
    private final Map<String, HologramEntry> hologramsById = new HashMap<>();
    private final DisplayRegistry displayRegistry;

    public HologramService(DisplayRegistry displayRegistry) {
        this.displayRegistry = displayRegistry;
    }

    public boolean exists(String id) {
        return hologramsById.containsKey(id);
    }

    public void spawn(String id, Player player, Component text) {
        if (id == null || id.isBlank() || player == null || text == null) {
            throw new IllegalArgumentException("id/player/text required");
        }
        Location loc = player.getLocation().clone().add(0, 1.8, 0);
        DisplayKey key = textKey(id);
        TextDisplay display = loc.getWorld().spawn(loc, TextDisplay.class, entity -> {
            entity.text(withFont(text));
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
        displayRegistry.register(key, display);
        hologramsById.put(id, new HologramEntry(key, HologramType.TEXT_DISPLAY));
    }

    public void setText(String id, Component text) {
        if (id == null || id.isBlank() || text == null) {
            return;
        }
        resolveText(id).ifPresent(display -> display.text(withFont(text)));
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
        HologramEntry entry = hologramsById.remove(id);
        if (entry == null) {
            return;
        }
        displayRegistry.remove(entry.key());
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
        DisplayKey key = itemKey(id);
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
        displayRegistry.register(key, display);
        hologramsById.put(id, new HologramEntry(key, HologramType.ITEM_DISPLAY));
    }

    public void moveItemToPlayer(String id, Player player) {
        if (id == null || id.isBlank() || player == null) {
            return;
        }
        resolveItem(id).ifPresent(display -> {
            Location loc = player.getLocation().clone().add(0, 1.8, 0);
            display.teleport(loc);
            display.setBillboard(Display.Billboard.VERTICAL);
        });
    }

    public void removeItem(String id) {
        remove(id);
    }

    public int size() {
        return hologramsById.size();
    }

    public Map<String, HologramEntry> entriesSnapshot() {
        return new HashMap<>(hologramsById);
    }

    public void clearAll() {
        displayRegistry.removeByNamespace(NAMESPACE);
        hologramsById.clear();
    }

    private Optional<TextDisplay> resolveText(String id) {
        return resolveEntry(id, HologramType.TEXT_DISPLAY)
            .flatMap(entry -> displayRegistry.resolveText(entry.key()));
    }

    private Optional<ItemDisplay> resolveItem(String id) {
        return resolveEntry(id, HologramType.ITEM_DISPLAY)
            .flatMap(entry -> displayRegistry.resolveItem(entry.key()));
    }

    private Optional<HologramEntry> resolveEntry(String id, HologramType expectedType) {
        HologramEntry entry = hologramsById.get(id);
        if (entry == null || entry.type() != expectedType) {
            if (entry != null && entry.type() != expectedType) {
                hologramsById.remove(id);
            }
            return Optional.empty();
        }
        var entity = displayRegistry.resolve(entry.key());
        if (entity.isEmpty()) {
            hologramsById.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    private DisplayKey textKey(String id) {
        return new DisplayKey(NAMESPACE, "holo", id, "text");
    }

    private DisplayKey itemKey(String id) {
        return new DisplayKey(NAMESPACE, "holo", id, "item");
    }

    private Component withFont(Component component) {
        if (component == null) {
            return Component.empty();
        }
        return component.font(Key.key("feud", "feud"));
    }

    public static record HologramEntry(DisplayKey key, HologramType type) {
    }
}
