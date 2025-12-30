package io.letsrolldrew.feud.commands.tree;

import org.bukkit.command.CommandSender;

public record CommandContext(CommandSender sender, String label, String[] args) {}
