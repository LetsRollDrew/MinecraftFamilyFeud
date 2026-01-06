package io.letsrolldrew.feud.ui.actions;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// registry mapping action ids to executable host book actions
// used by the /feud ui click <page> action <id> handler to resolve what code needs to run
// basically a lookup table for click actions

public final class HostBookActionRegistry {
    private final ConcurrentMap<HostBookActionId, HostBookAction> actions = new ConcurrentHashMap<>();

    public void register(HostBookActionId id, HostBookAction action) {
        HostBookActionId safeId = Objects.requireNonNull(id, "id");
        HostBookAction safeAction = Objects.requireNonNull(action, "action");
        actions.put(safeId, safeAction);
    }

    public Optional<HostBookAction> resolve(HostBookActionId id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(actions.get(id));
    }
}
