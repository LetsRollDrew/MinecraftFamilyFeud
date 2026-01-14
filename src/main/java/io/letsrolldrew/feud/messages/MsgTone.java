package io.letsrolldrew.feud.messages;

import net.kyori.adventure.text.format.NamedTextColor;

public enum MsgTone {
    ERROR(NamedTextColor.RED),
    USAGE(NamedTextColor.YELLOW),
    SUCCESS(NamedTextColor.GREEN),
    INFO(NamedTextColor.GRAY);

    private final NamedTextColor color;

    MsgTone(NamedTextColor color) {
        this.color = color;
    }

    public NamedTextColor color() {
        return color;
    }
}
