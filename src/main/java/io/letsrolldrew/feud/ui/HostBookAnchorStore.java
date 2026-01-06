package io.letsrolldrew.feud.ui;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class HostBookAnchorStore {
    private final ConcurrentMap<UUID, HostBookPage> anchors = new ConcurrentHashMap<>();

    public void set(UUID playerId, HostBookPage page) {
        if (playerId == null || page == null) {
            return;
        }

        anchors.put(playerId, page);
    }

    public HostBookPage get(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        return anchors.get(playerId);
    }

    public void clear(UUID playerId) {
        if (playerId == null) {
            return;
        }

        anchors.remove(playerId);
    }
}
