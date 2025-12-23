package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.util.Validation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public final class HostBookUiBuilder {
    private final String commandPrefix;

    public HostBookUiBuilder(String commandPrefix) {
        this.commandPrefix = Validation.requireNonBlank(commandPrefix, "commandPrefix");
    }

    public ItemStack createBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.title(Component.text("Family Feud Remote", NamedTextColor.GOLD));
        meta.author(Component.text("Family Feud", NamedTextColor.YELLOW));
        meta.pages(buildPages());
        book.setItemMeta(meta);
        return book;
    }

    List<Component> buildPages() {
        List<Component> pages = new ArrayList<>();
        pages.add(page(
            row(button("Reveal 1", "reveal 1"), button("Reveal 2", "reveal 2")),
            spacerLine(),
            row(button("Reveal 3", "reveal 3"), button("Reveal 4", "reveal 4")),
            spacerLine(),
            row(button("Reveal 5", "reveal 5"), button("Reveal 6", "reveal 6")),
            spacerLine(),
            row(button("Reveal 7", "reveal 7"), button("Reveal 8", "reveal 8")),
            spacerLine(),
            row(button("Strike", "strike"), button("Clear Strikes", "clearstrikes")),
            row(button("Add +5", "add 5"), button("Add +10", "add 10"))
        ));
        return pages;
    }

    private Component button(String label, String action) {
        String command = commandPrefix + " " + action;
        Component hover = action.startsWith("reveal")
            ? Component.text("Reveal (AnswerName: Points)")
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
}
