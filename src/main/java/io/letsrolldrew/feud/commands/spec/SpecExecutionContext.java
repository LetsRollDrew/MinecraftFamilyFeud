package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.command.CommandSender;

// context passed to SpecExecutor when executing a command

public final class SpecExecutionContext {
    private final CommandSender sender;
    private final String label;
    private final List<String> arguments;
    private final Map<String, Object> parsedArguments;

    public SpecExecutionContext(
            CommandSender sender, String label, List<String> arguments, Map<String, Object> parsedArguments) {
        this.sender = Objects.requireNonNull(sender, "sender");
        this.label = Objects.requireNonNull(label, "label");
        this.arguments = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(arguments, "arguments")));
        this.parsedArguments = Collections.unmodifiableMap(Objects.requireNonNull(parsedArguments, "parsedArguments"));
    }

    public CommandSender sender() {
        return sender;
    }

    public String label() {
        return label;
    }

    public List<String> arguments() {
        return arguments;
    }

    public Map<String, Object> parsedArguments() {
        return parsedArguments;
    }

    public String[] argumentArray() {
        return arguments.toArray(String[]::new);
    }

    public Optional<Object> parsedArgument(String name) {
        Objects.requireNonNull(name, "name");
        return Optional.ofNullable(parsedArguments.get(name));
    }
}
