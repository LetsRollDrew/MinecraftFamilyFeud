package io.letsrolldrew.feud.commands.handlers;

import io.letsrolldrew.feud.commands.tree.CommandContext;
import io.letsrolldrew.feud.commands.tree.CommandHandler;
import io.letsrolldrew.feud.ui.HostBookAnchorStore;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class UiHandler implements CommandHandler {
    private final BiFunction<CommandContext, String[], Boolean> delegate;
    private final HostBookAnchorStore anchorStore;
    private final Consumer<Player> refresher;

    public UiHandler(
            BiFunction<CommandContext, String[], Boolean> delegate,
            HostBookAnchorStore anchorStore,
            Consumer<Player> refresher) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.anchorStore = Objects.requireNonNull(anchorStore, "anchorStore");
        this.refresher = Objects.requireNonNull(refresher, "refresher");
    }

    @Override
    public boolean handle(CommandContext context, String[] remainingArgs) {
        if (remainingArgs == null || remainingArgs.length == 0) {
            return delegate.apply(context, remainingArgs);
        }

        if (!"click".equalsIgnoreCase(remainingArgs[0])) {
            return delegate.apply(context, remainingArgs);
        }

        return handleClick(context, remainingArgs);
    }

    private boolean handleClick(CommandContext context, String[] args) {
        if (!(context.sender() instanceof Player player)) {
            context.sender().sendMessage("Only players can use the host book.");
            return true;
        }

        if (args.length < 3) {
            context.sender().sendMessage("Usage: /feud ui click <page> <command...>");
            return true;
        }

        HostBookPage page = HostBookPage.fromToken(args[1]);
        if (page != null) {
            anchorStore.set(player.getUniqueId(), page);
        }

        String remainder = buildRemainder(args);
        if (remainder.isBlank()) {
            return true;
        }

        Bukkit.dispatchCommand(player, "feud " + remainder);
        refresher.accept(player);
        return true;
    }

    private String buildRemainder(String[] args) {
        if (args.length < 3) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }
        return sb.toString().trim();
    }
}
