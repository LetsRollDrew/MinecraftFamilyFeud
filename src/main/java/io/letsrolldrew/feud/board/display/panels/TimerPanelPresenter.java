package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class TimerPanelPresenter {
    private static final float CMD_TIMER_PANEL = 9005.0f;
    private static final float CMD_TIMER_LOGO = 9009.0f;

    // semantics similar to the board factory
    private static final double PANEL_FORWARD_NUDGE = 0.05;
    private static final double TEXT_FORWARD_NUDGE = 0.04;
    private static final double LOGO_FORWARD_NUDGE = 0.08;
    private static final double LOGO_WIDTH_FACTOR = 0.58;
    private static final double LOGO_HEIGHT_FACTOR = 0.58;

    // this is a fallback base; we compute a dynamic width per panel so it never wraps
    private static final int TEXT_LINE_WIDTH = 96;

    // namespaces DisplayKey + display tags
    private static final String BOARD_NAMESPACE = "timer";
    private static final String STORED_NAMESPACE = "panel-timer";

    private static final String TIMER_ID = "top-panel";

    private final DisplayRegistry displayRegistry;

    private final Set<String> storedPanelIds = new HashSet<>();
    private final Set<String> boardPanelIds = new HashSet<>();
    private final Map<String, DynamicBoardLayout> boardLayouts = new HashMap<>();
    private final Map<String, DynamicBoardLayout> storedLayouts = new HashMap<>();

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
        boardLayouts.put(boardId, layout);
        updateForBoard(boardId, 0, false);
    }

    public void spawnStored(String panelId, DynamicBoardLayout layout) {
        if (panelId == null || panelId.isBlank() || layout == null) {
            return;
        }
        spawnTimerPanel(STORED_NAMESPACE, panelId, layout);
        storedPanelIds.add(panelId);
        storedLayouts.put(panelId, layout);
        updatePanel(STORED_NAMESPACE, panelId, 0, false);
    }

    public void removeForBoard(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        removeTimerKeys(BOARD_NAMESPACE, boardId);
        boardPanelIds.remove(boardId);
        boardLayouts.remove(boardId);
    }

    public void removeStored(String panelId) {
        if (panelId == null || panelId.isBlank()) {
            return;
        }
        removeTimerKeys(STORED_NAMESPACE, panelId);
        storedPanelIds.remove(panelId);
        storedLayouts.remove(panelId);
    }

    public void updateForBoard(String boardId, int remainingSeconds) {
        updateForBoard(boardId, remainingSeconds, true);
    }

    public void updateForBoard(String boardId, int remainingSeconds, boolean running) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        updatePanel(BOARD_NAMESPACE, boardId, remainingSeconds, running);
    }

    public void updateAll(int remainingSeconds) {
        updateAll(remainingSeconds, true);
    }

    public void updateAll(int remainingSeconds, boolean running) {
        for (String boardId : boardPanelIds) {
            updatePanel(BOARD_NAMESPACE, boardId, remainingSeconds, running);
        }
        for (String panelId : storedPanelIds) {
            updatePanel(STORED_NAMESPACE, panelId, remainingSeconds, running);
        }
    }

    private void updatePanel(String namespace, String group, int remainingSeconds, boolean running) {
        DisplayKey textKey = textKey(namespace, group);
        displayRegistry.resolveText(textKey).ifPresent(display -> {
            display.text(Component.text(formatSecondsSafe(remainingSeconds)));
            display.setTextOpacity(running ? (byte) 0xFF : (byte) 0x00);
        });

        if (running) {
            displayRegistry.remove(logoKey(namespace, group));
            return;
        }
        ensureLogo(namespace, group);
    }

    private void ensureLogo(String namespace, String group) {
        DisplayKey key = logoKey(namespace, group);
        if (displayRegistry.resolveItem(key).isPresent()) {
            return;
        }

        DynamicBoardLayout layout = layoutFor(namespace, group);
        if (layout == null) {
            return;
        }

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        double panelWidth = layout.totalWidth();
        double panelHeight = layout.totalHeight();
        if (panelWidth <= 0.0 || panelHeight <= 0.0) {
            return;
        }

        float yaw = layout.facing().yaw();
        Vector3d center =
                TimerPanelPlacement.computeCenterOnSelection(layout, panelWidth, panelHeight, PANEL_FORWARD_NUDGE);
        Location centerLoc = PanelDisplayHelper.toLocation(world, center, yaw);
        spawnLogo(namespace, group, world, centerLoc, layout, yaw, panelWidth, panelHeight);
    }

    private DynamicBoardLayout layoutFor(String namespace, String group) {
        if (BOARD_NAMESPACE.equals(namespace)) {
            return boardLayouts.get(group);
        }
        return storedLayouts.get(group);
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
                false);

        spawnLogo(namespace, group, world, centerLoc, layout, yaw, panelWidth, panelHeight);
    }

    private void spawnLogo(
            String namespace,
            String group,
            World world,
            Location centerLoc,
            DynamicBoardLayout layout,
            float yaw,
            double panelWidth,
            double panelHeight) {
        DisplayKey key = logoKey(namespace, group);
        displayRegistry.remove(key);

        Location logoLoc = centerLoc.clone().add(
                layout.facing().forwardX() * LOGO_FORWARD_NUDGE, 0, layout.facing().forwardZ() * LOGO_FORWARD_NUDGE);

        double logoWidth = Math.max(0.01, panelWidth * LOGO_WIDTH_FACTOR);
        double logoHeight = Math.max(0.01, panelHeight * LOGO_HEIGHT_FACTOR);
        ItemStack logoStack = PanelDisplayHelper.stackWithCmd(CMD_TIMER_LOGO);

        ItemDisplay logo = world.spawn(logoLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(logoStack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f((float) logoWidth, (float) logoHeight, 0.01f),
                    new AxisAngle4f(0, 0, 0, 0)));
        });

        if (logo == null) {
            return;
        }
        DisplayTags.tag(logo, namespace, key.group());
        displayRegistry.register(key, logo);
    }

    private void removeTimerKeys(String namespace, String group) {
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "bg"));
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "text"));
        displayRegistry.remove(logoKey(namespace, group));
    }

    private static DisplayKey textKey(String namespace, String group) {
        return new DisplayKey(namespace, group, TIMER_ID, "text");
    }

    private static DisplayKey logoKey(String namespace, String group) {
        return new DisplayKey(namespace, group, TIMER_ID, "logo");
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
