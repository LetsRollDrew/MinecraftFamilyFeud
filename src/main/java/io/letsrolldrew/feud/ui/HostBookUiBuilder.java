package io.letsrolldrew.feud.ui;

import static io.letsrolldrew.feud.ui.BookTextFormatter.COL_GAP;
import static io.letsrolldrew.feud.ui.BookTextFormatter.abbreviate;
import static io.letsrolldrew.feud.ui.BookTextFormatter.formatRevealedLabel;
import static io.letsrolldrew.feud.ui.BookTextFormatter.strikeLine;
import static io.letsrolldrew.feud.ui.BookTextFormatter.toNoBreak;
import static io.letsrolldrew.feud.ui.BookTextFormatter.unrevealedLabel;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.game.TeamControl;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.util.Validation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class HostBookUiBuilder {

    private final String commandPrefix;
    private final SurveyRepository surveyRepository;
    private final List<String> fallbackHovers;
    private final NamespacedKey hostKey;
    private final DisplayBoardSelectionStore selectionStore;
    private boolean openBookEnabled;

    public HostBookUiBuilder(String commandPrefix) {
        this(commandPrefix, null, null, null, null);
    }

    public HostBookUiBuilder(
            String commandPrefix,
            SurveyRepository surveyRepository,
            List<String> fallbackHovers,
            NamespacedKey hostKey) {
        this(commandPrefix, surveyRepository, fallbackHovers, hostKey, null);
    }

    public HostBookUiBuilder(
            String commandPrefix,
            SurveyRepository surveyRepository,
            List<String> fallbackHovers,
            NamespacedKey hostKey,
            DisplayBoardSelectionStore selectionStore) {
        this.commandPrefix = Validation.requireNonBlank(commandPrefix, "commandPrefix");
        this.surveyRepository = surveyRepository;
        this.fallbackHovers = fallbackHovers;
        this.hostKey = hostKey;
        this.selectionStore = selectionStore;
    }

    public ItemStack createBook(Survey activeSurvey) {
        return createBook(resolveHoverTexts(), activeSurvey, Collections.emptySet(), 0, 3, 0, TeamControl.NONE);
    }

    public ItemStack createBookFor(Player player, Survey activeSurvey) {
        return createBookFor(
                player, resolveHoverTexts(), activeSurvey, Collections.emptySet(), 0, 3, 0, TeamControl.NONE);
    }

    public ItemStack createBook(List<String> hovers, Survey activeSurvey) {
        return createBook(hovers, activeSurvey, Collections.emptySet(), 0, 3, 0, TeamControl.NONE);
    }

    public ItemStack createBookFor(Player player, List<String> hovers, Survey activeSurvey) {
        return createBookFor(player, hovers, activeSurvey, Collections.emptySet(), 0, 3, 0, TeamControl.NONE);
    }

    public ItemStack createBook(
            List<String> hovers,
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        List<Component> pages = buildPages(
                hovers, activeSurvey, revealedSlots, strikeCount, maxStrikes, roundPoints, controllingTeam, null);
        Book adventureBook = BookFactory.create(titleComponent(), authorComponent(), pages);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        BookTagger.tagHostRemote(meta, hostKey);
        try {
            meta.title(titleComponent());
            meta.author(authorComponent());
        } catch (Throwable ignored) {
            meta.setTitle(titleString());
            meta.setAuthor("Family Feud");
        }
        meta.pages(adventureBook.pages());
        book.setItemMeta(meta);
        return book;
    }

    public ItemStack createBook(
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        return createBook(
                resolveHoverTexts(),
                activeSurvey,
                revealedSlots,
                strikeCount,
                maxStrikes,
                roundPoints,
                controllingTeam);
    }

    public ItemStack createBookFor(
            Player player,
            List<String> hovers,
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        DisplayBoardSelection selection = selectionFor(player);
        List<Component> pages = buildPages(
                hovers, activeSurvey, revealedSlots, strikeCount, maxStrikes, roundPoints, controllingTeam, selection);
        Book adventureBook = BookFactory.create(titleComponent(), authorComponent(), pages);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        BookTagger.tagHostRemote(meta, hostKey);
        try {
            meta.title(titleComponent());
            meta.author(authorComponent());
        } catch (Throwable ignored) {
            meta.setTitle(titleString());
            meta.setAuthor("Family Feud");
        }
        meta.pages(adventureBook.pages());
        book.setItemMeta(meta);
        return book;
    }

    public ItemStack createBook(
            List<String> hovers,
            Survey activeSurvey,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        return createBook(
                hovers, activeSurvey, Collections.emptySet(), strikeCount, maxStrikes, roundPoints, controllingTeam);
    }

    public ItemStack createBook(
            Survey activeSurvey, int strikeCount, int maxStrikes, int roundPoints, TeamControl controllingTeam) {
        return createBook(
                resolveHoverTexts(),
                activeSurvey,
                Collections.emptySet(),
                strikeCount,
                maxStrikes,
                roundPoints,
                controllingTeam);
    }

    public Component titleComponent() {
        return Component.text("Family Feud Remote", NamedTextColor.GOLD);
    }

    public Component authorComponent() {
        return Component.text("Family Feud", NamedTextColor.YELLOW);
    }

    public String titleString() {
        return "Family Feud Remote";
    }

    public NamespacedKey getHostKey() {
        return hostKey;
    }

    public Book asAdventureBook(
            List<String> hovers,
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        List<Component> pages = buildPages(
                hovers, activeSurvey, revealedSlots, strikeCount, maxStrikes, roundPoints, controllingTeam, null);
        return BookFactory.create(titleComponent(), authorComponent(), pages);
    }

    public void openBookIfEnabled(Player player, Book book) {
        if (!openBookEnabled || player == null || book == null) {
            return;
        }
        player.openBook(book);
    }

    public void setOpenBookEnabled(boolean openBookEnabled) {
        this.openBookEnabled = openBookEnabled;
    }

    List<Component> buildPages() {
        return buildPages(resolveHoverTexts(), null, Collections.emptySet(), 0, 3, 0, TeamControl.NONE, null);
    }

    List<Component> buildPages(List<String> hoverTexts, Survey activeSurvey) {
        List<String> hovers = hoverTexts == null ? defaultHovers() : hoverTexts;
        return buildPages(hovers, activeSurvey, Collections.emptySet(), 0, 3, 0, TeamControl.NONE, null);
    }

    List<Component> buildPages(
            List<String> hoverTexts,
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam,
            DisplayBoardSelection selection) {
        List<String> hovers = hoverTexts == null ? defaultHovers() : hoverTexts;

        List<Component> pages = new ArrayList<>();
        pages.add(controlPage(
                hovers, activeSurvey, revealedSlots, strikeCount, maxStrikes, roundPoints, controllingTeam));
        pages.add(surveyLoadPage(activeSurvey));
        pages.add(selectorPage(selection));
        pages.add(teamsTimerPage());
        return pages;
    }

    private Component teamsTimerPage() {
        Component teamsLine = Component.text("Teams: ")
                .append(buttonRunCommand("info", "/feud team info", "Show team info", NamedTextColor.BLUE, true));

        Component timerLine = Component.text("Timer: ")
                .append(buttonRunCommand("start", "/feud timer start", "Start timer", NamedTextColor.BLUE, true))
                .append(Component.space())
                .append(buttonRunCommand("stop", "/feud timer stop", "Stop timer", NamedTextColor.BLUE, true))
                .append(Component.space())
                .append(buttonRunCommand("reset", "/feud timer reset", "Reset timer", NamedTextColor.BLUE, true))
                .append(Component.space())
                .append(buttonRunCommand("status", "/feud timer status", "Timer status", NamedTextColor.BLUE, true));

        Component buzzLine = Component.text("Buzz: ")
                .append(buttonRunCommand("reset", "/feud buzz reset", "Reset buzz lock", NamedTextColor.BLUE, true));

        return page(teamsLine, timerLine, buzzLine);
    }

    private Component selectorPage(DisplayBoardSelection selection) {
        Component header = Component.text("Display Control", NamedTextColor.GOLD);

        Component actions = Component.join(
                JoinConfiguration.separator(Component.space()),
                buttonRunCommand(
                        "View Selection",
                        "/feud board display selector",
                        selectionHover(selection),
                        NamedTextColor.BLUE,
                        true),
                buttonRunCommand(
                        "Give Selector",
                        "/feud board display selector",
                        "Gives the Display Selector wand",
                        NamedTextColor.BLUE,
                        true));

        Component spawnLabel = Component.text("Spawn on Selection:", NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Requires an active selection")));

        Component spawnButtons = Component.empty()
                .append(buttonRunCommand(
                        "B", "/feud board display selection board board1", "Spawn Board", NamedTextColor.BLUE, true))
                .append(Component.space())
                .append(buttonRunCommand(
                        "SPR",
                        "/feud board display selection panels board1 red",
                        "Spawn Score Panel (Red)",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttonRunCommand(
                        "SPB",
                        "/feud board display selection panels board1 blue",
                        "Spawn Score Panel (Blue)",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttonRunCommand(
                        "T",
                        "/feud board display selection timer board1",
                        "Spawn Timer Panel",
                        NamedTextColor.BLUE,
                        true));

        Component teamsLabel = Component.text("Buzzer:", NamedTextColor.GRAY);

        Component teamsLine = Component.empty()
                .append(buttonRunCommand(
                        "Bind Blue",
                        "/feud team buzzer bind blue",
                        "Bind BLUE team buzzer to your next Right-Click on a block",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttonRunCommand(
                        "Bind Red",
                        "/feud team buzzer bind red",
                        "Bind RED team buzzer to your next Right-Click on a block",
                        NamedTextColor.BLUE,
                        true));

        return page(header, spacerLine(), actions, spacerLine(), spawnLabel, spawnButtons, teamsLabel, teamsLine);
    }

    @SuppressWarnings("unused")
    private Component button(String label, String action) {
        return button(label, action, "Click to run " + commandPrefix + " " + action, NamedTextColor.BLUE, true);
    }

    @SuppressWarnings("unused")
    private Component button(String label, String action, String hoverText) {
        return button(label, action, hoverText, NamedTextColor.BLUE, true);
    }

    private Component button(String label, String action, String hoverText, NamedTextColor color) {
        return button(label, action, hoverText, color, true);
    }

    private Component button(String label, String action, String hoverText, NamedTextColor color, boolean noBreak) {
        String command = commandPrefix + " " + action;

        Component hover = hoverText != null ? Component.text(hoverText) : Component.text("Click to run " + command);

        String shown = noBreak ? toNoBreak(label) : label;
        String stableLabel = "[" + shown + "]";

        return Component.text(stableLabel, color)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand(command));
    }

    private Component buttonRunCommand(
            String label, String fullCommand, String hoverText, NamedTextColor color, boolean noBreak) {
        String shown = noBreak ? toNoBreak(label) : label;
        String stableLabel = "[" + shown + "]";

        return Component.text(stableLabel, color)
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)))
                .clickEvent(ClickEvent.runCommand(fullCommand));
    }

    private Component buttonForSlot(int slot, List<String> hovers, Survey activeSurvey, Set<Integer> revealedSlots) {
        boolean revealed = revealedSlots.contains(slot);

        if (revealed
                && activeSurvey != null
                && slot - 1 < activeSurvey.answers().size()) {
            AnswerOption ans = activeSurvey.answers().get(slot - 1);

            // compact revealed labels
            String label = formatRevealedLabel(ans.text(), ans.points());

            String hover = "Slot " + slot + ": " + ans.text() + " (" + ans.points() + ")";
            return button(label, "reveal " + slot, hover, NamedTextColor.DARK_AQUA);
        }

        String label = unrevealedLabel(slot);
        return button(label, "reveal " + slot, hovers.get(slot - 1), NamedTextColor.BLUE);
    }

    private Component row(Component left, Component right) {
        return Component.join(JoinConfiguration.separator(COL_GAP), left, right);
    }

    private Component spacerLine() {
        return Component.text(" ");
    }

    private Component page(Component... components) {
        return Component.join(JoinConfiguration.separator(Component.newline()), components);
    }

    private List<String> resolveHoverTexts() {
        if (fallbackHovers != null && fallbackHovers.size() == 8) {
            return fallbackHovers;
        }
        if (surveyRepository == null || surveyRepository.listAll().isEmpty()) {
            return defaultHovers();
        }

        Survey chosen = surveyRepository.listAll().stream()
                .filter(s -> s.answers().size() >= 8)
                .findFirst()
                .orElse(surveyRepository.listAll().get(0));

        List<String> hovers = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            if (i < chosen.answers().size()) {
                AnswerOption option = chosen.answers().get(i);
                hovers.add("Reveal (" + option.text() + ": " + option.points() + ")");
            } else {
                hovers.add("Reveal (AnswerName: Points)");
            }
        }
        return hovers;
    }

    private List<String> defaultHovers() {
        return Collections.nCopies(8, "Reveal (AnswerName: Points)");
    }

    private Component controlPage(
            List<String> hovers,
            Survey activeSurvey,
            Set<Integer> revealedSlots,
            int strikeCount,
            int maxStrikes,
            int roundPoints,
            TeamControl controllingTeam) {
        List<Component> rows = new ArrayList<>();

        String displayName = activeSurvey == null ? "Select Survey Pg.2" : abbreviate(activeSurvey.displayName(), 32);

        rows.add(Component.text(displayName, NamedTextColor.GOLD));
        rows.add(spacerLine());

        rows.add(row(
                Component.text("Pts: " + roundPoints, NamedTextColor.GOLD),
                Component.text("Strikes: " + strikeLine(strikeCount, maxStrikes), NamedTextColor.RED)));
        NamedTextColor controlColor =
                switch (controllingTeam) {
                    case RED -> NamedTextColor.RED;
                    case BLUE -> NamedTextColor.BLUE;
                    default -> NamedTextColor.GRAY;
                };
        rows.add(spacerLine());
        rows.add(Component.text("In Control: " + controllingTeam.name(), controlColor));
        rows.add(spacerLine());
        rows.add(row(
                buttonForSlot(1, hovers, activeSurvey, revealedSlots),
                buttonForSlot(5, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(2, hovers, activeSurvey, revealedSlots),
                buttonForSlot(6, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(3, hovers, activeSurvey, revealedSlots),
                buttonForSlot(7, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(4, hovers, activeSurvey, revealedSlots),
                buttonForSlot(8, hovers, activeSurvey, revealedSlots)));

        rows.add(spacerLine());
        rows.add(row(
                controlButton("Ctrl RED", "control red", controllingTeam, TeamControl.RED),
                controlButton("Ctrl BLUE", "control blue", controllingTeam, TeamControl.BLUE)));
        rows.add(row3(
                button("Strike", "strike", "Add a strike", NamedTextColor.BLUE, true),
                rowSpacer(),
                button("Clear", "clearstrikes", "Clear all strikes", NamedTextColor.BLUE, true)));
        rows.add(row3(
                button("Reset", "reset", "Reset round (clear strikes, points, reveals)", NamedTextColor.GRAY, true),
                rowSpacer(),
                awardButton(controllingTeam, roundPoints)));

        return page(rows.toArray(new Component[0]));
    }

    private Component surveyLoadPage(Survey activeSurvey) {
        if (surveyRepository == null || surveyRepository.listAll().isEmpty()) {
            return page(
                    Component.text("Survey Selection List", NamedTextColor.GOLD),
                    spacerLine(),
                    Component.text("No surveys found. Use /feud survey list."));
        }

        List<Survey> surveys = surveyRepository.listAll();
        List<Component> rows = new ArrayList<>();

        rows.add(Component.text("Survey Selection List", NamedTextColor.GOLD));
        rows.add(spacerLine());

        for (int i = 0; i < surveys.size(); i += 3) {
            Survey s1 = surveys.get(i);
            Survey s2 = (i + 1) < surveys.size() ? surveys.get(i + 1) : null;
            Survey s3 = (i + 2) < surveys.size() ? surveys.get(i + 2) : null;

            Component c1 = surveyButton(s1, activeSurvey);
            Component c2 = s2 == null ? Component.text(" ") : surveyButton(s2, activeSurvey);
            Component c3 = s3 == null ? Component.text(" ") : surveyButton(s3, activeSurvey);

            rows.add(row3(c1, c2, c3));
        }

        return page(rows.toArray(new Component[0]));
    }

    private Component surveyButton(Survey survey, Survey activeSurvey) {
        String command = "/feud survey load " + survey.id();

        String label = survey.displayName(); // normal spaces, no abbreviation

        NamedTextColor color = activeSurvey != null && survey.id().equals(activeSurvey.id())
                ? NamedTextColor.GREEN
                : NamedTextColor.BLUE;

        return buttonRunCommand(label, command, survey.question(), color, false);
    }

    private Component row3(Component a, Component b, Component c) {
        return Component.join(JoinConfiguration.separator(COL_GAP), a, b, c);
    }

    private Component rowSpacer() {
        return Component.text("      ");
    }

    private Component controlButton(String label, String action, TeamControl current, TeamControl target) {
        NamedTextColor color = target == TeamControl.RED ? NamedTextColor.RED : NamedTextColor.BLUE;
        return button(label, action, "Give control to " + target.name(), color, true);
    }

    private Component awardButton(TeamControl controllingTeam, int roundPoints) {
        String hover = controllingTeam == TeamControl.NONE
                ? "Set control before awarding"
                : "Award points to " + controllingTeam.name() + " (" + roundPoints + " pts)";
        NamedTextColor color = controllingTeam == TeamControl.NONE ? NamedTextColor.GRAY : NamedTextColor.GOLD;
        return button("Award", "award", hover, color, true);
    }

    private DisplayBoardSelection selectionFor(Player player) {
        if (player == null || selectionStore == null) {
            return null;
        }
        return selectionStore.get(player.getUniqueId());
    }

    private String formatPoint(org.joml.Vector3d point) {
        if (point == null) {
            return "?";
        }
        return (int) point.x + "," + (int) point.y + "," + (int) point.z;
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
        return (int) minX + "," + (int) minY + "," + (int) minZ + " -> " + (int) maxX + "," + (int) maxY + ","
                + (int) maxZ;
    }

    private String selectionHover(DisplayBoardSelection selection) {
        if (selection == null) {
            return "No selection. Use the selector wand.";
        }
        return "Selection: " + formatBounds(selection) + " | Facing: "
                + selection.facing().name();
    }
}
