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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HostBookUiBuilder {
    private final String commandPrefix;
    private final SurveyRepository surveyRepository;
    private final List<String> fallbackHovers;

    public HostBookUiBuilder(String commandPrefix) {
        this(commandPrefix, null, null);
    }

    public HostBookUiBuilder(String commandPrefix, SurveyRepository surveyRepository, List<String> fallbackHovers) {
        this.commandPrefix = Validation.requireNonBlank(commandPrefix, "commandPrefix");
        this.surveyRepository = surveyRepository;
        this.fallbackHovers = fallbackHovers;
    }

    public ItemStack createBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(Component.text("Family Feud Remote", NamedTextColor.GOLD));
        meta.author(Component.text("Family Feud", NamedTextColor.YELLOW));
        meta.pages(buildPages(resolveHoverTexts()));
        book.setItemMeta(meta);
        return book;
    }

    public ItemStack createBook(List<String> hovers) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(titleComponent());
        meta.author(authorComponent());
        meta.pages(buildPages(hovers));
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

    List<Component> buildPages(List<String> hoverTexts) {
        List<String> hovers = hoverTexts == null ? defaultHovers() : hoverTexts;
        List<Component> pages = new ArrayList<>();
        pages.add(controlPage(hovers));
        pages.add(surveyLoadPage());
        return pages;
    }

    List<Component> buildPages() {
        return buildPages(resolveHoverTexts());
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

    private Component controlPage(List<String> hovers) {
        return page(
            row(button("Reveal 1", "reveal 1", hovers.get(0)), button("Reveal 2", "reveal 2", hovers.get(1))),
            spacerLine(),
            row(button("Reveal 3", "reveal 3", hovers.get(2)), button("Reveal 4", "reveal 4", hovers.get(3))),
            spacerLine(),
            row(button("Reveal 5", "reveal 5", hovers.get(4)), button("Reveal 6", "reveal 6", hovers.get(5))),
            spacerLine(),
            row(button("Reveal 7", "reveal 7", hovers.get(6)), button("Reveal 8", "reveal 8", hovers.get(7))),
            spacerLine(),
            row(button("Strike", "strike"), button("Clear Strikes", "clearstrikes")),
            row(button("Add +5", "add 5"), button("Add +10", "add 10"))
        );
    }

    private Component surveyLoadPage() {
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
            Component c1 = surveyButton(s1);
            Component c2 = s2 == null ? Component.text(" ") : surveyButton(s2);
            Component c3 = s3 == null ? Component.text(" ") : surveyButton(s3);
            rows.add(row3(c1, c2, c3));
        }
        return page(rows.toArray(new Component[0]));
    }

    private Component surveyButton(Survey survey) {
        String command = "/feud survey load " + survey.id();
        String label = abbreviate(survey.displayName(), 24);
        return Component.text(label, NamedTextColor.BLUE)
            .decorate(TextDecoration.UNDERLINED)
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
}
