package io.letsrolldrew.feud.display;

import io.letsrolldrew.feud.display.lookup.BukkitEntityLookup;
import io.letsrolldrew.feud.display.lookup.EntityLookup;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;

public final class DisplayRegistry {
    private final Map<DisplayKey, UUID> entries = new HashMap<>();
    private final Map<DisplayKey, UUID> worlds = new HashMap<>();
    private final EntityLookup entityLookup;
    private final File storeFile;
    private final YamlConfiguration storeConfig;

    // default wiring uses Bukkit,
    // we override in tests so we can inject a fake lookup, minor
    // seam to make DisplayRegistry testable, since we can't mock entities directly
    public DisplayRegistry() {
        this(new BukkitEntityLookup(), null);
    }

    public DisplayRegistry(EntityLookup entityLookup) {
        this(entityLookup, null);
    }

    public DisplayRegistry(EntityLookup entityLookup, File storeFile) {
        this.entityLookup = entityLookup;
        this.storeFile = storeFile;
        this.storeConfig = storeFile == null ? null : new YamlConfiguration();
        if (storeFile != null) {
            if (storeFile.getParentFile() != null) {
                // data folder exists
                storeFile.getParentFile().mkdirs();
            }
            loadFromDisk();
        }
    }

    public void register(DisplayKey key, Entity entity) {
        if (key == null || entity == null) {
            return;
        }
        entries.put(key, entity.getUniqueId());
        worlds.put(key, entity.getWorld().getUID());
        persist();
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
            worlds.remove(key);
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
        worlds.remove(key);
        if (uuid == null) {
            return;
        }
        Entity entity = entityLookup.get(uuid);
        if (entity != null) {
            entity.remove();
        }
        persist();
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
            worlds.remove(entry.getKey());
            removed++;
        }
        persist();
        return removed;
    }

    public int removeByGroup(String namespace, String group) {
        if (namespace == null || group == null) {
            return 0;
        }
        int removed = 0;
        var iterator = entries.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            DisplayKey key = entry.getKey();
            if (!namespace.equals(key.namespace()) || !group.equals(key.group())) {
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
            worlds.remove(key);
            removed++;
        }
        persist();
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
        worlds.clear();
        persist();
        return removed;
    }

    private void loadFromDisk() {
        if (storeFile == null || !storeFile.exists()) {
            return;
        }
        try {
            storeConfig.load(storeFile);
        } catch (Exception ignored) {
            return;
        }

        // Bukkit YAML returns raw Map<?, ?>
        // validate fields to avoid unchecked casts
        List<Map<?, ?>> raw = storeConfig.getMapList("entries");
        boolean changed = false;
        for (Map<?, ?> map : raw) {
            try {
                String worldId = str(map, "world");
                String entityId = str(map, "uuid");
                String namespace = str(map, "namespace");
                String group = str(map, "group");
                String id = str(map, "id");
                String part = str(map, "part");
                if (worldId == null
                        || entityId == null
                        || namespace == null
                        || group == null
                        || id == null
                        || part == null) {
                    changed = true;
                    continue;
                }
                UUID wUuid = UUID.fromString(worldId);
                UUID eUuid = UUID.fromString(entityId);
                DisplayKey key = new DisplayKey(namespace, group, id, part);
                var world = Bukkit.getWorld(wUuid);
                if (world == null) {
                    changed = true;
                    continue;
                }
                Entity entity = world.getEntity(eUuid);
                if (entity instanceof Display disp && !disp.isDead()) {
                    entries.put(key, eUuid);
                    worlds.put(key, wUuid);
                } else {
                    changed = true;
                }
            } catch (Exception ignored) {
                changed = true;
            }
        }
        if (changed) {
            persist();
        }
    }

    private void persist() {
        if (storeFile == null || storeConfig == null) {
            return;
        }
        List<Map<String, Object>> raw = new ArrayList<>();
        for (Map.Entry<DisplayKey, UUID> entry : entries.entrySet()) {
            DisplayKey key = entry.getKey();
            UUID worldId = worlds.get(key);
            if (worldId == null) {
                continue;
            }
            Map<String, Object> row = new HashMap<>();
            row.put("world", worldId.toString());
            row.put("uuid", entry.getValue().toString());
            row.put("namespace", key.namespace());
            row.put("group", key.group());
            row.put("id", key.id());
            row.put("part", key.part());
            raw.add(row);
        }
        storeConfig.set("entries", raw);
        try {
            if (storeFile.getParentFile() != null) {
                // data folder exists
                storeFile.getParentFile().mkdirs();
            }
            storeConfig.save(storeFile);
        } catch (IOException ignored) {
        }
    }

    private static String str(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String s ? s : null;
    }
}
