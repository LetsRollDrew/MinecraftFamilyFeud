package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
import io.letsrolldrew.feud.ui.actions.ActionIds;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

public final class FastMoneyPageBuilder {
    private final HostBookContext context;

    public FastMoneyPageBuilder(HostBookContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Component build() {
        List<Component> rows = new ArrayList<>();
        rows.add(Component.text("Fast Money Controller", NamedTextColor.GOLD));
        rows.add(Component.text("Load Survey Set", NamedTextColor.GRAY));
        rows.add(Component.join(
                JoinConfiguration.separator(Component.space()),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "S1",
                                ActionIds.fastMoneySet("s1"),
                                "Load survey set S1",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "S2",
                                ActionIds.fastMoneySet("s2"),
                                "Load survey set S2",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "S3",
                                ActionIds.fastMoneySet("s3"),
                                "Load survey set S3",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "S4",
                                ActionIds.fastMoneySet("s4"),
                                "Load survey set S4",
                                NamedTextColor.BLUE,
                                true)));
        rows.add(Component.text("Bind / Run", NamedTextColor.GRAY));
        rows.add(Component.join(
                JoinConfiguration.separator(Component.space()),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "P1",
                                ActionIds.fastMoneyBindP1(),
                                "Bind Player 1",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "P2",
                                ActionIds.fastMoneyBindP2(),
                                "Bind Player 2",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "X",
                                ActionIds.fastMoneyBindClear(),
                                "Clear bindings",
                                NamedTextColor.BLUE,
                                true),
                Component.text("|", NamedTextColor.GRAY),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "S",
                                ActionIds.fastMoneyStart(),
                                "Start",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "ST",
                                ActionIds.fastMoneyStop(),
                                "Stop",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.FAST_MONEY,
                                "N",
                                ActionIds.fastMoneyStatus(),
                                "Next",
                                NamedTextColor.BLUE,
                                true)));
        for (int q = 1; q <= 5; q++) {
            rows.add(Component.text("Q" + q + " | P1 | P2", NamedTextColor.GRAY));
            rows.add(buildFastMoneyRow(q));
        }
        return page(rows.toArray(new Component[0]));
    }

    private Component buildFastMoneyRow(int questionIndex) {
        List<Component> rowButtons = new ArrayList<>();
        for (int slot = 1; slot <= 8; slot++) {
            String actionId = ActionIds.fastMoneyReveal(questionIndex, slot);
            rowButtons.add(context.buttons()
                    .actionBare(
                            HostBookPage.FAST_MONEY,
                            String.valueOf(slot),
                            actionId,
                            hoverFor(questionIndex, slot),
                            NamedTextColor.BLUE,
                            true));
        }
        return Component.join(JoinConfiguration.separator(Component.space()), rowButtons);
    }

    private String hoverFor(int questionIndex, int slot) {
        if (context.fastMoneyHoverResolver() == null) {
            return "Answer for slot " + slot;
        }
        return context.fastMoneyHoverResolver().hoverFor(questionIndex, slot);
    }
}
