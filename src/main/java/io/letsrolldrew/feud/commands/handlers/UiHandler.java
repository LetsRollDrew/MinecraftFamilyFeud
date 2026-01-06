package io.letsrolldrew.feud.commands.handlers;

import io.letsrolldrew.feud.commands.tree.CommandContext;
import io.letsrolldrew.feud.commands.tree.CommandHandler;
import io.letsrolldrew.feud.ui.HostBookAnchorStore;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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

        if (args.length < 4 || !"action".equalsIgnoreCase(args[2])) {
            // host book clicks must use action so we can rotate pages
            context.sender().sendMessage("Usage: /feud ui click <page> action <actionId>");
            return true;
        }

        HostBookPage page = HostBookPage.fromToken(args[1]);
        if (page != null) {
            anchorStore.set(player.getUniqueId(), page);
        }

        String actionId = buildRemainder(args, 3);
        if (actionId.isBlank()) {
            player.sendMessage("Usage: /feud ui click <page> action <actionId>");
            return true;
        }

        player.sendMessage("Unknown UI action: " + actionId);
        refresher.accept(player);
        return true;
    }

    private String buildRemainder(String[] args, int startIndex) {
        if (args.length <= startIndex) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                sb.append(' ');
            }
            sb.append(args[i]);
        }

        return sb.toString().trim();
    }
}
