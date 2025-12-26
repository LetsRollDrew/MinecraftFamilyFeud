package io.letsrolldrew.feud.commands.handlers;

import io.letsrolldrew.feud.commands.tree.CommandContext;
import io.letsrolldrew.feud.commands.tree.CommandHandler;
import java.util.Objects;
import java.util.function.BiFunction;

public final class HostHandler implements CommandHandler {
    private final BiFunction<CommandContext, String, Boolean> delegate;

    public HostHandler(BiFunction<CommandContext, String, Boolean> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public boolean handle(CommandContext context, String[] remainingArgs) {
        String flavor = remainingArgs.length >= 1 ? remainingArgs[0] : "";
        return delegate.apply(context, flavor);
    }
}
