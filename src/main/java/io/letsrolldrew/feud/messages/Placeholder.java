package io.letsrolldrew.feud.messages;

import java.util.Objects;

public record Placeholder(String key, String value) {
    public Placeholder {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }

    public static Placeholder of(String key, Object value) {
        return new Placeholder(key, Objects.toString(value, ""));
    }

    public String token() {
        return "{" + key + "}";
    }
}
