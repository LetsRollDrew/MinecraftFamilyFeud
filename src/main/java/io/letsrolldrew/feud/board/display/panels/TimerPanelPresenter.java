package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.joml.Vector3d;

public final class TimerPanelPresenter {
    private static final float CMD_TIMER_PANEL = 9005.0f;

    // semantics similar to the board factory
    private static final double PANEL_FORWARD_NUDGE = 0.05;
    private static final double TEXT_FORWARD_NUDGE = 0.04;

    // this is a fallback base; we compute a dynamic width per panel so it never wraps
    private static final int TEXT_LINE_WIDTH = 96;

    // namespaces DisplayKey + display tags
    private static final String BOARD_NAMESPACE = "timer";
    private static final String STORED_NAMESPACE = "panel-timer";

    private static final String TIMER_ID = "top-panel";

    private final DisplayRegistry displayRegistry;

    private final Set<String> storedPanelIds = new HashSet<>();
    private final Set<String> boardPanelIds = new HashSet<>();

    public TimerPanelPresenter(DisplayRegistry displayRegistry) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
    }

    public void rehydrateStoredPanels(TimerPanelStore store) {
        if (store == null) {
            return;
        }
        Map<String, StoredTimerPanel> storedPanels = store.loadPanels();
        for (StoredTimerPanel panel : storedPanels.values()) {
            if (panel == null || panel.layout() == null) {
                continue;
            }
            spawnStored(panel.id(), panel.layout());
        }
    }

    public void spawnForBoard(String boardId, DynamicBoardLayout layout) {
        if (boardId == null || boardId.isBlank() || layout == null) {
            return;
        }
        spawnTimerPanel(BOARD_NAMESPACE, boardId, layout);
        boardPanelIds.add(boardId);
        updateForBoard(boardId, 0);
    }

    public void spawnStored(String panelId, DynamicBoardLayout layout) {
        if (panelId == null || panelId.isBlank() || layout == null) {
            return;
        }
        spawnTimerPanel(STORED_NAMESPACE, panelId, layout);
        storedPanelIds.add(panelId);
    }

    public void removeForBoard(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        removeTimerKeys(BOARD_NAMESPACE, boardId);
        boardPanelIds.remove(boardId);
    }

    public void removeStored(String panelId) {
        if (panelId == null || panelId.isBlank()) {
            return;
        }
        removeTimerKeys(STORED_NAMESPACE, panelId);
        storedPanelIds.remove(panelId);
    }

    public void updateForBoard(String boardId, int remainingSeconds) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        PanelDisplayHelper.setText(
                displayRegistry,
                new DisplayKey(BOARD_NAMESPACE, boardId, TIMER_ID, "text"),
                Component.text(formatSecondsSafe(remainingSeconds)));
    }

    public void updateAll(int remainingSeconds) {
        Component text = Component.text(formatSecondsSafe(remainingSeconds));

        for (String boardId : boardPanelIds) {
            PanelDisplayHelper.setText(
                    displayRegistry, new DisplayKey(BOARD_NAMESPACE, boardId, TIMER_ID, "text"), text);
        }
        for (String panelId : storedPanelIds) {
            PanelDisplayHelper.setText(
                    displayRegistry, new DisplayKey(STORED_NAMESPACE, panelId, TIMER_ID, "text"), text);
        }
    }

    private void spawnTimerPanel(String namespace, String group, DynamicBoardLayout layout) {
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        // replace existing
        removeTimerKeys(namespace, group);

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();
        if (panelWidth <= 0.0 || panelHeight <= 0.0) {
            return;
        }

        float yaw = layout.facing().yaw();
        ItemStack panelStack = PanelDisplayHelper.stackWithCmd(CMD_TIMER_PANEL);

        // center INSIDE the selected rectangle (no more hardcoded dims)
        Vector3d center =
                TimerPanelPlacement.computeCenterOnSelection(layout, panelWidth, panelHeight, PANEL_FORWARD_NUDGE);
        Location centerLoc = PanelDisplayHelper.toLocation(world, center, yaw);

        DisplayKey bgKey = new DisplayKey(namespace, group, TIMER_ID, "bg");
        DisplayKey textKey = new DisplayKey(namespace, group, TIMER_ID, "text");

        PanelDisplayHelper.spawnBackground(
                displayRegistry, bgKey, world, centerLoc, yaw, panelWidth, panelHeight, panelStack, namespace);

        // text size and position, note: ocasionally tweak vertical nudge if upscaling
        double verticalNudge = -panelHeight * 0.12;
        double textScale = PanelDisplayHelper.clamp(panelHeight * 1.75, 1.0, 200.0);

        int lineWidth = Math.max(TEXT_LINE_WIDTH, (int) (panelWidth * 120.0));

        PanelDisplayHelper.spawnText(
                displayRegistry,
                textKey,
                world,
                centerLoc,
                yaw,
                layout,
                lineWidth,
                textScale,
                verticalNudge,
                TEXT_FORWARD_NUDGE,
                namespace,
                true);
    }

    private void removeTimerKeys(String namespace, String group) {
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "bg"));
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "text"));
    }

    private static String formatSeconds(int remainingSeconds) {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;

        // helps format as 09 08 07 so double digits stays aligned
        if (mins == 0) {
            return String.format("%02d", secs);
        }
        return String.format("%d:%02d", mins, secs);
    }

    private static String formatSecondsSafe(int remainingSeconds) {
        return formatSeconds(Math.max(0, remainingSeconds));
    }
}
