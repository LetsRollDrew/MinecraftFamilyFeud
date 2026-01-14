package io.letsrolldrew.feud.ui;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

// helper to apply shared PDC tags to books

public final class BookTagger {

    private BookTagger() {}

    public static void tagHostRemote(BookMeta meta, NamespacedKey hostKey) {
        tagHostRemote(meta, hostKey, HostRemoteKind.MAP);
    }

    public static void tagHostRemote(BookMeta meta, NamespacedKey hostKey, HostRemoteKind kind) {
        if (meta == null || hostKey == null) {
            return;
        }
        if (kind == null) {
            kind = HostRemoteKind.MAP;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(hostKey, PersistentDataType.INTEGER, kind.tag());
    }
}
