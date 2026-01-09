package io.letsrolldrew.feud.commands.brigadier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mojang.brigadier.tree.CommandNode;
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

    @Test
    void enforcesRequirementsForBoardDisplayRemote() {
        CommandSpecificationNode remote = CommandSpecificationNode.builder(ArgType.LITERAL, "remote")
                .requirements(List.of(Requirements.permission("familyfeud.host"), Requirements.playerOnly()))
                .executor(ctx -> true)
                .build();

        CommandSpecificationNode display = CommandSpecificationNode.builder(ArgType.LITERAL, "display")
                .child(remote)
                .build();

        CommandSpecificationNode board = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .child(display)
                .build();

        CommandSpecificationNode root = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(board)
                .build();

        BrigadierAdapter adapter = new BrigadierAdapter();
        LiteralCommandNode<CommandSourceStack> node = adapter.build(root);

        CommandNode<CommandSourceStack> remoteNode =
                BrigadierAdapter.findChild(BrigadierAdapter.findChild(node, "board"), "display")
                        .getChild("remote");

        CommandSourceStack consoleSource = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        CommandSender console = mock(CommandSender.class);
        when(console.hasPermission("familyfeud.host")).thenReturn(true);
        when(consoleSource.getSender()).thenReturn(console);

        CommandSourceStack playerNoPerm = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        CommandSender player = mock(org.bukkit.entity.Player.class);
        when(player.hasPermission("familyfeud.host")).thenReturn(false);
        when(playerNoPerm.getSender()).thenReturn(player);

        CommandSourceStack playerWithPerm = mock(CommandSourceStack.class, Mockito.RETURNS_DEEP_STUBS);
        CommandSender playerPerm = mock(org.bukkit.entity.Player.class);
        when(playerPerm.hasPermission("familyfeud.host")).thenReturn(true);
        when(playerWithPerm.getSender()).thenReturn(playerPerm);

        assertFalse(remoteNode.getRequirement().test(consoleSource));
        assertFalse(remoteNode.getRequirement().test(playerNoPerm));
        assertTrue(remoteNode.getRequirement().test(playerWithPerm));
    }
}
