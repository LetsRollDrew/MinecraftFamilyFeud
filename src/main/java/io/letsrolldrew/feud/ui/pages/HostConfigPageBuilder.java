package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.ui.BookButtonFactory;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class HostConfigPageBuilder {
    private final BookButtonFactory buttons;

    public HostConfigPageBuilder(BookButtonFactory buttons) {
        this.buttons = Objects.requireNonNull(buttons, "buttons");
    }

    public Component build() {
        Component teamsLine = Component.text("Teams: ")
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "info",
                        "/feud team info",
                        "Show team info",
                        NamedTextColor.BLUE,
                        true));

        Component timerLine = Component.text("Timer: ")
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "start",
                        "/feud timer start",
                        "Start timer",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "stop",
                        "/feud timer stop",
                        "Stop timer",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "reset",
                        "/feud timer reset",
                        "Reset timer",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "status",
                        "/feud timer status",
                        "Timer status",
                        NamedTextColor.BLUE,
                        true));

        Component buzzLine = Component.text("Buzz: ")
                .append(buttons.runCommand(
                        HostBookPage.FAST_MONEY_CONFIG,
                        "reset",
                        "/feud buzz reset",
                        "Reset buzz lock",
                        NamedTextColor.BLUE,
                        true));

        return page(teamsLine, timerLine, buzzLine);
    }
}
