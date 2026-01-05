package io.letsrolldrew.feud.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation") // ClickEvent#value is deprecated in adventure API, fix later
class HostBookUiBuilderTest {

    @Test
    void bookPagesContainUiCommands() {
        HostBookUiBuilder builder = new HostBookUiBuilder("/feud ui");

        List<String> hovers = Collections.nCopies(8, "Reveal");
        List<Component> pages = builder.buildPages(
                hovers,
                null, // activeSurvey
                Set.of(), // revealedSlots
                0, // strikeCount
                3, // maxStrikes
                0, // roundPoints
                io.letsrolldrew.feud.game.TeamControl.NONE,
                null);

        List<String> commands = collectCommands(pages.get(0));
        assertTrue(commands.contains("/feud ui click control ui reveal 1"));
        assertTrue(commands.contains("/feud ui click control ui strike"));
        assertTrue(commands.contains("/feud ui click control ui clearstrikes"));
        assertTrue(commands.contains("/feud ui click control ui control red"));
        assertTrue(commands.contains("/feud ui click control ui control blue"));
        assertTrue(commands.contains("/feud ui click control ui award"));
        assertTrue(commands.contains("/feud ui click control ui reset"));
    }

    @Test
    void fastMoneyPageContainsCommands() {
        HostBookUiBuilder builder = new HostBookUiBuilder("/feud");
        List<Component> pages = builder.buildPages(
                Collections.nCopies(8, "Reveal"),
                null,
                Set.of(),
                0,
                3,
                0,
                io.letsrolldrew.feud.game.TeamControl.NONE,
                null);

        Component fastMoneyPage = pages.get(4);
        List<String> commands = collectCommands(fastMoneyPage);

        assertTrue(commands.contains("/feud ui click fast_money fastmoney set s1"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney bind p1"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney bind p2"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney bind clear"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney start"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney stop"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney status"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney reveal 1 1"));
        assertTrue(commands.contains("/feud ui click fast_money fastmoney reveal 5 8"));
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
