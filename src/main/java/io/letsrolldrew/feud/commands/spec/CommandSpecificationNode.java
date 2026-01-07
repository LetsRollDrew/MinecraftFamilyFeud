package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// represents a single node in the command specification tree

public final class CommandSpecificationNode {
    private final ArgType type;
    private final String name;
    private final List<Requirement> requirements;
    private final SpecExecutor executor;
    private final List<CommandSpecificationNode> children;

    private CommandSpecificationNode(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.requirements = Collections.unmodifiableList(new ArrayList<>(builder.requirements));
        this.executor = builder.executor;
        this.children = Collections.unmodifiableList(new ArrayList<>(builder.children));
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

    public Optional<SpecExecutor> executor() {
        return Optional.ofNullable(executor);
    }

    public List<CommandSpecificationNode> children() {
        return children;
    }

    public static Builder builder(ArgType type, String name) {
        return new Builder(type, name);
    }

    public static final class Builder {
        private final ArgType type;
        private final String name;
        private final List<Requirement> requirements = new ArrayList<>();
        private final List<CommandSpecificationNode> children = new ArrayList<>();
        private SpecExecutor executor;

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

        public Builder executor(SpecExecutor executor) {
            this.executor = Objects.requireNonNull(executor, "executor");
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
