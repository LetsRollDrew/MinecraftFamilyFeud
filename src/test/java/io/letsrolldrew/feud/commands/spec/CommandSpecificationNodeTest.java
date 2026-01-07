package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CommandSpecificationNodeTest {

    @Test
    void literalAliasesCreatesOneNodePerLiteral() {
        List<CommandSpecificationNode> nodes = CommandSpecificationNode.literalAliases("remove", "delete", "rm");

        assertEquals(3, nodes.size());
        assertEquals("remove", nodes.get(0).name());
        assertEquals("delete", nodes.get(1).name());
        assertEquals("rm", nodes.get(2).name());
    }
}
