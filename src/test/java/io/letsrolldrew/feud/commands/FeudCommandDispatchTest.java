package io.letsrolldrew.feud.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import io.letsrolldrew.feud.FeudPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FeudCommandDispatchTest {

    private ServerMock server;
    private FeudPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(FeudPlugin.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void helpCommandDisplaysHelp() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud help");

        assertTrue(handled);
        assertTrue(console.nextMessage().toLowerCase().contains("familyfeud commands"));
    }

    @Test
    void holoCommandWithoutArgsShowsUsage() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud holo");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("usage: /feud holo"));
    }
}
