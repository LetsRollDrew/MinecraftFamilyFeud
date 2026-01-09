package io.letsrolldrew.feud.commands.brigadier;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.letsrolldrew.feud.commands.spec.ArgType;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.GreedyToken;
import io.letsrolldrew.feud.commands.spec.Requirement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;

// builds a Brigadier command tree from a CommandSpecificationNode

public final class BrigadierAdapter {

    public LiteralCommandNode<CommandSourceStack> buildWithExecution(
            CommandSpecificationNode rootSpec, BrigadierExecution execution) {
        Objects.requireNonNull(rootSpec, "rootSpec");
        Objects.requireNonNull(execution, "execution");

        ArgumentBuilder<CommandSourceStack, ?> builder = createBuilder(rootSpec, new ArrayList<>(), execution, true);
        if (!(builder instanceof LiteralArgumentBuilder)) {
            throw new IllegalStateException("Root builder must be a LiteralArgumentBuilder");
        }

        @SuppressWarnings("unchecked")
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder =
                (LiteralArgumentBuilder<CommandSourceStack>) builder;

        return literalBuilder.build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> createBuilder(
            CommandSpecificationNode spec,
            List<TokenExtractor> pathExtractors,
            BrigadierExecution execution,
            boolean isRoot) {
        ArgumentBuilder<CommandSourceStack, ?> builder = createNodeBuilder(spec);

        List<TokenExtractor> withCurrent = new ArrayList<>(pathExtractors);
        if (!isRoot) {
            withCurrent.add(extractorFor(spec));
        }

        applyRequirements(builder, spec.requirements());
        attachChildren(builder, spec.children(), withCurrent, execution);

        if (execution != null && spec.executes()) {
            builder.executes(ctx -> execution.run(ctx.getSource(), materializeArgs(withCurrent, ctx)));
        }

        return builder;
    }

    private ArgumentBuilder<CommandSourceStack, ?> createNodeBuilder(CommandSpecificationNode spec) {
        return switch (spec.type()) {
            case LITERAL -> LiteralArgumentBuilder.<CommandSourceStack>literal(spec.name());
            case WORD ->
                RequiredArgumentBuilder.<CommandSourceStack, String>argument(spec.name(), StringArgumentType.word());
            case INT ->
                RequiredArgumentBuilder.<CommandSourceStack, Integer>argument(
                        spec.name(), IntegerArgumentType.integer());
            case GREEDY ->
                RequiredArgumentBuilder.<CommandSourceStack, String>argument(
                        spec.name(), StringArgumentType.greedyString());
        };
    }

    private void applyRequirements(ArgumentBuilder<CommandSourceStack, ?> builder, List<Requirement> requirements) {
        if (requirements.isEmpty()) {
            return;
        }

        builder.requires(source -> {
            CommandSender sender = source.getSender();

            for (Requirement requirement : requirements) {
                if (!requirement.test(sender)) {
                    return false;
                }
            }

            return true;
        });
    }

    private void attachChildren(
            ArgumentBuilder<CommandSourceStack, ?> parent,
            List<CommandSpecificationNode> children,
            List<TokenExtractor> pathExtractors,
            BrigadierExecution execution) {
        ensureNoDuplicateLiterals(children);
        for (CommandSpecificationNode child : children) {
            ArgumentBuilder<CommandSourceStack, ?> childBuilder =
                    createBuilder(child, pathExtractors, execution, false);
            parent.then(childBuilder);
        }
    }

    // to inspect the built tree in tests

    public static CommandNode<CommandSourceStack> findChild(CommandNode<CommandSourceStack> node, String name) {
        return node.getChildren().stream()
                .filter(child -> child.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    private TokenExtractor extractorFor(CommandSpecificationNode spec) {
        return switch (spec.type()) {
            case LITERAL -> ctx -> List.of(spec.name());
            case WORD -> ctx -> List.of(StringArgumentType.getString(ctx, spec.name()));
            case INT -> ctx -> List.of(String.valueOf(IntegerArgumentType.getInteger(ctx, spec.name())));
            case GREEDY ->
                ctx -> {
                    String raw = StringArgumentType.getString(ctx, spec.name());
                    if (raw.isBlank()) {
                        return List.of();
                    }
                    if (spec.greedyToken() == GreedyToken.RAW_SINGLE_ARG) {
                        return List.of(raw);
                    }
                    return Arrays.asList(raw.split(" "));
                };
        };
    }

    private List<String> materializeArgs(List<TokenExtractor> extractors, CommandContext<CommandSourceStack> ctx) {
        List<String> args = new ArrayList<>();
        for (TokenExtractor extractor : extractors) {
            args.addAll(extractor.extract(ctx));
        }
        return args;
    }

    private void ensureNoDuplicateLiterals(List<CommandSpecificationNode> children) {
        List<String> names = new ArrayList<>();
        for (CommandSpecificationNode child : children) {
            if (child.type() == ArgType.LITERAL) {
                String name = child.name().toLowerCase();
                if (names.contains(name)) {
                    throw new IllegalStateException("Duplicate literal child: " + child.name());
                }
                names.add(name);
            }
        }
    }

    @FunctionalInterface
    public interface BrigadierExecution {
        int run(CommandSourceStack source, List<String> args);
    }

    private interface TokenExtractor {
        List<String> extract(CommandContext<CommandSourceStack> ctx);
    }
}
