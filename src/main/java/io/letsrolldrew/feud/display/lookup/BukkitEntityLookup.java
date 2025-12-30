package io.letsrolldrew.feud.display.lookup;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public final class BukkitEntityLookup implements EntityLookup {
    @Override
    public Entity get(UUID id) {
        return Bukkit.getEntity(id);
    }
}
