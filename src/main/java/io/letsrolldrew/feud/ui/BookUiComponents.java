package io.letsrolldrew.feud.ui;

import static io.letsrolldrew.feud.ui.BookTextFormatter.COL_GAP;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;

public final class BookUiComponents {
    private BookUiComponents() {}

    public static Component page(Component... components) {
        return Component.join(JoinConfiguration.separator(Component.newline()), components);
    }

    public static Component row(Component left, Component right) {
        return Component.join(JoinConfiguration.separator(COL_GAP), left, right);
    }

    public static Component row3(Component a, Component b, Component c) {
        return Component.join(JoinConfiguration.separator(COL_GAP), a, b, c);
    }

    public static Component spacerLine() {
        return Component.text(" ");
    }

    public static Component rowSpacer() {
        return Component.text("      ");
    }
}
