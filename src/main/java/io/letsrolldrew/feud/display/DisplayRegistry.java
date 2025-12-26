package io.letsrolldrew.feud.display;

import io.letsrolldrew.feud.display.lookup.BukkitEntityLookup;
import io.letsrolldrew.feud.display.lookup.EntityLookup;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DisplayRegistry {
    private final Map<DisplayKey, UUID> entries = new HashMap<>();
    private final EntityLookup entityLookup;

    // default wiring uses Bukkit,
    // we override in tests so we can inject a fake lookup, minor
    // seam to make DisplayRegistry testable, since we can't mock entities directly
    public DisplayRegistry() {
        this(new BukkitEntityLookup());
    }

    public DisplayRegistry(EntityLookup entityLookup) {
        this.entityLookup = entityLookup;
    }

    public void register(DisplayKey key, Entity entity) {
        if (key == null || entity == null) {
            return;
        }
        entries.put(key, entity.getUniqueId());
    }

    public Optional<Entity> resolve(DisplayKey key) {
        if (key == null) {
            return Optional.empty();
        }
        UUID uuid = entries.get(key);
        if (uuid == null) {
            return Optional.empty();
        }
        Entity entity = entityLookup.get(uuid);
        if (entity == null || entity.isDead()) {
            entries.remove(key);
            return Optional.empty();
        }
        return Optional.of(entity);
    }

    public Optional<TextDisplay> resolveText(DisplayKey key) {
        return resolve(key).filter(e -> e instanceof TextDisplay).map(e -> (TextDisplay) e);
    }

    public Optional<ItemDisplay> resolveItem(DisplayKey key) {
        return resolve(key).filter(e -> e instanceof ItemDisplay).map(e -> (ItemDisplay) e);
    }

    public void remove(DisplayKey key) {
        if (key == null) {
            return;
        }
        UUID uuid = entries.remove(key);
        if (uuid == null) {
            return;
        }
        Entity entity = entityLookup.get(uuid);
        if (entity != null) {
            entity.remove();
        }
    }

    public int removeByNamespace(String namespace) {
        if (namespace == null) {
            return 0;
        }
        int removed = 0;
        var iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (!namespace.equals(entry.getKey().namespace())) {
                continue;
            }
            UUID uuid = entry.getValue();
            if (uuid != null) {
                Entity entity = entityLookup.get(uuid);
                if (entity != null) {
                    entity.remove();
                }
            }
            iterator.remove();
            removed++;
        }
        return removed;
    }

    public int removeAll() {
        int removed = 0;
        for (UUID uuid : entries.values()) {
            if (uuid == null) {
                continue;
            }
            Entity entity = entityLookup.get(uuid);
            if (entity != null) {
                entity.remove();
                removed++;
            }
        }
        entries.clear();
        return removed;
    }
}
