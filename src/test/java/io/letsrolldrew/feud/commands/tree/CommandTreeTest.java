package io.letsrolldrew.feud.commands.tree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

final class CommandTreeTest {

    @Test
    void dispatchRoutesToLeafAndPassesRemainingArgs() {
        CommandSender sender = mock(CommandSender.class);
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<String[]> remainingCapture = new AtomicReference<>();
        CommandHandler handler = (ctx, remaining) -> {
            called.set(true);
            remainingCapture.set(remaining);
            return true;
        };
        CommandNode root = new CommandNode("root", null, false, null);
        root.addChild(new CommandNode("foo", null, false, handler));
        CommandTree tree = new CommandTree(root, null);

        boolean handled = tree.dispatch(sender, "feud", new String[] {"foo", "a", "b", "c"});

        assertTrue(handled);
        assertTrue(called.get());
        assertArrayEquals(new String[] {"a", "b", "c"}, remainingCapture.get());
    }

    @Test
    void unknownPathFallsBackToHelp() {
        CommandSender sender = mock(CommandSender.class);
        AtomicBoolean helpCalled = new AtomicBoolean(false);
        CommandHandler helpHandler = (ctx, remaining) -> {
            helpCalled.set(true);
            return true;
        };
        CommandNode root = new CommandNode("root", null, false, null);
        CommandTree tree = new CommandTree(root, helpHandler);

        boolean handled = tree.dispatch(sender, "feud", new String[] {"nope"});

        assertTrue(handled);
        assertTrue(helpCalled.get());
    }

    @Test
    void permissionGuardShortCircuits() {
        CommandSender sender = mock(CommandSender.class);
        when(sender.hasPermission("perm.admin")).thenReturn(false);
        AtomicBoolean called = new AtomicBoolean(false);
        CommandHandler handler = (ctx, remaining) -> {
            called.set(true);
            return true;
        };
        CommandNode root = new CommandNode("root", null, false, null);
        root.addChild(new CommandNode("admin", "perm.admin", false, handler));
        CommandTree tree = new CommandTree(root, null);

        boolean handled = tree.dispatch(sender, "feud", new String[] {"admin"});

        assertTrue(handled);
        assertFalse(called.get());
        verify(sender).sendMessage("Admin Only");
    }

    @Test
    void playerOnlyGuardShortCircuits() {
        CommandSender sender = mock(CommandSender.class);
        AtomicBoolean called = new AtomicBoolean(false);
        CommandHandler handler = (ctx, remaining) -> {
            called.set(true);
            return true;
        };
        CommandNode root = new CommandNode("root", null, false, null);
        root.addChild(new CommandNode("player", null, true, handler));
        CommandTree tree = new CommandTree(root, null);

        boolean handled = tree.dispatch(sender, "feud", new String[] {"player"});

        assertTrue(handled);
        assertFalse(called.get());
        verify(sender).sendMessage("Must be a player");
    }

    @Test
    void playerOnlyAllowsPlayerSender() {
        Player sender = mock(Player.class);
        when(sender.hasPermission("perm.use")).thenReturn(true);
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicReference<String[]> remainingCapture = new AtomicReference<>();
        CommandHandler handler = (ctx, remaining) -> {
            called.set(true);
            remainingCapture.set(remaining);
            return true;
        };
        CommandNode root = new CommandNode("root", null, false, null);
        root.addChild(new CommandNode("player", "perm.use", true, handler));
        CommandTree tree = new CommandTree(root, null);

        boolean handled = tree.dispatch(sender, "feud", new String[] {"player", "x"});

        assertTrue(handled);
        assertTrue(called.get());
        assertArrayEquals(new String[] {"x"}, remainingCapture.get());
    }
}
