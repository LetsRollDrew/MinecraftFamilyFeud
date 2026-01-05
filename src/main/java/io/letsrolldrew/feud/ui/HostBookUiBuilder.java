package io.letsrolldrew.feud.ui;

import static io.letsrolldrew.feud.ui.BookTextFormatter.abbreviate;
import static io.letsrolldrew.feud.ui.BookTextFormatter.formatRevealedLabel;
import static io.letsrolldrew.feud.ui.BookTextFormatter.strikeLine;
import static io.letsrolldrew.feud.ui.BookTextFormatter.unrevealedLabel;
import static io.letsrolldrew.feud.ui.BookUiComponents.page;
import static io.letsrolldrew.feud.ui.BookUiComponents.row;
import static io.letsrolldrew.feud.ui.BookUiComponents.row3;
import static io.letsrolldrew.feud.ui.BookUiComponents.rowSpacer;
import static io.letsrolldrew.feud.ui.BookUiComponents.spacerLine;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.fastmoney.FastMoneyService;
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
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class HostBookUiBuilder {
    private static final String UI_CLICK_PREFIX = "/feud ui click ";

    private static final List<HostBookPage> PAGE_ORDER = List.of(
            HostBookPage.CONTROL,
            HostBookPage.SURVEYS,
            HostBookPage.SELECTOR,
            HostBookPage.FAST_MONEY_CONFIG,
            HostBookPage.FAST_MONEY);

    private final SurveyRepository surveyRepository;
    private final List<String> fallbackHovers;
    private final NamespacedKey hostKey;
    private final DisplayBoardSelectionStore selectionStore;
    private final BookButtonFactory buttons;
    private boolean openBookEnabled;
    private FastMoneyHoverResolver fastMoneyHoverResolver;
    private HostBookAnchorStore hostBookAnchorStore;

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
        Validation.requireNonBlank(commandPrefix, "commandPrefix");
        this.surveyRepository = surveyRepository;
        this.fallbackHovers = fallbackHovers;
        this.hostKey = hostKey;
        this.selectionStore = selectionStore;
        this.buttons = new BookButtonFactory(UI_CLICK_PREFIX);
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
        return createBookInternal(
                player, hovers, activeSurvey, revealedSlots, strikeCount, maxStrikes, roundPoints, controllingTeam);
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

    private ItemStack createBookInternal(
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
        rotatePagesForPlayer(player, pages);
        return toWrittenBook(pages);
    }

    private ItemStack toWrittenBook(List<Component> pages) {
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

    public void setFastMoneyService(FastMoneyService fastMoneyService) {
        if (fastMoneyService == null || surveyRepository == null) {
            this.fastMoneyHoverResolver = null;
            return;
        }
        this.fastMoneyHoverResolver = new FastMoneyHoverResolver(fastMoneyService, surveyRepository);
    }

    public void setHostBookAnchorStore(HostBookAnchorStore hostBookAnchorStore) {
        this.hostBookAnchorStore = hostBookAnchorStore;
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
        pages.add(fastMoneyPage());
        return pages;
    }

    private Component teamsTimerPage() {
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

    private Component fastMoneyPage() {
        List<Component> rows = new ArrayList<>();
        rows.add(Component.text("Fast Money Controller", NamedTextColor.GOLD));
        rows.add(Component.text("Load Survey Set", NamedTextColor.GRAY));
        rows.add(Component.join(
                JoinConfiguration.separator(Component.space()),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "S1",
                        "/feud fastmoney set s1",
                        "Load survey set S1",
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "S2",
                        "/feud fastmoney set s2",
                        "Load survey set S2",
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "S3",
                        "/feud fastmoney set s3",
                        "Load survey set S3",
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "S4",
                        "/feud fastmoney set s4",
                        "Load survey set S4",
                        NamedTextColor.BLUE,
                        true)));
        rows.add(Component.text("Bind / Run", NamedTextColor.GRAY));
        rows.add(Component.join(
                JoinConfiguration.separator(Component.space()),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "P1",
                        "/feud fastmoney bind p1",
                        "Bind Player 1",
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "P2",
                        "/feud fastmoney bind p2",
                        "Bind Player 2",
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY,
                        "X",
                        "/feud fastmoney bind clear",
                        "Clear bindings",
                        NamedTextColor.BLUE,
                        true),
                Component.text("|", NamedTextColor.GRAY),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY, "S", "/feud fastmoney start", "Start", NamedTextColor.BLUE, true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY, "ST", "/feud fastmoney stop", "Stop", NamedTextColor.BLUE, true),
                buttons.runCommand(
                        HostBookPage.FAST_MONEY, "N", "/feud fastmoney status", "Next", NamedTextColor.BLUE, true)));
        for (int q = 1; q <= 5; q++) {
            rows.add(Component.text("Q" + q + " | P1 | P2", NamedTextColor.GRAY));
            rows.add(buildFastMoneyRow(q));
        }
        return page(rows.toArray(new Component[0]));
    }

    private Component buildFastMoneyRow(int questionIndex) {
        List<Component> buttons = new ArrayList<>();
        for (int slot = 1; slot <= 8; slot++) {
            String cmd = "/feud fastmoney reveal " + questionIndex + " " + slot;
            buttons.add(this.buttons.runCommandBare(
                    HostBookPage.FAST_MONEY,
                    String.valueOf(slot),
                    cmd,
                    fastMoneyHover(questionIndex, slot),
                    NamedTextColor.BLUE,
                    true));
        }
        return Component.join(JoinConfiguration.separator(Component.space()), buttons);
    }

    private Component selectorPage(DisplayBoardSelection selection) {
        Component header = Component.text("Display Control", NamedTextColor.GOLD);

        Component actions = Component.join(
                JoinConfiguration.separator(Component.space()),
                buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "View Selection",
                        "/feud board display selector",
                        selectionHover(selection),
                        NamedTextColor.BLUE,
                        true),
                buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "Give Selector",
                        "/feud board display selector",
                        "Gives the Display Selector wand",
                        NamedTextColor.BLUE,
                        true));

        Component spawnLabel = Component.text("Spawn on Selection:", NamedTextColor.GRAY)
                .hoverEvent(HoverEvent.showText(Component.text("Requires an active selection")));

        Component spawnButtons = Component.empty()
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "B",
                        "/feud board display selection board board1",
                        "Spawn Board",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "SPR",
                        "/feud board display selection panels board1 red",
                        "Spawn Score Panel (Red)",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "SPB",
                        "/feud board display selection panels board1 blue",
                        "Spawn Score Panel (Blue)",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "T",
                        "/feud board display selection timer board1",
                        "Spawn Timer Panel",
                        NamedTextColor.BLUE,
                        true));

        Component teamsLabel = Component.text("Buzzer:", NamedTextColor.GRAY);

        Component teamsLine = Component.empty()
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "Bind Blue",
                        "/feud team buzzer bind blue",
                        "Bind BLUE team buzzer to your next Right-Click on a block",
                        NamedTextColor.BLUE,
                        true))
                .append(Component.space())
                .append(buttons.runCommand(
                        HostBookPage.SELECTOR,
                        "Bind Red",
                        "/feud team buzzer bind red",
                        "Bind RED team buzzer to your next Right-Click on a block",
                        NamedTextColor.BLUE,
                        true));

        return page(header, spacerLine(), actions, spacerLine(), spawnLabel, spawnButtons, teamsLabel, teamsLine);
    }

    @SuppressWarnings("unused")
    private String fastMoneyHover(int questionIndex, int slot) {
        if (fastMoneyHoverResolver == null) {
            return "Answer for slot " + slot;
        }
        return fastMoneyHoverResolver.hoverFor(questionIndex, slot);
    }

    private Component buttonForSlot(
            HostBookPage page, int slot, List<String> hovers, Survey activeSurvey, Set<Integer> revealedSlots) {
        boolean revealed = revealedSlots.contains(slot);

        if (revealed
                && activeSurvey != null
                && slot - 1 < activeSurvey.answers().size()) {
            AnswerOption ans = activeSurvey.answers().get(slot - 1);

            // compact revealed labels
            String label = formatRevealedLabel(ans.text(), ans.points());

            String hover = "Slot " + slot + ": " + ans.text() + " (" + ans.points() + ")";
            return buttons.button(page, label, "ui reveal " + slot, hover, NamedTextColor.DARK_AQUA, true);
        }

        String label = unrevealedLabel(slot);
        return buttons.button(page, label, "ui reveal " + slot, hovers.get(slot - 1), NamedTextColor.BLUE, true);
    }

    private void rotatePagesForPlayer(Player player, List<Component> pages) {
        // rotate so the last clicked page becomes index 0 for this player
        if (player == null || hostBookAnchorStore == null || pages == null || pages.isEmpty()) {
            return;
        }

        HostBookPage anchor = hostBookAnchorStore.get(player.getUniqueId());
        if (anchor == null) {
            return;
        }

        int anchorIndex = PAGE_ORDER.indexOf(anchor);
        if (anchorIndex <= 0) {
            return;
        }

        List<Component> rotated = new ArrayList<>(pages.size());
        rotated.addAll(pages.subList(anchorIndex, pages.size()));
        rotated.addAll(pages.subList(0, anchorIndex));

        pages.clear();
        pages.addAll(rotated);
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
                buttonForSlot(HostBookPage.CONTROL, 1, hovers, activeSurvey, revealedSlots),
                buttonForSlot(HostBookPage.CONTROL, 5, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 2, hovers, activeSurvey, revealedSlots),
                buttonForSlot(HostBookPage.CONTROL, 6, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 3, hovers, activeSurvey, revealedSlots),
                buttonForSlot(HostBookPage.CONTROL, 7, hovers, activeSurvey, revealedSlots)));
        rows.add(row(
                buttonForSlot(HostBookPage.CONTROL, 4, hovers, activeSurvey, revealedSlots),
                buttonForSlot(HostBookPage.CONTROL, 8, hovers, activeSurvey, revealedSlots)));

        rows.add(spacerLine());
        rows.add(row(
                controlButton(HostBookPage.CONTROL, "Ctrl RED", "control red", controllingTeam, TeamControl.RED),
                controlButton(HostBookPage.CONTROL, "Ctrl BLUE", "control blue", controllingTeam, TeamControl.BLUE)));
        rows.add(row3(
                buttons.button(HostBookPage.CONTROL, "Strike", "ui strike", "Add a strike", NamedTextColor.BLUE, true),
                rowSpacer(),
                buttons.button(
                        HostBookPage.CONTROL,
                        "Clear",
                        "ui clearstrikes",
                        "Clear all strikes",
                        NamedTextColor.BLUE,
                        true)));
        rows.add(row3(
                buttons.button(
                        HostBookPage.CONTROL,
                        "Reset",
                        "ui reset",
                        "Reset round (clear strikes, points, reveals)",
                        NamedTextColor.GRAY,
                        true),
                rowSpacer(),
                awardButton(HostBookPage.CONTROL, controllingTeam, roundPoints)));

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

        return buttons.runCommand(HostBookPage.SURVEYS, label, command, survey.question(), color, false);
    }

    private Component controlButton(
            HostBookPage page, String label, String action, TeamControl current, TeamControl target) {
        NamedTextColor color = target == TeamControl.RED ? NamedTextColor.RED : NamedTextColor.BLUE;
        return buttons.button(page, label, "ui " + action, "Give control to " + target.name(), color, true);
    }

    private Component awardButton(HostBookPage page, TeamControl controllingTeam, int roundPoints) {
        String hover = controllingTeam == TeamControl.NONE
                ? "Set control before awarding"
                : "Award points to " + controllingTeam.name() + " (" + roundPoints + " pts)";
        NamedTextColor color = controllingTeam == TeamControl.NONE ? NamedTextColor.GRAY : NamedTextColor.GOLD;
        return buttons.button(page, "Award", "ui award", hover, color, true);
    }

    private DisplayBoardSelection selectionFor(Player player) {
        if (player == null || selectionStore == null) {
            return null;
        }
        return selectionStore.get(player.getUniqueId());
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
