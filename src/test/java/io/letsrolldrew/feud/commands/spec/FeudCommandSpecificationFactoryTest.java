package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class FeudCommandSpecificationFactoryTest {

    @Test
    void buildsBaseSpecificationWithRootHelpAndVersion() {
        AtomicBoolean rootCalled = new AtomicBoolean(false);
        AtomicBoolean helpCalled = new AtomicBoolean(false);
        AtomicBoolean versionCalled = new AtomicBoolean(false);

        SpecExecutor rootExec = ctx -> rootCalled.compareAndSet(false, true);
        SpecExecutor helpExec = ctx -> helpCalled.compareAndSet(false, true);
        SpecExecutor versionExec = ctx -> versionCalled.compareAndSet(false, true);

        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildBaseSpecification(rootExec, helpExec, versionExec);

        assertEquals("feud", root.name());
        assertTrue(root.executor().isPresent());
        assertSame(rootExec, root.executor().orElseThrow());
        assertEquals(2, root.children().size());

        CommandSpecificationNode helpNode = root.children().get(0);
        CommandSpecificationNode versionNode = root.children().get(1);

        assertEquals("help", helpNode.name());
        assertSame(helpExec, helpNode.executor().orElseThrow());

        assertEquals("version", versionNode.name());
        assertSame(versionExec, versionNode.executor().orElseThrow());
    }
}
