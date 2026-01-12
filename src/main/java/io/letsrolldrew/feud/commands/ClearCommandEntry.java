package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.board.display.panels.TimerPanelStore;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.effects.holo.HologramService;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public final class ClearCommandEntry {
    private final Plugin plugin;
    private final String adminPermission;
    private final DisplayRegistry displayRegistry;
    private final DisplayBoardService displayBoardPresenter;
    private final HologramService hologramService;
    private final ScorePanelStore scorePanelStore;
    private final TimerPanelStore timerPanelStore;

    public ClearCommandEntry(
            Plugin plugin,
            String adminPermission,
            DisplayRegistry displayRegistry,
            DisplayBoardService displayBoardPresenter,
            HologramService hologramService,
            ScorePanelStore scorePanelStore,
            TimerPanelStore timerPanelStore) {
        this.plugin = plugin;
        this.adminPermission = adminPermission;
        this.displayRegistry = displayRegistry;
        this.displayBoardPresenter = displayBoardPresenter;
        this.hologramService = hologramService;
        this.scorePanelStore = scorePanelStore;
        this.timerPanelStore = timerPanelStore;
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (args.length >= 1 && "all".equalsIgnoreCase(args[0])) {
            return handleClearAll(sender);
        }
        sender.sendMessage("Usage: /feud clear all");
        return true;
    }

    private boolean handleClearAll(CommandSender sender) {
        if (!sender.hasPermission(adminPermission)) {
            sender.sendMessage("Admin only");
            return true;
        }

        int removed = displayRegistry.removeAll();
        removed += removeTaggedDisplays("board");

        displayBoardPresenter.clearAll();
        hologramService.clearAll();
        if (scorePanelStore != null) {
            scorePanelStore.clear();
        }
        if (timerPanelStore != null) {
            timerPanelStore.clear();
        }

        sender.sendMessage("Cleared " + removed + " displays");
        return true;
    }

    private int removeTaggedDisplays(String kind) {
        int removed = 0;
        for (var world : plugin.getServer().getWorlds()) {
            for (org.bukkit.entity.Display display : world.getEntitiesByClass(org.bukkit.entity.Display.class)) {
                if (DisplayTags.isManaged(display, kind)) {
                    display.remove();
                    removed++;
                }
            }
        }
        return removed;
    }
}
