package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public final class TimerPanelPresenter {
    private static final float CMD_TIMER_PANEL = 9005.0f;
    private static final double PANEL_WIDTH_BLOCKS = 3.0;
    private static final double PANEL_HEIGHT_BLOCKS = 2.0;
    private static final double GAP_ABOVE_BOARD_BLOCKS = 1.0;
    private static final double FORWARD_NUDGE = 0.05;
    private static final double TEXT_FORWARD_NUDGE = 0.04;
    private static final int TEXT_LINE_WIDTH = 96;
    private static final double TEXT_SCALE = 0.9;
    private static final String TIMER_NAMESPACE = "timer";
    private static final String STORED_TIMER_NAMESPACE = "panel-timer";
    private static final String TIMER_ID = "top-timer";

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
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        removeForBoard(boardId);

        double panelWidth = PANEL_WIDTH_BLOCKS;
        double panelHeight = PANEL_HEIGHT_BLOCKS;
        double gapAbove = GAP_ABOVE_BOARD_BLOCKS;

        Vector3d center = TimerPanelPlacement.computeCenter(layout, gapAbove, panelHeight, FORWARD_NUDGE);
        spawnBackground(world, center, layout.facing().yaw(), panelWidth, panelHeight, TIMER_NAMESPACE, boardId);
        spawnText(world, center, layout.facing().yaw(), layout, panelHeight, TIMER_NAMESPACE, boardId);
        boardPanelIds.add(boardId);
        updateForBoard(boardId, 0);
    }

    public void spawnStored(String panelId, DynamicBoardLayout layout) {
        if (panelId == null || panelId.isBlank() || layout == null) {
            return;
        }
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        removeStored(panelId);

        double panelWidth = PANEL_WIDTH_BLOCKS;
        double panelHeight = PANEL_HEIGHT_BLOCKS;
        double gapAbove = GAP_ABOVE_BOARD_BLOCKS;

        Vector3d center = TimerPanelPlacement.computeCenter(layout, gapAbove, panelHeight, FORWARD_NUDGE);
        spawnBackground(world, center, layout.facing().yaw(), panelWidth, panelHeight, STORED_TIMER_NAMESPACE, panelId);
        spawnText(world, center, layout.facing().yaw(), layout, panelHeight, STORED_TIMER_NAMESPACE, panelId);
        storedPanelIds.add(panelId);
    }

    public void updateForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
    }

    public void updateForBoard(String boardId, int remainingSeconds) {
        if (boardId == null || boardId.isBlank()) {
            return;
        }
        String formatted = formatSeconds(Math.max(0, remainingSeconds));
        setText(new DisplayKey(TIMER_NAMESPACE, boardId, TIMER_ID, "text"), Component.text(formatted));
    }

    public void removeForBoard(String boardId) {
        if (boardId == null) {
            return;
        }
        removePanelKeys(TIMER_NAMESPACE, boardId);
        boardPanelIds.remove(boardId);
    }

    public void removeStored(String panelId) {
        if (panelId == null || panelId.isBlank()) {
            return;
        }
        removePanelKeys(STORED_TIMER_NAMESPACE, panelId);
        storedPanelIds.remove(panelId);
    }

    public void updateAll(int remainingSeconds) {
        for (String boardId : boardPanelIds) {
            setText(new DisplayKey(TIMER_NAMESPACE, boardId, TIMER_ID, "text"), Component.text(formatSecondsSafe(remainingSeconds)));
        }
        for (String panelId : storedPanelIds) {
            setText(new DisplayKey(STORED_TIMER_NAMESPACE, panelId, TIMER_ID, "text"), Component.text(formatSecondsSafe(remainingSeconds)));
        }
    }

    private void spawnBackground(
            World world,
            Vector3d center,
            float yaw,
            double panelWidth,
            double panelHeight,
            String namespace,
            String group) {
        Location loc = new Location(world, center.x, center.y, center.z, yaw, 0f);
        ItemStack stack = stackWithCmd(CMD_TIMER_PANEL);

        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) panelWidth, (float) panelHeight, 0.01f),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayKey key = new DisplayKey(namespace, group, TIMER_ID, "bg");
        DisplayTags.tag(display, namespace, key.group());
        displayRegistry.register(key, display);
    }

    private void spawnText(
            World world,
            Vector3d center,
            float yaw,
            DynamicBoardLayout layout,
            double panelHeight,
            String namespace,
            String group) {
        double verticalOffset = panelHeight * 0.05;
        Location loc = new Location(world, center.x, center.y + verticalOffset, center.z, yaw, 0f);

        Location spawnLoc = loc.clone()
                .add(
                        layout.facing().forwardX() * TEXT_FORWARD_NUDGE,
                        0,
                        layout.facing().forwardZ() * TEXT_FORWARD_NUDGE);

        TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            entity.setShadowed(true);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(org.bukkit.Color.fromARGB(0));
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setViewRange(48f);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setLineWidth(TEXT_LINE_WIDTH);
            entity.text(Component.empty());
            entity.setTextOpacity((byte) 0);
            try {
                double scale = TEXT_SCALE * layout.cellHeight();
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) scale, (float) scale, (float) scale),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayKey key = new DisplayKey(namespace, group, TIMER_ID, "text");
        DisplayTags.tag(display, namespace, key.group());
        displayRegistry.register(key, display);
    }

    private static ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    private void setText(DisplayKey key, Component text) {
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.text(text == null ? Component.empty() : text);
            display.setTextOpacity((byte) 0xFF);
        });
    }

    private void removePanelKeys(String namespace, String group) {
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "bg"));
        displayRegistry.remove(new DisplayKey(namespace, group, TIMER_ID, "text"));
    }

    private static String formatSeconds(int remainingSeconds) {
        int mins = remainingSeconds / 60;
        int secs = remainingSeconds % 60;
        if (mins == 0) {
            return Integer.toString(secs);
        }
        return String.format("%d:%02d", mins, secs);
    }

    private static String formatSecondsSafe(int remainingSeconds) {
        return formatSeconds(Math.max(0, remainingSeconds));
    }
}
