package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class FeudCommandSpecificationFactoryTest {

    @Test
    void factoryBuildsBoardDisplayOnceAndRemoteExists() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode board = child(root, "board");
        assertNotNull(board, "expected /feud board");

        long displayCount = board.children().stream()
                .filter(n -> n.name().equalsIgnoreCase("display"))
                .count();
        assertEquals(1L, displayCount, "board must contain exactly one 'display' literal");

        CommandSpecificationNode display = child(board, "display");
        assertNotNull(display, "expected /feud board display");

        CommandSpecificationNode remote = child(display, "remote");
        assertNotNull(remote, "expected /feud board display remote");
    }

    @Test
    void factorySetsUiClickActionIdToRawSingleArg() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode ui = child(root, "ui");
        assertNotNull(ui);

        CommandSpecificationNode click = child(ui, "click");
        assertNotNull(click);

        CommandSpecificationNode page = firstChildByType(click, ArgType.WORD, "page");
        assertNotNull(page);

        CommandSpecificationNode action = child(page, "action");
        assertNotNull(action);

        CommandSpecificationNode actionId = firstChildByType(action, ArgType.GREEDY, "actionId");
        assertNotNull(actionId);

        assertEquals(
                GreedyToken.RAW_SINGLE_ARG,
                actionId.greedyToken(),
                "ui click actionId should preserve raw greedy as one token");
    }

    @Test
    void uiBranchUsesHostRequirementAndTypedChildren() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode ui = child(root, "ui");
        assertNotNull(ui);
        assertFalse(ui.requirements().isEmpty());

        CommandSpecificationNode reveal = child(ui, "reveal");
        assertNotNull(reveal);
        assertEquals(ArgType.INT, firstChildByType(reveal, ArgType.INT, "slot").type());

        CommandSpecificationNode add = child(ui, "add");
        assertNotNull(add);
        assertEquals(ArgType.INT, firstChildByType(add, ArgType.INT, "points").type());
    }

    @Test
    void holoBranchRequiresAdmin() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode holo = child(root, "holo");
        assertNotNull(holo);
        assertFalse(holo.requirements().isEmpty());

        CommandSpecificationNode text = child(holo, "text");
        assertNotNull(text);
        assertEquals(ArgType.GREEDY, firstChildByType(text, ArgType.GREEDY, "rest").type());
    }

    @Test
    void boardDisplayRemoteSingleBranchAndGreedyRest() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode board = child(root, "board");
        CommandSpecificationNode display = child(board, "display");
        CommandSpecificationNode remote = child(display, "remote");

        assertNotNull(board);
        assertNotNull(display);
        assertNotNull(remote);
        assertEquals(ArgType.GREEDY, firstChildByType(remote, ArgType.GREEDY, "rest").type());
    }

    @Test
    void surveyLoadRequiresHost() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode survey = child(root, "survey");
        CommandSpecificationNode load = child(survey, "load");

        assertNotNull(load);
        assertFalse(load.requirements().isEmpty());
        assertEquals(ArgType.WORD, firstChildByType(load, ArgType.WORD, "id").type());
    }

    @Test
    void teamRequiresHostOrAdminAndGreedyTail() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode team = child(root, "team");
        assertNotNull(team);
        assertFalse(team.requirements().isEmpty());
        assertEquals(ArgType.GREEDY, firstChildByType(team, ArgType.GREEDY, "rest").type());
    }

    @Test
    void timerSupportsOptionalSeconds() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode timer = child(root, "timer");
        CommandSpecificationNode start = child(timer, "start");
        CommandSpecificationNode reset = child(timer, "reset");

        assertNotNull(timer);
        assertEquals(ArgType.INT, firstChildByType(start, ArgType.INT, "seconds").type());
        assertEquals(ArgType.INT, firstChildByType(reset, ArgType.INT, "seconds").type());
    }

    @Test
    void fastMoneyHasExpectedChildrenAndRequirements() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode fastmoney = child(root, "fastmoney");
        assertNotNull(fastmoney);
        assertFalse(fastmoney.requirements().isEmpty());

        CommandSpecificationNode answer = child(fastmoney, "answer");
        assertNotNull(answer);
        assertEquals(ArgType.GREEDY, firstChildByType(answer, ArgType.GREEDY, "text").type());

        CommandSpecificationNode bind = child(fastmoney, "bind");
        assertNotNull(bind);
        assertEquals(ArgType.WORD, firstChildByType(bind, ArgType.WORD, "target").type());
    }

    @Test
    void clearIsAdminOnlyAndBuzzUsesRequirement() {
        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildFullSpecification("host.perm", "admin.perm");

        CommandSpecificationNode clear = child(root, "clear");
        CommandSpecificationNode buzz = child(root, "buzz");

        assertNotNull(clear);
        assertFalse(clear.requirements().isEmpty());
        assertEquals("all", child(clear, "all").name());

        assertNotNull(buzz);
        assertFalse(buzz.requirements().isEmpty());
    }

    private static CommandSpecificationNode child(CommandSpecificationNode node, String name) {
        for (CommandSpecificationNode c : node.children()) {
            if (c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    private static CommandSpecificationNode firstChildByType(CommandSpecificationNode node, ArgType type, String name) {
        for (CommandSpecificationNode c : node.children()) {
            if (c.type() == type && c.name().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }
}
