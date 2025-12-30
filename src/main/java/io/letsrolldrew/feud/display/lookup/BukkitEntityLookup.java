package io.letsrolldrew.feud.display.lookup;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.UUID;

public final class BukkitEntityLookup implements EntityLookup {
    @Override
    public Entity get(UUID id) {
        return Bukkit.getEntity(id);
    }
}
