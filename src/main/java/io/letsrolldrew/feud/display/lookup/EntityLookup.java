package io.letsrolldrew.feud.display.lookup;

import java.util.UUID;
import org.bukkit.entity.Entity;

public interface EntityLookup {
    Entity get(UUID id);
}
