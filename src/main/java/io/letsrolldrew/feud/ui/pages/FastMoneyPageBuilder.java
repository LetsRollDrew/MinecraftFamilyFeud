package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
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
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "S1",
                                "/feud fastmoney set s1",
                                "Load survey set S1",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "S2",
                                "/feud fastmoney set s2",
                                "Load survey set S2",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "S3",
                                "/feud fastmoney set s3",
                                "Load survey set S3",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "S4",
                                "/feud fastmoney set s4",
                                "Load survey set S4",
                                NamedTextColor.BLUE,
                                true)));
        rows.add(Component.text("Bind / Run", NamedTextColor.GRAY));
        rows.add(Component.join(
                JoinConfiguration.separator(Component.space()),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "P1",
                                "/feud fastmoney bind p1",
                                "Bind Player 1",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "P2",
                                "/feud fastmoney bind p2",
                                "Bind Player 2",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "X",
                                "/feud fastmoney bind clear",
                                "Clear bindings",
                                NamedTextColor.BLUE,
                                true),
                Component.text("|", NamedTextColor.GRAY),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "S",
                                "/feud fastmoney start",
                                "Start",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "ST",
                                "/feud fastmoney stop",
                                "Stop",
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.FAST_MONEY,
                                "N",
                                "/feud fastmoney status",
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
            String cmd = "/feud fastmoney reveal " + questionIndex + " " + slot;
            rowButtons.add(context.buttons()
                    .runCommandBare(
                            HostBookPage.FAST_MONEY,
                            String.valueOf(slot),
                            cmd,
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
