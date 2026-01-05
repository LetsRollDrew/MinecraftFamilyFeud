package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SelectorPageBuilder {
    private final HostBookContext context;

    public SelectorPageBuilder(HostBookContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Component build(DisplayBoardSelection selection) {
        Component header = Component.text("Display Control", NamedTextColor.GOLD);

        Component actions = Component.join(
                JoinConfiguration.separator(Component.space()),
                context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "View Selection",
                                "/feud board display selector",
                                selectionHover(selection),
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "Give Selector",
                                "/feud board display selector",
                                "Gives the Display Selector wand",
                                NamedTextColor.BLUE,
                                true));

        Component spawnLabel = Component.text("Spawn on Selection:", NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Requires an active selection")));

        Component spawnButtons = Component.empty()
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "B",
                                "/feud board display selection board board1",
                                "Spawn Board",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "SPR",
                                "/feud board display selection panels board1 red",
                                "Spawn Score Panel (Red)",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "SPB",
                                "/feud board display selection panels board1 blue",
                                "Spawn Score Panel (Blue)",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "T",
                                "/feud board display selection timer board1",
                                "Spawn Timer Panel",
                                NamedTextColor.BLUE,
                                true));

        Component teamsLabel = Component.text("Buzzer:", NamedTextColor.GRAY);

        Component teamsLine = Component.empty()
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "Bind Blue",
                                "/feud team buzzer bind blue",
                                "Bind BLUE team buzzer to your next Right-Click on a block",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .runCommand(
                                HostBookPage.SELECTOR,
                                "Bind Red",
                                "/feud team buzzer bind red",
                                "Bind RED team buzzer to your next Right-Click on a block",
                                NamedTextColor.BLUE,
                                true));

        return page(
                header,
                Component.text(" "),
                actions,
                Component.text(" "),
                spawnLabel,
                spawnButtons,
                teamsLabel,
                teamsLine);
    }

    private String selectionHover(DisplayBoardSelection selection) {
        if (selection == null) {
            return "No selection Use the selector wand";
        }
        return "Selection: " + formatBounds(selection) + " | Facing: "
                + selection.facing().name();
    }

    private String formatBounds(DisplayBoardSelection selection) {
        if (selection == null || selection.cornerA() == null || selection.cornerB() == null) {
            return "none";
        }
        double minX = Math.min(selection.cornerA().x, selection.cornerB().x);
        double minY = Math.min(selection.cornerA().y, selection.cornerB().y);
        double minZ = Math.min(selection.cornerA().z, selection.cornerB().z);
        double maxX = Math.max(selection.cornerA().x, selection.cornerB().x);
        double maxY = Math.max(selection.cornerA().y, selection.cornerB().y);
        double maxZ = Math.max(selection.cornerA().z, selection.cornerB().z);
        return (int) minX
                + ","
                + (int) minY
                + ","
                + (int) minZ
                + " to "
                + (int) maxX
                + ","
                + (int) maxY
                + ","
                + (int) maxZ;
    }
}
