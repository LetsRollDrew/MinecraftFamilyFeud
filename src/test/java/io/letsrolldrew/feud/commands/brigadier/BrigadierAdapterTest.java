package io.letsrolldrew.feud.commands.brigadier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.letsrolldrew.feud.commands.spec.ArgType;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.Requirements;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

final class BrigadierAdapterTest {

    @Test
    void executingRootPassesEmptyArgs() throws Exception {
        CommandSpecificationNode spec =
                CommandSpecificationNode.builder(ArgType.LITERAL, "feud").build();

        AtomicReference<List<String>> captured = new AtomicReference<>();
        var root = new BrigadierAdapter().buildWithExecution(spec, (source, args) -> {
            captured.set(args);
            return 1;
        });

        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);

        dispatcher.execute("feud", stack(sender(false, Set.of())));

        assertNotNull(captured.get());
        assertEquals(List.of(), captured.get(), "root command should pass args=[] (label excluded)");
    }

    @Test
    void executingLeafOmitsRootLabel() throws Exception {
        CommandSpecificationNode spec = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "ui")
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "strike")
                                .build())
                        .build())
                .build();

        AtomicReference<List<String>> captured = new AtomicReference<>();
        var root = new BrigadierAdapter().buildWithExecution(spec, (source, args) -> {
            captured.set(args);
            return 1;
        });

        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);

        dispatcher.execute("feud ui strike", stack(sender(false, Set.of())));

        assertEquals(List.of("ui", "strike"), captured.get(), "args must not include 'feud'");
    }

    @Test
    void greedyRawSingleArgIsPreserved() throws Exception {
        CommandSpecificationNode spec = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "ui")
                        .requirements(List.of(Requirements.permission("host.perm")))
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "click")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "page")
                                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "action")
                                                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "actionId")
                                                        .greedyRawSingleArg()
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();

        AtomicReference<List<String>> captured = new AtomicReference<>();
        var root = new BrigadierAdapter().buildWithExecution(spec, (source, args) -> {
            captured.set(args);
            return 1;
        });

        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);

        // must be a Player + must have host permission
        dispatcher.execute("feud ui click 2 action hello world", stack(sender(true, Set.of("host.perm"))));

        assertEquals(
                List.of("ui", "click", "2", "action", "hello world"),
                captured.get(),
                "RAW_SINGLE_ARG should keep greedy string as one arg");
    }

    @Test
    void greedySplitSpacesMatchesBukkitStyle() throws Exception {
        CommandSpecificationNode spec = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "fastmoney")
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "answer")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "text")
                                        .build())
                                .build())
                        .build())
                .build();

        AtomicReference<List<String>> captured = new AtomicReference<>();
        var root = new BrigadierAdapter().buildWithExecution(spec, (source, args) -> {
            captured.set(args);
            return 1;
        });

        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);

        dispatcher.execute("feud fastmoney answer hello world", stack(sender(true, Set.of())));

        assertEquals(
                List.of("fastmoney", "answer", "hello", "world"),
                captured.get(),
                "SPLIT_SPACES should split greedy into Bukkit-style tokens");
    }

    @Test
    void requirementDeniesWhenPermissionMissing() {
        CommandSpecificationNode spec = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "secret")
                        .requirements(List.of(Requirements.permission("perm.secret")))
                        .build())
                .build();

        var root = new BrigadierAdapter().buildWithExecution(spec, (source, args) -> 1);

        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        dispatcher.getRoot().addChild(root);

        assertThrows(
                CommandSyntaxException.class,
                () -> dispatcher.execute("feud secret", stack(sender(false, Set.of()))),
                "without perm, branch should not parse");
    }

    @Test
    void duplicateLiteralChildrenThrow() {
        CommandSpecificationNode spec = CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "dup").build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "dup").build()))
                .build();

        assertThrows(
                IllegalStateException.class,
                () -> new BrigadierAdapter().buildWithExecution(spec, (source, args) -> 1),
                "adapter should throw when duplicate literal siblings exist");
    }

    private static CommandSourceStack stack(CommandSender sender) {
        return new TestStack(sender, new Location(null, 0, 0, 0), null);
    }

    private static CommandSender sender(boolean asPlayer, Set<String> perms) {
        Class<?>[] ifaces = asPlayer ? new Class<?>[] {Player.class} : new Class<?>[] {CommandSender.class};

        return (CommandSender)
                Proxy.newProxyInstance(BrigadierAdapterTest.class.getClassLoader(), ifaces, (proxy, method, args) -> {
                    String name = method.getName();

                    if (name.equals("hasPermission") && args != null && args.length == 1) {
                        return perms.contains(String.valueOf(args[0]));
                    }

                    if (name.equals("isPermissionSet")) {
                        return true;
                    }

                    if (name.equals("getName")) {
                        return "TestSender";
                    }

                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }

                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }

                    if (method.getReturnType().equals(long.class)) {
                        return 0L;
                    }

                    if (method.getReturnType().equals(float.class)) {
                        return 0f;
                    }

                    if (method.getReturnType().equals(double.class)) {
                        return 0d;
                    }

                    return null;
                });
    }

    private static final class TestStack implements CommandSourceStack {
        private final CommandSender sender;
        private final Location location;
        private final Entity executor;

        private TestStack(CommandSender sender, Location location, Entity executor) {
            this.sender = sender;
            this.location = location;
            this.executor = executor;
        }

        @Override
        public Location getLocation() {
            return location.clone();
        }

        @Override
        public CommandSender getSender() {
            return sender;
        }

        @Override
        public Entity getExecutor() {
            return executor;
        }

        @Override
        public CommandSourceStack withLocation(Location location) {
            return new TestStack(sender, location, executor);
        }

        @Override
        public CommandSourceStack withExecutor(Entity executor) {
            return new TestStack(sender, location, executor);
        }
    }
}
