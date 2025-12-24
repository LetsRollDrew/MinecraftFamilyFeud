package io.letsrolldrew.feud.effects.holo;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//hologram registry prototype, no persistence yet.
//setting up skeleton for future use

public final class HologramService {
    private final Map<String, UUID> hologramsById = new HashMap<>();

    public boolean exists(String id) {
        return hologramsById.containsKey(id);
    }

    public void spawn(String id, Player player, Component text) {

    }

    public void setText(String id, Component text) {

    }

    public void moveToPlayer(String id, Player player) {

    }

    public void remove(String id) {

    }

    public int size() {
        return hologramsById.size();
    }
}
