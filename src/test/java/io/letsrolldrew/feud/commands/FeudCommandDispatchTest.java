package io.letsrolldrew.feud.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.command.ConsoleCommandSenderMock;
import io.letsrolldrew.feud.FeudPlugin;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FeudCommandDispatchTest {

    private ServerMock server;
    private FeudPlugin plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        writeFastMoneyConfig();
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

    @Test
    void teamCommandIsReachable() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud team info");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("teams") || msg.contains("host"));
    }

    @Test
    void timerCommandIsReachable() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud timer status");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("timer"));
    }

    @Test
    void buzzCommandIsReachable() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud buzz reset");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("buzz"));
    }

    @Test
    void fastMoneyStatusIsReachable() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud fastmoney status");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("fast money"));
    }

    @Test
    void fastMoneyBindP1IsReachableForPlayer() {
        ConsoleCommandSenderMock console = server.getConsoleSender();

        boolean handled = server.dispatchCommand(console, "feud fastmoney bind p1");

        assertTrue(handled);
        String msg = console.nextMessage().toLowerCase();
        assertTrue(msg.contains("only players"));
    }

    private void writeFastMoneyConfig() {
        try {
            File dataDir = new File(server.getPluginsFolder(), "FamilyFeud");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            File file = new File(dataDir, "fast-money.yml");
            String contents = """
                    fastMoney:
                      packs:
                        s1:
                          targetScore: 200
                          player1Seconds: 20
                          player2Seconds: 25
                          surveys: ["example_animals", "example_breakfast", "block_taste", "suspicious_animal", "build_materials"]
                    """;
            Files.writeString(file.toPath(), contents, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }
}
