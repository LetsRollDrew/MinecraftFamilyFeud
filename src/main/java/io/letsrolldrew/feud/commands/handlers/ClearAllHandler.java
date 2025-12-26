package io.letsrolldrew.feud.commands.handlers;

import io.letsrolldrew.feud.commands.tree.CommandContext;
import io.letsrolldrew.feud.commands.tree.CommandHandler;
import java.util.Objects;
import java.util.function.Function;

public final class ClearAllHandler implements CommandHandler {
    private final Function<CommandContext, Boolean> delegate;

    public ClearAllHandler(Function<CommandContext, Boolean> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean handle(CommandContext context, String[] remainingArgs) {
        return delegate.apply(context);
    }
}
