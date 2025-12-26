package io.letsrolldrew.feud.ui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

// helper to apply shared PDC tags to books

public final class BookTagger {

    private BookTagger() {
    }

    public static void tagHostRemote(BookMeta meta, NamespacedKey hostKey) {
        if (meta == null || hostKey == null) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(hostKey, PersistentDataType.INTEGER, 1);
    }
}
