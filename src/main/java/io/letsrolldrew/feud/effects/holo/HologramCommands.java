package io.letsrolldrew.feud.effects.holo;

import org.bukkit.command.CommandSender;

//Handles /feud holo subcommands

public final class HologramCommands {
    private final HologramService service;

    public HologramCommands(HologramService service) {
        this.service = service;
    }

    // return true if handled, false to signal help to caller.
    public boolean handle(CommandSender sender, String[] args) {
        sender.sendMessage("Usage: /feud holo <spawn|set|move|remove|list>");
        return true;
    }
}
