package io.letsrolldrew.feud.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HostBookUiBuilderTest {

    @Test
    void bookPagesContainUiCommands() {
        HostBookUiBuilder builder = new HostBookUiBuilder("/feud ui");
        List<Component> pages = builder.buildPages();

        List<String> commands = collectCommands(pages.get(0));
        assertTrue(commands.contains("/feud ui reveal 1"));
        assertTrue(commands.contains("/feud ui strike"));
        assertTrue(commands.contains("/feud ui clearstrikes"));
        assertTrue(commands.contains("/feud ui add 5"));
    }

    private List<String> collectCommands(Component component) {
        List<String> commands = new ArrayList<>();
        ClickEvent click = component.clickEvent();
        if (click != null && click.action() == ClickEvent.Action.RUN_COMMAND) {
            commands.add(click.value());
        }
        for (Component child : component.children()) {
            commands.addAll(collectCommands(child));
        }
        return commands;
    }
}
