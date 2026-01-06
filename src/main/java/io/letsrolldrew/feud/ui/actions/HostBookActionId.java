package io.letsrolldrew.feud.ui.actions;

import io.letsrolldrew.feud.util.Validation;
import java.util.Objects;

public final class HostBookActionId {
    private final String value;

    private HostBookActionId(String value) {
        this.value = value;
    }

    public static HostBookActionId of(String id) {
        String validId = Validation.requireNonBlank(id, "actionId").toLowerCase();
        return new HostBookActionId(validId);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HostBookActionId that = (HostBookActionId) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
