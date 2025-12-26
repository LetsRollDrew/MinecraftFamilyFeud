package io.letsrolldrew.feud.commands.tree;

public interface CommandHandler {
    boolean handle(CommandContext context, String[] remainingArgs);
}
