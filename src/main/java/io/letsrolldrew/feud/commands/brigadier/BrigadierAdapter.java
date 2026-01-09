package io.letsrolldrew.feud.commands.brigadier;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.letsrolldrew.feud.commands.spec.ArgType;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.Requirement;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.List;
import java.util.Objects;
import org.bukkit.command.CommandSender;

// Builds a Brigadier command tree from a CommandSpecificationNode

public final class BrigadierAdapter {

    public LiteralCommandNode<CommandSourceStack> build(CommandSpecificationNode rootSpec) {
        Objects.requireNonNull(rootSpec, "rootSpec");

        if (rootSpec.type() != ArgType.LITERAL) {
            throw new IllegalArgumentException("Root command must be a literal");
        }

        ArgumentBuilder<CommandSourceStack, ?> builder = createBuilder(rootSpec);
        if (!(builder instanceof LiteralArgumentBuilder)) {
            throw new IllegalStateException("Root builder must be a LiteralArgumentBuilder");
        }

        @SuppressWarnings("unchecked")
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder =
                (LiteralArgumentBuilder<CommandSourceStack>) builder;

        return literalBuilder.build();
    }

    private ArgumentBuilder<CommandSourceStack, ?> createBuilder(CommandSpecificationNode spec) {
        ArgumentBuilder<CommandSourceStack, ?> builder = createNodeBuilder(spec);

        applyRequirements(builder, spec.requirements());
        attachChildren(builder, spec.children());

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
            ArgumentBuilder<CommandSourceStack, ?> parent, List<CommandSpecificationNode> children) {
        for (CommandSpecificationNode child : children) {
            ArgumentBuilder<CommandSourceStack, ?> childBuilder = createBuilder(child);
            parent.then(childBuilder);
        }
    }

    /**
     * Convenience to inspect the built tree in tests.
     */
    public static CommandNode<CommandSourceStack> findChild(CommandNode<CommandSourceStack> node, String name) {
        return node.getChildren().stream()
                .filter(child -> child.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
