package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookTextFormatter.abbreviate;
import static io.letsrolldrew.feud.ui.BookTextFormatter.formatRevealedLabel;
import static io.letsrolldrew.feud.ui.BookTextFormatter.strikeLine;
import static io.letsrolldrew.feud.ui.BookTextFormatter.unrevealedLabel;
import static io.letsrolldrew.feud.ui.BookUiComponents.page;
import static io.letsrolldrew.feud.ui.BookUiComponents.row;
import static io.letsrolldrew.feud.ui.BookUiComponents.row3;
import static io.letsrolldrew.feud.ui.BookUiComponents.rowSpacer;
import static io.letsrolldrew.feud.ui.BookUiComponents.spacerLine;

import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.ui.HostBookContext;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ControlPageBuilder {
    private final HostBookContext context;

    public ControlPageBuilder(HostBookContext context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Component build(ControlPageModel model) {
        List<String> hovers = model.hovers() == null ? List.of() : model.hovers();
        Survey activeSurvey = model.activeSurvey();
        List<Component> rows = new ArrayList<>();

        String displayName = activeSurvey == null ? "Select Survey Pg.2" : abbreviate(activeSurvey.displayName(), 32);

        rows.add(Component.text(displayName, NamedTextColor.GOLD));
        rows.add(spacerLine());

        rows.add(row(
                Component.text("Pts: " + model.roundPoints(), NamedTextColor.GOLD),
                Component.text("Strikes: " + strikeLine(model.strikeCount(), model.maxStrikes()), NamedTextColor.RED)));
        NamedTextColor controlColor =
                switch (model.controllingTeam()) {
                    case RED -> NamedTextColor.RED;
                    case BLUE -> NamedTextColor.BLUE;
                    default -> NamedTextColor.GRAY;
                };
        rows.add(spacerLine());
        rows.add(Component.text("In Control: " + model.controllingTeam().name(), controlColor));
        rows.add(spacerLine());
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 1, hovers, activeSurvey, model.revealedSlots()),
                buttonForSlot(HostBookPage.CONTROL, 5, hovers, activeSurvey, model.revealedSlots())));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 2, hovers, activeSurvey, model.revealedSlots()),
                buttonForSlot(HostBookPage.CONTROL, 6, hovers, activeSurvey, model.revealedSlots())));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 3, hovers, activeSurvey, model.revealedSlots()),
                buttonForSlot(HostBookPage.CONTROL, 7, hovers, activeSurvey, model.revealedSlots())));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 4, hovers, activeSurvey, model.revealedSlots()),
                buttonForSlot(HostBookPage.CONTROL, 8, hovers, activeSurvey, model.revealedSlots())));

        rows.add(spacerLine());
        rows.add(row(
                controlButton(
                        HostBookPage.CONTROL, "Ctrl RED", "control red", model.controllingTeam(), TeamControl.RED),
                controlButton(
                        HostBookPage.CONTROL, "Ctrl BLUE", "control blue", model.controllingTeam(), TeamControl.BLUE)));
        rows.add(row3(
                context.buttons()
                        .button(HostBookPage.CONTROL, "Strike", "ui strike", "Add a strike", NamedTextColor.BLUE, true),
                rowSpacer(),
                context.buttons()
                        .button(
                                HostBookPage.CONTROL,
                                "Clear",
                                "ui clearstrikes",
                                "Clear all strikes",
                                NamedTextColor.BLUE,
                                true)));
        rows.add(row3(
                context.buttons()
                        .button(
                                HostBookPage.CONTROL,
                                "Reset",
                                "ui reset",
                                "Reset round (clear strikes, points, reveals)",
                                NamedTextColor.GRAY,
                                true),
                rowSpacer(),
                awardButton(HostBookPage.CONTROL, model.controllingTeam(), model.roundPoints())));

        return page(rows.toArray(new Component[0]));
    }

    private Component buttonForSlot(
            HostBookPage page, int slot, List<String> hovers, Survey activeSurvey, Set<Integer> revealedSlots) {
        boolean revealed = revealedSlots != null && revealedSlots.contains(slot);

        if (revealed
                && activeSurvey != null
                && slot - 1 < activeSurvey.answers().size()) {
            AnswerOption ans = activeSurvey.answers().get(slot - 1);

            String label = formatRevealedLabel(ans.text(), ans.points());

            String hover = "Slot " + slot + ": " + ans.text() + " (" + ans.points() + ")";
            return context.buttons().button(page, label, "ui reveal " + slot, hover, NamedTextColor.DARK_AQUA, true);
        }

        String label = unrevealedLabel(slot);
        String hover = (hovers != null && hovers.size() >= slot) ? hovers.get(slot - 1) : "Reveal";
        return context.buttons().button(page, label, "ui reveal " + slot, hover, NamedTextColor.BLUE, true);
    }

    private Component controlButton(
            HostBookPage page, String label, String action, TeamControl current, TeamControl target) {
        NamedTextColor color = target == TeamControl.RED ? NamedTextColor.RED : NamedTextColor.BLUE;
        return context.buttons().button(page, label, "ui " + action, "Give control to " + target.name(), color, true);
    }

    private Component awardButton(HostBookPage page, TeamControl controllingTeam, int roundPoints) {
        String hover = controllingTeam == TeamControl.NONE
                ? "Set control before awarding"
                : "Award points to " + controllingTeam.name() + " (" + roundPoints + " pts)";
        NamedTextColor color = controllingTeam == TeamControl.NONE ? NamedTextColor.GRAY : NamedTextColor.GOLD;
        return context.buttons().button(page, "Award", "ui award", hover, color, true);
    }
}
