package io.letsrolldrew.feud.commands.spec;

@FunctionalInterface
public interface SpecExecutor {
    boolean execute(SpecExecutionContext context);
}
