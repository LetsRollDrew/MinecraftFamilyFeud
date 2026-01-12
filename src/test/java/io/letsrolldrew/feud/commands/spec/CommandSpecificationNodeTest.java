package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class CommandSpecificationNodeTest {

    @Test
    void defaultsExecuteAndSplitGreedy() {
        CommandSpecificationNode node =
                CommandSpecificationNode.builder(ArgType.LITERAL, "feud").build();

        assertTrue(node.executes());
        assertEquals(GreedyToken.SPLIT_SPACES, node.greedyToken());
    }

    @Test
    void builderSupportsNoExecAndGreedyRaw() {
        CommandSpecificationNode node = CommandSpecificationNode.builder(ArgType.GREEDY, "text")
                .noExec()
                .greedyRawSingleArg()
                .build();

        assertFalse(node.executes());
        assertEquals(GreedyToken.RAW_SINGLE_ARG, node.greedyToken());
    }

    @Test
    void childrenAreCopiedAndImmutable() {
        CommandSpecificationNode child =
                CommandSpecificationNode.builder(ArgType.LITERAL, "child").build();
        CommandSpecificationNode parent = CommandSpecificationNode.builder(ArgType.LITERAL, "root")
                .child(child)
                .build();

        assertEquals(1, parent.children().size());
        assertEquals("child", parent.children().get(0).name());

        assertThrowsUnsupported(parent.children());
    }

    @Test
    void literalAliasesCreatesNodesPerLiteral() {
        List<CommandSpecificationNode> nodes = CommandSpecificationNode.literalAliases("a", "b", "c");

        assertEquals(3, nodes.size());
        assertEquals("a", nodes.get(0).name());
        assertEquals("b", nodes.get(1).name());
        assertEquals("c", nodes.get(2).name());
        assertEquals(ArgType.LITERAL, nodes.get(0).type());
    }

    private void assertThrowsUnsupported(List<CommandSpecificationNode> children) {
        boolean threw = false;
        try {
            children.add(CommandSpecificationNode.builder(ArgType.LITERAL, "x").build());
        } catch (UnsupportedOperationException ex) {
            threw = true;
        }
        assertTrue(threw);
    }
}
