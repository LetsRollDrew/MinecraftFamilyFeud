package io.letsrolldrew.feud.display;

import java.util.Objects;

public record DisplayKey(String namespace, String group, String id, String part) {
    public DisplayKey {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(part, "part");
    }
}
