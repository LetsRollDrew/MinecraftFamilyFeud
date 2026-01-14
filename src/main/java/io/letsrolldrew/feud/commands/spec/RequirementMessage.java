package io.letsrolldrew.feud.commands.spec;

import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import java.util.Objects;

final class RequirementMessage {
    private final Msg msg;
    private final Placeholder[] placeholders;

    RequirementMessage(Msg msg, Placeholder... placeholders) {
        this.msg = Objects.requireNonNull(msg, "msg");
        this.placeholders = placeholders == null ? new Placeholder[0] : placeholders.clone();
    }

    Msg msg() {
        return msg;
    }

    Placeholder[] placeholders() {
        return placeholders.clone();
    }
}
