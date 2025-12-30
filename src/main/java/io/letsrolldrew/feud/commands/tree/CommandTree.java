package io.letsrolldrew.feud.commands.tree;

import java.util.Arrays;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandTree {
    private final CommandNode root;
    private final CommandHandler helpHandler;

    public CommandTree(CommandNode root, CommandHandler helpHandler) {
        this.root = Objects.requireNonNull(root, "root");
        this.helpHandler = helpHandler;
    }

    public boolean dispatch(CommandSender sender, String label, String[] args) {
        if (sender == null) {
            return false;
        }
        CommandContext context = new CommandContext(sender, label, args);
        CommandNode current = root;
        int index = 0;

        while (index < args.length) {
            String token = args[index];
            CommandNode next = current.childFor(token);
            if (next == null) {
                return handleHelp(context);
            }
            if (!next.isPermitted(sender)) {
                sender.sendMessage("Admin Only");
                return true;
            }
            if (next.requiresPlayer() && !(sender instanceof Player)) {
                sender.sendMessage("Must be a player");
                return true;
            }
            current = next;
            index++;

            // stop walking if we hit a leaf with a handler so it can consume remaining args
            // b/c otherwise extra args would bypass the handler and trigger help instead
            if (current.handler() != null && current.children().isEmpty()) {
                break;
            }
        }

        String[] remaining = Arrays.copyOfRange(args, index, args.length);
        if (current.handler() != null) {
            return current.handler().handle(context, remaining);
        }
        return handleHelp(context);
    }

    public CommandNode root() {
        return root;
    }

    private boolean handleHelp(CommandContext context) {
        if (helpHandler != null) {
            return helpHandler.handle(context, new String[0]);
        }
        return false;
    }
}
