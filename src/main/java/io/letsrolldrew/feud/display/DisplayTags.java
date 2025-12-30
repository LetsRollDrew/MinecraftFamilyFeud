package io.letsrolldrew.feud.display;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataType;

public final class DisplayTags {
    private static final NamespacedKey MANAGED = NamespacedKey.fromString("feud:managed");
    private static final NamespacedKey KIND = NamespacedKey.fromString("feud:kind");
    private static final NamespacedKey GROUP = NamespacedKey.fromString("feud:group");

    private DisplayTags() {
    }

    public static void tag(Display display, String kind, String group) {
        if (display == null || MANAGED == null || KIND == null || GROUP == null) {
            return;
        }
        var pdc = display.getPersistentDataContainer();
        pdc.set(MANAGED, PersistentDataType.INTEGER, 1);
        if (kind != null) {
            pdc.set(KIND, PersistentDataType.STRING, kind);
        }
        if (group != null) {
            pdc.set(GROUP, PersistentDataType.STRING, group);
        }
    }

    public static boolean isManaged(Display display) {
        if (display == null || MANAGED == null) {
            return false;
        }
        Integer flag = display.getPersistentDataContainer().get(MANAGED, PersistentDataType.INTEGER);
        return flag != null && flag == 1;
    }

    public static boolean isManaged(Display display, String kind) {
        if (!isManaged(display)) {
            return false;
        }
        if (kind == null || KIND == null) {
            return true;
        }
        String stored = display.getPersistentDataContainer().get(KIND, PersistentDataType.STRING);
        return kind.equals(stored);
    }
}
