package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.util.Validation;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HostBookUiBuilder {
    private final String commandPrefix;
    private final SurveyRepository surveyRepository;
    private final List<String> fallbackHovers;
    private final NamespacedKey hostKey;

    public HostBookUiBuilder(String commandPrefix) {
        this(commandPrefix, null, null, null);
    }

    public HostBookUiBuilder(String commandPrefix, SurveyRepository surveyRepository, List<String> fallbackHovers, NamespacedKey hostKey) {
        this.commandPrefix = Validation.requireNonBlank(commandPrefix, "commandPrefix");
        this.surveyRepository = surveyRepository;
        this.fallbackHovers = fallbackHovers;
        this.hostKey = hostKey;
    }

    public ItemStack createBook(Survey activeSurvey) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(titleComponent());
        meta.author(authorComponent());
        tag(meta);
        meta.pages(buildPages(resolveHoverTexts(), activeSurvey));
        book.setItemMeta(meta);
        return book;
    }

    public ItemStack createBook(List<String> hovers, Survey activeSurvey) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(titleComponent());
        meta.author(authorComponent());
        tag(meta);
        meta.pages(buildPages(hovers, activeSurvey));
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

    List<Component> buildPages(List<String> hoverTexts, Survey activeSurvey) {
        List<String> hovers = hoverTexts == null ? defaultHovers() : hoverTexts;
        List<Component> pages = new ArrayList<>();
        pages.add(controlPage(hovers, activeSurvey));
        pages.add(surveyLoadPage(activeSurvey));
        return pages;
    }

    List<Component> buildPages() {
        return buildPages(resolveHoverTexts(), null);
    }

    private Component button(String label, String action) {
        return button(label, action, "Click to run " + commandPrefix + " " + action);
    }

    private Component button(String label, String action, String hoverText) {
        String command = commandPrefix + " " + action;
        Component hover = action.startsWith("reveal")
            ? Component.text(hoverText)
            : Component.text("Click to run " + command);
        return Component.text(label, NamedTextColor.BLUE)
            .decorate(TextDecoration.UNDERLINED)
            .hoverEvent(HoverEvent.showText(hover))
            .clickEvent(ClickEvent.runCommand(command));
    }

    private Component row(Component left, Component right) {
        return Component.join(JoinConfiguration.separator(spacer()), left, right);
    }

    private Component spacer() {
        return Component.text("   ");
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

    private Component controlPage(List<String> hovers, Survey activeSurvey) {
        Component headerLabel = Component.text("Active Survey:", NamedTextColor.GOLD);
        List<Component> header = new ArrayList<>();
        header.add(headerLabel);
        if (activeSurvey == null) {
            header.add(Component.text("None", NamedTextColor.GOLD));
        } else {
            header.add(Component.text(abbreviate(activeSurvey.displayName(), 32), NamedTextColor.GOLD));
        }
        List<Component> rows = new ArrayList<>();
        rows.addAll(header);
        rows.add(spacerLine());
        rows.add(row(button("Reveal 1", "reveal 1", hovers.get(0)), button("Reveal 2", "reveal 2", hovers.get(1))));
        rows.add(spacerLine());
        rows.add(row(button("Reveal 3", "reveal 3", hovers.get(2)), button("Reveal 4", "reveal 4", hovers.get(3))));
        rows.add(spacerLine());
        rows.add(row(button("Reveal 5", "reveal 5", hovers.get(4)), button("Reveal 6", "reveal 6", hovers.get(5))));
        rows.add(spacerLine());
        rows.add(row(button("Reveal 7", "reveal 7", hovers.get(6)), button("Reveal 8", "reveal 8", hovers.get(7))));
        rows.add(spacerLine());
        rows.add(row(button("Strike", "strike"), button("Clear Strikes", "clearstrikes")));
        rows.add(row(button("Add +5", "add 5"), button("Add +10", "add 10")));
        return page(rows.toArray(new Component[0]));
    }

    private Component surveyLoadPage(Survey activeSurvey) {
        if (surveyRepository == null || surveyRepository.listAll().isEmpty()) {
            return page(
                Component.text("Survey Selection List", NamedTextColor.GOLD),
                spacerLine(),
                Component.text("No surveys found. Use /feud survey list.")
            );
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
        String label = abbreviate(survey.displayName(), 24);
        NamedTextColor color = activeSurvey != null && survey.id().equals(activeSurvey.id())
            ? NamedTextColor.GREEN
            : NamedTextColor.BLUE;
        return Component.text(label, color)
            .hoverEvent(HoverEvent.showText(Component.text(survey.question())))
            .clickEvent(ClickEvent.runCommand(command));
    }

    private Component row3(Component a, Component b, Component c) {
        return Component.join(JoinConfiguration.separator(spacer()), a, b, c);
    }

    private String abbreviate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLen - 3)) + "...";
    }

    private void tag(BookMeta meta) {
        if (hostKey == null) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(hostKey, PersistentDataType.INTEGER, 1);
    }
}
