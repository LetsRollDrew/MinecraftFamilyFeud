package io.letsrolldrew.feud.commands.tree;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandNode {
    private final String literal;
    private final String permission;
    private final boolean playerOnly;
    private final CommandHandler handler;
    private final Map<String, CommandNode> children = new HashMap<>();

    public CommandNode(String literal) {
        this(literal, null, false, null);
    }

    public CommandNode(String literal, String permission, boolean playerOnly, CommandHandler handler) {
        this.literal = Objects.requireNonNull(literal, "literal").toLowerCase(Locale.ROOT);
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.handler = handler;
    }

    public CommandNode addChild(CommandNode child) {
        Objects.requireNonNull(child, "child");
        children.put(child.literal, child);
        return this;
    }

    public Map<String, CommandNode> children() {
        return Collections.unmodifiableMap(children);
    }

    public CommandHandler handler() {
        return handler;
    }

    public String permission() {
        return permission;
    }

    public boolean playerOnly() {
        return playerOnly;
    }

    public CommandNode childFor(String token) {
        if (token == null) {
            return null;
        }
        return children.get(token.toLowerCase(Locale.ROOT));
    }

    public boolean isPermitted(CommandSender sender) {
        if (permission == null || permission.isBlank()) {
            return true;
        }
        return sender.hasPermission(permission);
    }

    public boolean requiresPlayer() {
        return playerOnly;
    }

    public boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
}
