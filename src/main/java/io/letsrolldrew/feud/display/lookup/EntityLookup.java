package io.letsrolldrew.feud.display.lookup;

import org.bukkit.entity.Entity;

import java.util.UUID;

public interface EntityLookup {
    Entity get(UUID id);
}
