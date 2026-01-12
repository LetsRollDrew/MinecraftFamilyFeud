package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import io.letsrolldrew.feud.ui.actions.ActionIds;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HostBookActionRouter {
    private final FastMoneyCommands fastMoneyCommands;
    private final DisplayBoardSelectionStore displayBoardSelectionStore;

    public HostBookActionRouter(
            FastMoneyCommands fastMoneyCommands, DisplayBoardSelectionStore displayBoardSelectionStore) {
        this.fastMoneyCommands = fastMoneyCommands;
        this.displayBoardSelectionStore = displayBoardSelectionStore;
    }

    public boolean handle(Player player, String actionId) {
        if (player == null || actionId == null || actionId.isBlank()) {
            return false;
        }

        String normalized = actionId.toLowerCase(Locale.ROOT);

        // UI control actions
        if (normalized.startsWith("control.reveal.")) {
            String slot = normalized.substring("control.reveal.".length());
            return runPlayerCommand(player, "feud ui reveal " + slot);
        }
        if (normalized.equals(ActionIds.controlStrike())) {
            return runPlayerCommand(player, "feud ui strike");
        }
        if (normalized.equals(ActionIds.controlClearStrikes())) {
            return runPlayerCommand(player, "feud ui clearstrikes");
        }
        if (normalized.equals(ActionIds.controlControlRed())) {
            return runPlayerCommand(player, "feud ui control red");
        }
        if (normalized.equals(ActionIds.controlControlBlue())) {
            return runPlayerCommand(player, "feud ui control blue");
        }
        if (normalized.equals(ActionIds.controlAward())) {
            return runPlayerCommand(player, "feud ui award");
        }
        if (normalized.equals(ActionIds.controlReset())) {
            return runPlayerCommand(player, "feud ui reset");
        }

        // Survey load
        if (normalized.startsWith("surveys.load.")) {
            String surveyId = normalized.substring("surveys.load.".length());
            return runPlayerCommand(player, "feud survey load " + surveyId);
        }

        // Host config actions
        if (normalized.equals(ActionIds.hostConfigTeamInfo())) {
            return runPlayerCommand(player, "feud team info");
        }
        if (normalized.equals(ActionIds.hostConfigTimerStart())) {
            return runPlayerCommand(player, "feud timer start");
        }
        if (normalized.equals(ActionIds.hostConfigTimerStop())) {
            return runPlayerCommand(player, "feud timer stop");
        }
        if (normalized.equals(ActionIds.hostConfigTimerReset())) {
            return runPlayerCommand(player, "feud timer reset");
        }
        if (normalized.equals(ActionIds.hostConfigTimerStatus())) {
            return runPlayerCommand(player, "feud timer status");
        }
        if (normalized.equals(ActionIds.hostConfigBuzzReset())) {
            return runPlayerCommand(player, "feud buzz reset");
        }

        // Fast Money
        if (normalized.equals(ActionIds.fastMoneyBindP1())) {
            return runPlayerCommand(player, "feud fastmoney bind p1");
        }
        if (normalized.equals(ActionIds.fastMoneyBindP2())) {
            return runPlayerCommand(player, "feud fastmoney bind p2");
        }
        if (normalized.equals(ActionIds.fastMoneyBindClear())) {
            return runPlayerCommand(player, "feud fastmoney bind clear");
        }
        if (normalized.startsWith("fastmoney.set.")) {
            String setId = normalized.substring("fastmoney.set.".length());
            return runPlayerCommand(player, "feud fastmoney set " + setId);
        }
        if (normalized.equals(ActionIds.fastMoneyStart())) {
            return runPlayerCommand(player, "feud fastmoney start");
        }
        if (normalized.equals(ActionIds.fastMoneyStop())) {
            return runPlayerCommand(player, "feud fastmoney stop");
        }
        if (normalized.equals(ActionIds.fastMoneyStatus())) {
            return runPlayerCommand(player, "feud fastmoney status");
        }
        if (normalized.startsWith("fastmoney.reveal.")) {
            return handleFastMoneyReveal(player, normalized);
        }

        // Selector
        if (normalized.equals(ActionIds.selectorGiveSelector())) {
            return runPlayerCommand(player, "feud board display wand");
        }
        if (normalized.equals(ActionIds.selectorBindBlue())) {
            return runPlayerCommand(player, "feud team buzzer bind blue");
        }
        if (normalized.equals(ActionIds.selectorBindRed())) {
            return runPlayerCommand(player, "feud team buzzer bind red");
        }
        if (normalized.equals(ActionIds.selectorViewSelection())) {
            describeSelection(player);
            return true;
        }
        if (normalized.startsWith("selector.spawn.")) {
            player.sendMessage("Selection spawn actions are not yet wired to commands");
            return true;
        }

        return false;
    }

    private boolean handleFastMoneyReveal(Player player, String actionId) {
        String suffix = actionId.substring("fastmoney.reveal.".length());
        String[] parts = suffix.split("\\.");
        if (parts.length != 2) {
            player.sendMessage("Invalid Fast Money reveal action: " + actionId);
            return true;
        }

        int questionIndex;
        int slot;
        try {
            questionIndex = Integer.parseInt(parts[0]);
            slot = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            player.sendMessage("Invalid Fast Money indices in action: " + actionId);
            return true;
        }

        fastMoneyCommands.reveal(player, questionIndex, slot);
        return true;
    }

    private void describeSelection(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("No active display selection");
            return;
        }

        if (displayBoardSelectionStore == null) {
            sender.sendMessage("No selection information available");
            return;
        }
        DisplayBoardSelection selection = displayBoardSelectionStore.get(player.getUniqueId());
        if (selection == null) {
            sender.sendMessage("No active display selection");
            return;
        }
        sender.sendMessage("Selection: " + selection.cornerA() + " to " + selection.cornerB() + " facing "
                + selection.facing().name());
    }

    private boolean runPlayerCommand(Player player, String command) {
        if (player == null || command == null || command.isBlank()) {
            return false;
        }
        Bukkit.dispatchCommand(player, command);
        return true;
    }
}
