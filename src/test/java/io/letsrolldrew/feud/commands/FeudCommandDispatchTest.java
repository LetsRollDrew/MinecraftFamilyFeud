package io.letsrolldrew.feud.commands;

import static org.junit.jupiter.api.Assertions.assertTrue;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.letsrolldrew.feud.FeudPlugin;
import java.io.File;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;
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
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud help");

        assertTrue(handled);

        String all = sender.joinedLower();
        assertTrue(all.contains("familyfeud commands"));
    }

    @Test
    void holoCommandWithoutArgsShowsUsage() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud holo");

        assertTrue(handled);

        String all = sender.joinedLower();
        assertTrue(all.contains("usage: /feud holo"));
    }

    @Test
    void teamCommandIsReachable() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud team info");

        assertTrue(handled);

        String all = sender.joinedLower();
        assertTrue(all.contains("teams") || all.contains("host") || all.contains("usage"));
    }

    @Test
    void timerCommandIsReachable() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud timer status");

        assertTrue(handled);

        String all = sender.joinedLower();
        assertTrue(all.contains("timer") || all.contains("status") || all.contains("usage"));
    }

    @Test
    void buzzCommandIsReachable() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud buzz reset");

        assertTrue(handled);
    }

    @Test
    void fastMoneyStatusIsReachable() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud fastmoney status");

        assertTrue(handled);

        String all = sender.joinedLower();
        assertTrue(all.contains("fast money") || all.contains("fastmoney") || all.contains("usage"));
    }

    @Test
    void fastMoneyBindP1IsHandledOnNonPlayer() {
        CapturingSender sender = CapturingSender.allowAllPerms();

        boolean handled = server.dispatchCommand(sender.sender(), "feud fastmoney bind p1");

        assertTrue(handled);
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

    private static final class CapturingSender {
        private final List<String> messages = new ArrayList<>();
        private final Set<String> perms;
        private final boolean allowAll;

        private CapturingSender(Set<String> perms, boolean allowAll) {
            this.perms = perms;
            this.allowAll = allowAll;
        }

        static CapturingSender allowAllPerms() {
            return new CapturingSender(Set.of(), true);
        }

        CommandSender sender() {
            return (CommandSender) Proxy.newProxyInstance(
                    FeudCommandDispatchTest.class.getClassLoader(),
                    new Class<?>[] {CommandSender.class},
                    (proxy, method, args) -> {
                        String name = method.getName();

                        // permissions
                        if (name.equals("hasPermission") && args != null && args.length == 1) {
                            if (allowAll) {
                                return true;
                            }
                            return perms.contains(String.valueOf(args[0]));
                        }

                        if (name.equals("isOp")) {
                            return true;
                        }

                        // message capture
                        if (name.equals("sendMessage")) {
                            if (args != null && args.length >= 1) {
                                Object a0 = args[0];

                                if (a0 instanceof String s) {
                                    messages.add(s);
                                } else if (a0 instanceof String[] arr) {
                                    for (String s : arr) {
                                        if (s != null) {
                                            messages.add(s);
                                        }
                                    }
                                }
                            }
                            return null;
                        }

                        // identity
                        if (name.equals("getName")) {
                            return "CapturingSender";
                        }

                        // defaults
                        Class<?> rt = method.getReturnType();
                        if (rt == boolean.class) return false;
                        if (rt == int.class) return 0;
                        if (rt == long.class) return 0L;
                        if (rt == float.class) return 0f;
                        if (rt == double.class) return 0d;

                        return null;
                    });
        }

        String joinedLower() {
            return String.join("\n", messages).toLowerCase();
        }
    }
}
