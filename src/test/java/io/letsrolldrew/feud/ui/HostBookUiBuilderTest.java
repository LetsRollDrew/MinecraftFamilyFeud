package io.letsrolldrew.feud.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.junit.jupiter.api.Test;

class HostBookUiBuilderTest {

    private static final String CLICK_PREFIX = "feud ui click ";

    @Test
    void bookPagesContainUiCommands() {
        HostBookUiBuilder builder = new HostBookUiBuilder("/feud ui");

        List<Component> pages = builder.buildPages(
                Collections.nCopies(8, "Reveal"),
                null,
                Set.of(),
                0,
                3,
                0,
                io.letsrolldrew.feud.game.TeamControl.NONE,
                null);

        List<String> cmds = collectAllRunCommands(pages);

        // should have at least one ui-click command
        assertAny(cmds, c -> c.startsWith(CLICK_PREFIX), "Expected at least one 'feud ui click ...' command");

        // control page anchors
        assertAny(cmds, isOnPage("control", c -> c.contains("reveal 1")), "Missing control reveal 1");
        assertAny(cmds, isOnPage("control", c -> containsWholeToken(c, "strike")), "Missing control strike");
        assertAny(cmds, isOnPage("control", c -> c.contains("clearstrikes")), "Missing control clearstrikes");
        assertAny(cmds, isOnPage("control", c -> c.contains("control red")), "Missing control control red");
        assertAny(cmds, isOnPage("control", c -> c.contains("control blue")), "Missing control control blue");
        assertAny(cmds, isOnPage("control", c -> containsWholeToken(c, "award")), "Missing control award");
        assertAny(cmds, isOnPage("control", c -> containsWholeToken(c, "reset")), "Missing control reset");
    }

    @Test
    void fastMoneyPageContainsCommands() {
        HostBookUiBuilder builder = new HostBookUiBuilder("/feud ui");

        List<Component> pages = builder.buildPages(
                Collections.nCopies(8, "Reveal"),
                null,
                Set.of(),
                0,
                3,
                0,
                io.letsrolldrew.feud.game.TeamControl.NONE,
                null);

        List<String> cmds = collectAllRunCommands(pages);

        // allow either token for now
        Predicate<String> onFastMoney = isOnAnyPage(List.of("fast_money", "fast_money_config"), c -> true);

        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney set s1")), "Missing fastmoney set s1");
        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney bind p1")), "Missing fastmoney bind p1");
        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney bind p2")), "Missing fastmoney bind p2");
        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney bind clear")), "Missing fastmoney bind clear");
        assertAny(cmds, onFastMoney.and(c -> containsWholeToken(c, "fastmoney start")), "Missing fastmoney start");
        assertAny(cmds, onFastMoney.and(c -> containsWholeToken(c, "fastmoney stop")), "Missing fastmoney stop");
        assertAny(cmds, onFastMoney.and(c -> containsWholeToken(c, "fastmoney status")), "Missing fastmoney status");

        // spotcheck reveal endpoints
        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney reveal 1 1")), "Missing fastmoney reveal 1 1");
        assertAny(cmds, onFastMoney.and(c -> c.contains("fastmoney reveal 5 8")), "Missing fastmoney reveal 5 8");
    }


    private Predicate<String> isOnPage(String pageToken, Predicate<String> rest) {
        String prefix = CLICK_PREFIX + pageToken + " ";
        return cmd -> cmd.startsWith(prefix) && rest.test(cmd);
    }

    private Predicate<String> isOnAnyPage(List<String> pageTokens, Predicate<String> rest) {
        List<String> prefixes = new ArrayList<>(pageTokens.size());
        for (String tok : pageTokens) {
            prefixes.add(CLICK_PREFIX + tok + " ");
        }
        return cmd -> {
            for (String p : prefixes) {
                if (cmd.startsWith(p) && rest.test(cmd)) {
                    return true;
                }
            }
            return false;
        };
    }

    private List<String> collectAllRunCommands(List<Component> pages) {
        List<String> commands = new ArrayList<>();
        for (Component page : pages) {
            commands.addAll(collectRunCommands(page));
        }
        return commands;
    }

    private List<String> collectRunCommands(Component component) {
        List<String> commands = new ArrayList<>();

        ClickEvent click = component.clickEvent();
        if (click != null && click.action() == ClickEvent.Action.RUN_COMMAND) {
            String value = normalize(click.value());
            if (value != null && !value.isBlank()) {
                commands.add(value);
            }
        }

        for (Component child : component.children()) {
            commands.addAll(collectRunCommands(child));
        }

        return commands;
    }

    private String normalize(String cmd) {
        if (cmd == null) {
            return null;
        }
        String s = cmd.trim();

        // remove leading slash
        if (s.startsWith("/")) {
            s = s.substring(1);
        }

        s = s.replace('\u00A0', ' ');

        s = s.replaceAll("\\s+", " ").toLowerCase();

        return s;
    }

    private boolean containsWholeToken(String command, String tokenOrPhrase) {
        String padded = " " + command + " ";
        String needle = " " + tokenOrPhrase + " ";
        return padded.contains(needle);
    }

    private void assertAny(List<String> commands, Predicate<String> predicate, String message) {
        assertTrue(commands.stream().anyMatch(predicate), message + "\nCommands were:\n" + commands);
    }
}
