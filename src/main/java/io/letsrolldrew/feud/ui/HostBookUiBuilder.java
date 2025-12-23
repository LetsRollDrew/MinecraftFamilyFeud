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

    public HostBookUiBuilder(String commandPrefix) {
        this(commandPrefix, null);
    }

    public HostBookUiBuilder(String commandPrefix, SurveyRepository surveyRepository) {
        this.commandPrefix = Validation.requireNonBlank(commandPrefix, "commandPrefix");
        this.surveyRepository = surveyRepository;
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

    List<Component> buildPages(List<String> hoverTexts) {
        List<String> hovers = hoverTexts == null ? Collections.nCopies(8, "Reveal (AnswerName: Points)") : hoverTexts;
        List<Component> pages = new ArrayList<>();
        pages.add(page(
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
        ));
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
        if (surveyRepository == null || surveyRepository.listAll().isEmpty()) {
            return Collections.nCopies(8, "Reveal (AnswerName: Points)");
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
}
