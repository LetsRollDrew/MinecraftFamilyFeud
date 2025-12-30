package io.letsrolldrew.feud.ui;

import java.util.List;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;

// helper to build Adventure books from component title/author/pages
// separate so book creation is consistent everywhere

public final class BookFactory {

    private BookFactory() {}

    public static Book create(Component title, Component author, List<Component> pages) {
        if (title == null) {
            throw new IllegalArgumentException("title required");
        }
        if (author == null) {
            throw new IllegalArgumentException("author required");
        }
        if (pages == null) {
            throw new IllegalArgumentException("pages required");
        }
        return Book.book(title, author, pages);
    }
}
