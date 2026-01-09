package io.letsrolldrew.feud.commands.brigadier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.letsrolldrew.feud.commands.brigadier.BrigadierAdapter.BrigadierExecution;
import io.letsrolldrew.feud.commands.spec.ArgType;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.Requirement;
import io.letsrolldrew.feud.commands.spec.Requirements;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BrigadierAdapterTest {

    @Test
    void buildsLiteralTreeFromSpecification() {
        CommandSpecificationNode help =
                CommandSpecificationNode.builder(ArgType.LITERAL, "help").build();

        CommandSpecificationNode root = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(help)
                .build();

        BrigadierAdapter adapter = new BrigadierAdapter();
        LiteralCommandNode<CommandSourceStack> node = adapter.build(root);

        assertEquals("feud", node.getName());
        assertNotNull(BrigadierAdapter.findChild(node, "help"));
    }

    @Test
    void appliesRequirementsToNodes() {
        Requirement adminOnly = Requirements.permission("familyfeud.admin");

        CommandSpecificationNode admin = CommandSpecificationNode.builder(ArgType.LITERAL, "admin")
                .requirement(adminOnly)
                .build();

        CommandSpecificationNode root = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(admin)
                .build();

        BrigadierAdapter adapter = new BrigadierAdapter();
        LiteralCommandNode<CommandSourceStack> node = adapter.build(root);

        CommandSourceStack source = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        CommandSender sender = mock(CommandSender.class);
        when(source.getSender()).thenReturn(sender);

        CommandSourceStack sourceWithPerm = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        CommandSender senderWithPerm = mock(CommandSender.class);
        when(sourceWithPerm.getSender()).thenReturn(senderWithPerm);
        when(senderWithPerm.hasPermission("familyfeud.admin")).thenReturn(true);

        assertFalse(node.getChild("admin").getRequirement().test(source));
        assertTrue(node.getChild("admin").getRequirement().test(sourceWithPerm));
    }

    @Test
    void executesWithReconstructedArgs() {
        CommandSpecificationNode arg = CommandSpecificationNode.builder(ArgType.WORD, "target")
                .executor(ctx -> true)
                .build();

        CommandSpecificationNode root = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "test")
                        .child(arg)
                        .executor(ctx -> true)
                        .build())
                .executor(ctx -> true)
                .build();

        BrigadierExecution execution = (source, args) -> {
            assertEquals(List.of("feud", "test", "value"), args);
            return 1;
        };

        BrigadierAdapter adapter = new BrigadierAdapter();
        LiteralCommandNode<CommandSourceStack> node = adapter.buildWithExecution(root, execution);

        CommandSourceStack source = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        when(source.getSender()).thenReturn(mock(CommandSender.class));

        var dispatcher = new com.mojang.brigadier.CommandDispatcher<CommandSourceStack>();
        dispatcher.getRoot().addChild(node);

        try {
            dispatcher.execute("feud test value", source);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException ex) {
            throw new AssertionError("Dispatch failed", ex);
        }
    }
}
