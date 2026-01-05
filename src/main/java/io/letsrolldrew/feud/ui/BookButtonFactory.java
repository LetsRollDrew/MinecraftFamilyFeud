package io.letsrolldrew.feud.ui;

import static io.letsrolldrew.feud.ui.BookTextFormatter.toNoBreak;

import io.letsrolldrew.feud.util.Validation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class BookButtonFactory {
    private final String clickPrefix;

    public BookButtonFactory(String clickPrefix) {
        this.clickPrefix = Validation.requireNonBlank(clickPrefix, "clickPrefix");
    }

    public Component button(
            HostBookPage page, String label, String action, String hoverText, NamedTextColor color, boolean noBreak) {
        String command = clickPrefix + " " + page.token() + " " + action;
        Component hover = hoverText != null ? Component.text(hoverText) : Component.text("Click to run " + command);

        String shown = noBreak ? toNoBreak(label) : label;
        String stableLabel = "[" + shown + "]";

        return Component.text(stableLabel, color)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public Component runCommand(
            HostBookPage page,
            String label,
            String fullCommand,
            String hoverText,
            NamedTextColor color,
            boolean noBreak) {
        String shown = noBreak ? toNoBreak(label) : label;
        String stableLabel = "[" + shown + "]";
        String remainder = stripFeudPrefix(fullCommand);
        String command = clickPrefix + " " + page.token() + " " + remainder;

        return Component.text(stableLabel, color)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public Component runCommandBare(
            HostBookPage page,
            String label,
            String fullCommand,
            String hoverText,
            NamedTextColor color,
            boolean noBreak) {
        String shown = noBreak ? toNoBreak(label) : label;
        String remainder = stripFeudPrefix(fullCommand);
        String command = clickPrefix + " " + page.token() + " " + remainder;
        return Component.text(shown, color)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    private String stripFeudPrefix(String fullCommand) {
        if (fullCommand == null) {
            return "";
        }
        String trimmed = fullCommand.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.startsWith("feud ")) {
            return trimmed.substring("feud ".length()).trim();
        }
        return trimmed;
    }
}
