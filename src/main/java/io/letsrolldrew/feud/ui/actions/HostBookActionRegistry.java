package io.letsrolldrew.feud.ui.actions;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
