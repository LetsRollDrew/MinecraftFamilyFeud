package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// represents a single node in the command specification tree

public final class CommandSpecificationNode {
    private final ArgType type;
    private final String name;
    private final List<Requirement> requirements;
    private final List<CommandSpecificationNode> children;
    private final boolean executes;
    private final GreedyToken greedyToken;

    private CommandSpecificationNode(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.requirements = Collections.unmodifiableList(new ArrayList<>(builder.requirements));
        this.children = Collections.unmodifiableList(new ArrayList<>(builder.children));
        this.executes = builder.executes;
        this.greedyToken = builder.greedyToken;
    }

    public ArgType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public List<Requirement> requirements() {
        return requirements;
    }

    public List<CommandSpecificationNode> children() {
        return children;
    }

    public boolean executes() {
        return executes;
    }

    public GreedyToken greedyToken() {
        return greedyToken;
    }

    public static Builder builder(ArgType type, String name) {
        return new Builder(type, name);
    }

    // for creating separate literal nodes for the same command action

    public static List<CommandSpecificationNode> literalAliases(String firstLiteral, String... otherLiterals) {
        Objects.requireNonNull(firstLiteral, "firstLiteral");

        List<CommandSpecificationNode> nodes = new ArrayList<>();
        nodes.add(
                CommandSpecificationNode.builder(ArgType.LITERAL, firstLiteral).build());

        if (otherLiterals != null) {
            for (String literal : otherLiterals) {
                nodes.add(CommandSpecificationNode.builder(ArgType.LITERAL, Objects.requireNonNull(literal, "literal"))
                        .build());
            }
        }

        return nodes;
    }

    public static final class Builder {
        private final ArgType type;
        private final String name;
        private final List<Requirement> requirements = new ArrayList<>();
        private final List<CommandSpecificationNode> children = new ArrayList<>();
        private boolean executes = true;
        private GreedyToken greedyToken = GreedyToken.SPLIT_SPACES;

        private Builder(ArgType type, String name) {
            this.type = Objects.requireNonNull(type, "type");
            this.name = Objects.requireNonNull(name, "name");
        }

        public Builder requirement(Requirement requirement) {
            requirements.add(Objects.requireNonNull(requirement, "requirement"));
            return this;
        }

        public Builder requirements(List<Requirement> requirements) {
            Objects.requireNonNull(requirements, "requirements");
            for (Requirement requirement : requirements) {
                requirement(requirement);
            }

            return this;
        }

        public Builder noExec() {
            this.executes = false;
            return this;
        }

        public Builder greedyRawSingleArg() {
            this.greedyToken = GreedyToken.RAW_SINGLE_ARG;
            return this;
        }

        public Builder greedySplitSpaces() {
            this.greedyToken = GreedyToken.SPLIT_SPACES;
            return this;
        }

        public Builder child(CommandSpecificationNode child) {
            children.add(Objects.requireNonNull(child, "child"));
            return this;
        }

        public Builder children(List<CommandSpecificationNode> children) {
            Objects.requireNonNull(children, "children");
            for (CommandSpecificationNode child : children) {
                child(child);
            }

            return this;
        }

        public CommandSpecificationNode build() {
            return new CommandSpecificationNode(this);
        }
    }
}
