package io.letsrolldrew.feud.commands.handlers;

import io.letsrolldrew.feud.commands.tree.CommandContext;
import io.letsrolldrew.feud.commands.tree.CommandHandler;
import java.util.Objects;
import java.util.function.BiFunction;

public final class BuzzHandler implements CommandHandler {
    private final BiFunction<CommandContext, String[], Boolean> delegate;

    public BuzzHandler(BiFunction<CommandContext, String[], Boolean> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean handle(CommandContext context, String[] remainingArgs) {
        return delegate.apply(context, remainingArgs);
    }
}
