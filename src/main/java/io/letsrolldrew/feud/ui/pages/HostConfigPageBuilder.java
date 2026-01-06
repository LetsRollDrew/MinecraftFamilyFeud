package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
import io.letsrolldrew.feud.ui.actions.ActionIds;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class HostConfigPageBuilder {
    private final HostBookContext context;

    public HostConfigPageBuilder(HostBookContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Component build() {
        Component teamsLine = Component.text("Teams: ")
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "info",
                                ActionIds.hostConfigTeamInfo(),
                                "Show team info",
                                NamedTextColor.BLUE,
                                true));

        Component timerLine = Component.text("Timer: ")
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "start",
                                ActionIds.hostConfigTimerStart(),
                                "Start timer",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "stop",
                                ActionIds.hostConfigTimerStop(),
                                "Stop timer",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "reset",
                                ActionIds.hostConfigTimerReset(),
                                "Reset timer",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "status",
                                ActionIds.hostConfigTimerStatus(),
                                "Timer status",
                                NamedTextColor.BLUE,
                                true));

        Component buzzLine = Component.text("Buzz: ")
                .append(context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY_CONFIG,
                                "reset",
                                ActionIds.hostConfigBuzzReset(),
                                "Reset buzz lock",
                                NamedTextColor.BLUE,
                                true));

        return page(teamsLine, timerLine, buzzLine);
    }
}
