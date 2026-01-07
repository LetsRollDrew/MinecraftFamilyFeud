package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
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

    @Test
    void buildsUiSpecificationUnderHostPermission() {
        AtomicBoolean uiCalled = new AtomicBoolean(false);
        AtomicBoolean revealCalled = new AtomicBoolean(false);
        AtomicBoolean strikeCalled = new AtomicBoolean(false);
        AtomicBoolean clearCalled = new AtomicBoolean(false);
        AtomicBoolean addCalled = new AtomicBoolean(false);
        AtomicBoolean controlCalled = new AtomicBoolean(false);
        AtomicBoolean awardCalled = new AtomicBoolean(false);
        AtomicBoolean resetCalled = new AtomicBoolean(false);
        AtomicBoolean clickCalled = new AtomicBoolean(false);

        SpecExecutor rootExec = ctx -> true;
        SpecExecutor helpExec = ctx -> true;
        SpecExecutor versionExec = ctx -> true;
        SpecExecutor uiExec = ctx -> uiCalled.compareAndSet(false, true);
        SpecExecutor revealExec = ctx -> revealCalled.compareAndSet(false, true);
        SpecExecutor strikeExec = ctx -> strikeCalled.compareAndSet(false, true);
        SpecExecutor clearExec = ctx -> clearCalled.compareAndSet(false, true);
        SpecExecutor addExec = ctx -> addCalled.compareAndSet(false, true);
        SpecExecutor controlExec = ctx -> controlCalled.compareAndSet(false, true);
        SpecExecutor awardExec = ctx -> awardCalled.compareAndSet(false, true);
        SpecExecutor resetExec = ctx -> resetCalled.compareAndSet(false, true);
        SpecExecutor clickExec = ctx -> clickCalled.compareAndSet(false, true);

        FeudCommandSpecificationFactory factory = new FeudCommandSpecificationFactory();
        CommandSpecificationNode root = factory.buildUiSpecification(
                "familyfeud.host",
                rootExec,
                helpExec,
                versionExec,
                uiExec,
                revealExec,
                strikeExec,
                clearExec,
                addExec,
                controlExec,
                awardExec,
                resetExec,
                clickExec);

        assertEquals("feud", root.name());
        assertTrue(root.executor().isPresent());
        assertEquals(3, root.children().size());

        CommandSpecificationNode ui = root.children().get(2);
        assertEquals("ui", ui.name());
        assertTrue(ui.executor().isPresent());
        assertEquals(1, ui.requirements().size());
        assertEquals(8, ui.children().size());

        List<String> childNames =
                ui.children().stream().map(CommandSpecificationNode::name).toList();
        assertTrue(childNames.containsAll(
                List.of("reveal", "strike", "clearstrikes", "add", "control", "award", "reset", "click")));

        CommandSpecificationNode revealNode = ui.children().get(childNames.indexOf("reveal"));
        assertEquals(1, revealNode.children().size());
        assertEquals(ArgType.INT, revealNode.children().get(0).type());

        CommandSpecificationNode addNode = ui.children().get(childNames.indexOf("add"));
        assertEquals(1, addNode.children().size());
        assertEquals(ArgType.INT, addNode.children().get(0).type());

        CommandSpecificationNode controlNode = ui.children().get(childNames.indexOf("control"));
        assertEquals(1, controlNode.children().size());
        assertEquals(ArgType.WORD, controlNode.children().get(0).type());

        CommandSpecificationNode clickNode = ui.children().get(childNames.indexOf("click"));
        assertEquals(1, clickNode.children().size());
        assertEquals(ArgType.WORD, clickNode.children().get(0).type());
    }
}
