package io.letsrolldrew.feud.messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class Messages {
    private final Component prefix;

    public Messages(String pluginName) {
        Objects.requireNonNull(pluginName, "pluginName");
        this.prefix = Component.text()
                .append(Component.text("[", NamedTextColor.DARK_GRAY))
                .append(Component.text(pluginName, NamedTextColor.GOLD))
                .append(Component.text("]", NamedTextColor.DARK_GRAY))
                .append(Component.space())
                .build();
    }

    public void error(Audience audience, Msg msg, Placeholder... placeholders) {
        send(audience, MsgTone.ERROR, msg, placeholders);
    }

    public void usage(Audience audience, Msg msg, Placeholder... placeholders) {
        send(audience, MsgTone.USAGE, msg, placeholders);
    }

    public void success(Audience audience, Msg msg, Placeholder... placeholders) {
        send(audience, MsgTone.SUCCESS, msg, placeholders);
    }

    public void info(Audience audience, Msg msg, Placeholder... placeholders) {
        send(audience, MsgTone.INFO, msg, placeholders);
    }

    public void send(Audience audience, MsgTone tone, Msg msg, Placeholder... placeholders) {
        Objects.requireNonNull(audience, "audience");
        Objects.requireNonNull(tone, "tone");
        Objects.requireNonNull(msg, "msg");
        for (Component line : renderLines(tone, msg, placeholders)) {
            audience.sendMessage(line);
        }
    }

    public List<Component> renderLines(MsgTone tone, Msg msg, Placeholder... placeholders) {
        Objects.requireNonNull(tone, "tone");
        Objects.requireNonNull(msg, "msg");

        String expanded = applyPlaceholders(msg.template(), placeholders);
        String[] rawLines = expanded.split("\\R", -1);

        List<Component> out = new ArrayList<>(rawLines.length);
        for (String raw : rawLines) {
            if (raw == null || raw.isEmpty()) {
                out.add(Component.empty());
                continue;
            }

            out.add(Component.join(JoinConfiguration.noSeparators(), prefix, Component.text(raw, tone.color())));
        }

        return List.copyOf(out);
    }

    private static String applyPlaceholders(String template, Placeholder... placeholders) {
        String out = Objects.requireNonNull(template, "template");
        if (placeholders == null || placeholders.length == 0) {
            return out;
        }
        for (Placeholder placeholder : placeholders) {
            if (placeholder == null) {
                continue;
            }
            out = out.replace(placeholder.token(), placeholder.value());
        }
        return out;
    }
}
