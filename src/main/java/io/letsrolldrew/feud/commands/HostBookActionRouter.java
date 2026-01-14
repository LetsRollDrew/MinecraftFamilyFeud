package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.display.DisplayBoardPresenter;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelection;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.ui.actions.ActionIds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HostBookActionRouter {
    private final Messages messages;
    private final FastMoneyCommands fastMoneyCommands;
    private final DisplayBoardSelectionStore displayBoardSelectionStore;
    private final DisplayBoardPresenter displayBoardPresenter;

    public HostBookActionRouter(
            Messages messages,
            FastMoneyCommands fastMoneyCommands,
            DisplayBoardSelectionStore displayBoardSelectionStore,
            DisplayBoardPresenter displayBoardPresenter) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.fastMoneyCommands = fastMoneyCommands;
        this.displayBoardSelectionStore = displayBoardSelectionStore;
        this.displayBoardPresenter = displayBoardPresenter;
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
        if (normalized.equals(ActionIds.selectorSpawnBoard())) {
            return spawnBoardFromSelection(player);
        }
        if (normalized.equals(ActionIds.selectorSpawnPanelsRed())) {
            return spawnPanelsFromSelection(player, "red");
        }
        if (normalized.equals(ActionIds.selectorSpawnPanelsBlue())) {
            return spawnPanelsFromSelection(player, "blue");
        }
        if (normalized.equals(ActionIds.selectorSpawnTimer())) {
            return spawnTimerFromSelection(player);
        }

        return false;
    }

    private boolean handleFastMoneyReveal(Player player, String actionId) {
        String suffix = actionId.substring("fastmoney.reveal.".length());
        String[] parts = suffix.split("\\.");
        if (parts.length != 2) {
            messages.error(player, Msg.INVALID_FAST_MONEY_REVEAL_ACTION, Placeholder.of("actionId", actionId));
            return true;
        }

        int questionIndex;
        int slot;
        try {
            questionIndex = Integer.parseInt(parts[0]);
            slot = Integer.parseInt(parts[1]);
        } catch (NumberFormatException ex) {
            messages.error(player, Msg.INVALID_FAST_MONEY_INDICES_ACTION, Placeholder.of("actionId", actionId));
            return true;
        }

        fastMoneyCommands.reveal(player, questionIndex, slot);
        return true;
    }

    private void describeSelection(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.DISPLAY_SELECTION_NONE);
            return;
        }

        if (displayBoardSelectionStore == null) {
            messages.error(sender, Msg.DISPLAY_SELECTION_INFO_UNAVAILABLE);
            return;
        }
        DisplayBoardSelection selection = displayBoardSelectionStore.get(player.getUniqueId());
        if (selection == null) {
            messages.error(sender, Msg.DISPLAY_SELECTION_NONE);
            return;
        }
        messages.info(
                sender,
                Msg.DISPLAY_SELECTION_STATUS,
                Placeholder.of("cornerA", selection.cornerA()),
                Placeholder.of("cornerB", selection.cornerB()),
                Placeholder.of("facing", selection.facing().name()));
    }

    private boolean runPlayerCommand(Player player, String command) {
        if (player == null || command == null || command.isBlank()) {
            return false;
        }
        Bukkit.dispatchCommand(player, command);
        return true;
    }

    private boolean spawnBoardFromSelection(Player player) {
        String boardId = chooseNewBoardId();
        return runPlayerCommand(player, "feud board display selection board " + boardId);
    }

    private boolean spawnPanelsFromSelection(Player player, String team) {
        String boardId = firstBoardIdOrNull();
        if (boardId == null) {
            messages.error(player, Msg.BOARD_LIST_EMPTY);
            return true;
        }
        return runPlayerCommand(player, "feud board display selection panels " + boardId + " " + team);
    }

    private boolean spawnTimerFromSelection(Player player) {
        String boardId = firstBoardIdOrNull();
        if (boardId == null) {
            messages.error(player, Msg.BOARD_LIST_EMPTY);
            return true;
        }
        return runPlayerCommand(player, "feud board display selection timer " + boardId);
    }

    private String firstBoardIdOrNull() {
        if (displayBoardPresenter == null) {
            return null;
        }
        List<String> ids = new ArrayList<>(displayBoardPresenter.listBoards());
        Collections.sort(ids);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private String chooseNewBoardId() {
        Set<String> existing = new HashSet<>();
        if (displayBoardPresenter != null) {
            existing.addAll(displayBoardPresenter.listBoards());
        }

        for (int i = 1; i <= 10_000; i++) {
            String candidate = "board" + i;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }

        return "board" + System.currentTimeMillis();
    }
}
