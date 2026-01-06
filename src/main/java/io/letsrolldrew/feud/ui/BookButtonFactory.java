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

    public Component action(
            HostBookPage page, String label, String actionId, String hoverText, NamedTextColor color, boolean noBreak) {
        // emit the standard click shape: /feud ui click <page> action <id>
        String shown = noBreak ? toNoBreak(label) : label;
        String stableLabel = "[" + shown + "]";
        String command = formatActionCommand(page, actionId);

        Component hover = hoverText != null ? Component.text(hoverText) : Component.text("Click to run " + command);

        return Component.text(stableLabel, color)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand(command));
    }

    public Component actionBare(
            HostBookPage page, String label, String actionId, String hoverText, NamedTextColor color, boolean noBreak) {
        String shown = noBreak ? toNoBreak(label) : label;
        String command = formatActionCommand(page, actionId);
        return Component.text(shown, color)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                .clickEvent(ClickEvent.runCommand(command));
    }

    private String formatActionCommand(HostBookPage page, String actionId) {
        String validated = Validation.requireNonBlank(actionId, "actionId").toLowerCase();
        return clickPrefix + " " + page.token() + " action " + validated;
    }
}
