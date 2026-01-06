package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
import io.letsrolldrew.feud.ui.actions.ActionIds;
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
                        .action(
                                HostBookPage.SELECTOR,
                                "View Selection",
                                ActionIds.selectorViewSelection(),
                                selectionHover(selection),
                                NamedTextColor.BLUE,
                                true),
                context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "Give Selector",
                                ActionIds.selectorGiveSelector(),
                                "Gives the Display Selector wand",
                                NamedTextColor.BLUE,
                                true));

        Component spawnLabel = Component.text("Spawn on Selection:", NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Requires an active selection")));

        Component spawnButtons = Component.empty()
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "B",
                                ActionIds.selectorSpawnBoard(),
                                "Spawn Board",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "SPR",
                                ActionIds.selectorSpawnPanelsRed(),
                                "Spawn Score Panel (Red)",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "SPB",
                                ActionIds.selectorSpawnPanelsBlue(),
                                "Spawn Score Panel (Blue)",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "T",
                                ActionIds.selectorSpawnTimer(),
                                "Spawn Timer Panel",
                                NamedTextColor.BLUE,
                                true));

        Component teamsLabel = Component.text("Buzzer:", NamedTextColor.GRAY);

        Component teamsLine = Component.empty()
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "Bind Blue",
                                ActionIds.selectorBindBlue(),
                                "Bind BLUE team buzzer to your next Right-Click on a block",
                                NamedTextColor.BLUE,
                                true))
                .append(Component.space())
                .append(context.buttons()
                        .action(
                                HostBookPage.SELECTOR,
                                "Bind Red",
                                ActionIds.selectorBindRed(),
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
