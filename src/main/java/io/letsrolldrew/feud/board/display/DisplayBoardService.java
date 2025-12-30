package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.effects.anim.AnimationService;
import io.letsrolldrew.feud.effects.anim.AnimationStep;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DisplayBoardService implements DisplayBoardPresenter {
    private static final float CMD_HIDDEN = 9001.0f;
    private static final float CMD_REVEALED = 9002.0f;
    private static final float CMD_FLASH = 9003.0f;

    private final Map<String, BoardInstance> boards = new HashMap<>();
    private final Map<String, BoardInstance> dynamicBoards = new HashMap<>();
    private final Map<String, LayoutMetrics> metricsByBoardId = new HashMap<>();
    private final BoardLayout layout = BoardLayout.defaultLayout();
    private final DisplayRegistry displayRegistry;
    private final AnimationService animationService;
    private final DynamicBoardStore dynamicStore;

    public DisplayBoardService(DisplayRegistry displayRegistry, AnimationService animationService) {
        this(displayRegistry, animationService, null);
    }

    public DisplayBoardService(DisplayRegistry displayRegistry, AnimationService animationService, File dynamicStoreFile) {
        this.displayRegistry = displayRegistry;
        this.animationService = animationService;
        this.dynamicStore = new DynamicBoardStore(dynamicStoreFile);
        loadDynamicBoards();
    }
    @Override
    public void createBoard(String boardId, Location anchor, Player facingReference) {
        if (boardId == null || boardId.isBlank() || anchor == null || facingReference == null) {
            return;
        }
        World world = anchor.getWorld();
        if (world == null) {
            return;
        }
        if (boards.containsKey(boardId)) {
            return;
        }

        BoardFacing facing = BoardFacing.fromYaw(facingReference.getLocation().getYaw());
        BoardSpace space = new BoardSpace(anchor.clone(), facing);
        ItemStack hiddenStack = stackWithCmd(CMD_HIDDEN);

        List<SlotInstance> slots = new ArrayList<>(layout.slotRows() * layout.slotCols());
        int slotIndex = 0;
        for (int col = 0; col < layout.slotCols(); col++) {
            double colX = col == 0 ? 0 : layout.slotWidth() + layout.columnGap();
            for (int row = 0; row < layout.slotRows(); row++) {
                double yOffset = -(row * (layout.slotHeight() + layout.rowGap()));

                Location slotLoc = space.at(colX, yOffset, 0);

                SlotInstance slot = buildSlot(boardId, slotIndex);

                spawnBackground(slot.backgroundKey(), world, slotLoc, hiddenStack, facing.yaw());

                Location ansLoc = space.at(colX - 1.3, yOffset, layout.textZOffset());
                spawnText(slot.answerKey(), world, ansLoc, layout.textScale(), facing.yaw(), facing);

                Location ptsLoc = space.at(colX + 1.4, yOffset, layout.textZOffset());
                spawnText(slot.pointsKey(), world, ptsLoc, layout.textScale(), facing.yaw(), facing);

                slots.add(slot);
                slotIndex++;
            }
        }

        boards.put(boardId, new BoardInstance(boardId, anchor.clone(), facing.yaw(), slots));
        metricsByBoardId.put(boardId, metricsFromStaticLayout());
    }

    @Override
    public void destroyBoard(String boardId) {
        BoardInstance instance = boards.remove(boardId);
        if (instance != null) {
            removeSlotEntities(instance.slots());
            metricsByBoardId.remove(boardId);
            return;
        }
        BoardInstance dyn = dynamicBoards.remove(boardId);
        if (dyn != null) {
            removeSlotEntities(dyn.slots());
            dynamicStore.removeLayout(boardId);
            metricsByBoardId.remove(boardId);
        }
    }

    @Override
    public void setSlot(String boardId, int slotIndex, String answer, Integer points, boolean revealed) {
        if (boardId == null) {
            return;
        }
        if (revealed) {
            if (points == null) {
                return;
            }
            revealSlot(boardId, slotIndex, answer == null ? "" : answer, points);
        } else {
            hideSlot(boardId, slotIndex);
        }
    }

    @Override
    public void revealSlot(String boardId, int slotIndex, String answer, int points) {
        SlotInstance slot = slotFor(boardId, slotIndex);
        if (slot == null) {
            return;
        }
        animationService.cancel(slot.backgroundKey());
        List<AnimationStep> steps = List.of(
            new AnimationStep(0, () -> setBackgroundCmd(slot.backgroundKey(), CMD_FLASH)),
            new AnimationStep(2, () -> setBackgroundCmd(slot.backgroundKey(), CMD_HIDDEN)),
            new AnimationStep(2, () -> setBackgroundCmd(slot.backgroundKey(), CMD_FLASH)),
            new AnimationStep(2, () -> setBackgroundCmd(slot.backgroundKey(), CMD_REVEALED)),
            new AnimationStep(0, () -> {
                setAnswerText(boardId, slot.answerKey(), answer);
                setPointsText(boardId, slot.pointsKey(), points);
            })
        );
        animationService.schedule(slot.backgroundKey(), steps);
    }

    @Override
    public void hideSlot(String boardId, int slotIndex) {
        SlotInstance slot = slotFor(boardId, slotIndex);
        if (slot == null) {
            return;
        }
        animationService.cancel(slot.backgroundKey());
        setBackgroundCmd(slot.backgroundKey(), CMD_HIDDEN);
        clearText(slot.answerKey());
        clearText(slot.pointsKey());
    }

    @Override
    public void clearAll() {
        for (BoardInstance instance : boards.values()) {
            removeSlotEntities(instance.slots());
        }
        boards.clear();
        for (BoardInstance instance : dynamicBoards.values()) {
            removeSlotEntities(instance.slots());
        }
        dynamicBoards.clear();
        dynamicStore.clear();
        metricsByBoardId.clear();
    }

    private SlotInstance buildSlot(String boardId, int slotIndex) {
        String slotId = "slot" + (slotIndex + 1);
        DisplayKey bgKey = new DisplayKey("board", boardId, slotId, "bg");
        DisplayKey ansKey = new DisplayKey("board", boardId, slotId, "answer");
        DisplayKey ptsKey = new DisplayKey("board", boardId, slotId, "points");
        return new SlotInstance(bgKey, ansKey, ptsKey);
    }

    private SlotInstance slotFor(String boardId, int slotIndex) {
        BoardInstance board = boards.get(boardId);
        if (board == null) {
            board = dynamicBoards.get(boardId);
        }
        if (board == null) {
            return null;
        }
        int idx = slotIndex - 1;
        if (idx < 0 || idx >= board.slots().size()) {
            return null;
        }
        return board.slots().get(idx);
    }

    private void spawnBackground(DisplayKey key, World world, Location loc, ItemStack stack, float yaw) {
        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);

            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f(layout.backgroundScaleX(), layout.backgroundScaleY(), layout.backgroundScaleZ()),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }

        try {
            display.setRotation(yaw, 0f);
        } catch (Throwable ignored) {
        }
        io.letsrolldrew.feud.display.DisplayTags.tag(display, "board", key.group());
        displayRegistry.register(key, display);
    }

    private void spawnText(DisplayKey key, World world, Location loc, float scale, float yaw, BoardFacing facing) {
        // nudge text slightly forward in board space so it renders above the background plane
        Location spawnLoc = loc.clone();
        double forwardNudge = 0.08;
        spawnLoc.add(facing.forwardX() * forwardNudge, 0, facing.forwardZ() * forwardNudge);

        TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);

            entity.setShadowed(true);
            entity.setSeeThrough(false);
            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }
            try {
                entity.setBrightness(new Display.Brightness(15, 15));
            } catch (Throwable ignored) {
            }
            entity.setViewRange(64f);

            entity.text(Component.empty());
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            int lineWidth = (int) Math.max(40, layout.slotWidth() * 14);
            entity.setLineWidth(lineWidth);

            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f(scale, scale, scale),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }

        try {
            display.setRotation(yaw, 0f);
        } catch (Throwable ignored) {
        }

        DisplayTags.tag(display, "board", key.group());
        displayRegistry.register(key, display);
    }

    private ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    private void removeSlotEntities(List<SlotInstance> slots) {
        for (SlotInstance slot : slots) {
            displayRegistry.remove(slot.backgroundKey());
            displayRegistry.remove(slot.answerKey());
            displayRegistry.remove(slot.pointsKey());
            animationService.cancel(slot.backgroundKey());
        }
    }

    private void setBackgroundCmd(DisplayKey key, float cmd) {
        displayRegistry.resolveItem(key).ifPresent(display -> {
            ItemStack stack = display.getItemStack();
            if (stack == null) {
                stack = new ItemStack(Material.PAPER);
            } else {
                stack = stack.clone();
            }
            ItemMeta meta = stack.getItemMeta();
            CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
            cmdComponent.setFloats(java.util.List.of(cmd));
            meta.setCustomModelDataComponent(cmdComponent);
            stack.setItemMeta(meta);
            display.setItemStack(stack);
        });
    }

    private void setAnswerText(String boardId, DisplayKey key, String text) {
        Component value = text == null ? Component.empty() : Component.text(text);
        applyAnswerText(boardId, key, value);
    }

    private void setPointsText(String boardId, DisplayKey key, Integer points) {
        String value = points == null ? "" : Integer.toString(points);
        applyPointsText(boardId, key, Component.text(value));
    }

    private void clearText(DisplayKey key) {
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.text(Component.empty());
            try {
                display.setTextOpacity((byte) 0);
            } catch (Throwable ignored) {
            }
        });
    }

    private void applyPointsText(String boardId, DisplayKey key, Component component) {
        LayoutMetrics metrics = metricsFor(boardId);
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.setAlignment(TextDisplay.TextAlignment.RIGHT);
            String plain = PlainTextComponentSerializer.plainText().serialize(component).trim();

            double regionBlocks = Math.max(0.01, metrics.pointsWidth() - (2.0 * metrics.pad()));
            double regionPx = regionBlocks * metrics.pxPerBlock();

            double desiredScale = metrics.baseScale();
            if (!plain.isEmpty()) {
                double estimatedPx = plain.length() * 7.0;
                double maxScaleToFit = regionPx / Math.max(1.0, estimatedPx);
                desiredScale = Math.min(desiredScale, maxScaleToFit);
                desiredScale = Math.max(desiredScale, metrics.baseScale() * 0.45);
            }

            float scale = (float) desiredScale;
            setUniformScale(display, scale);

            int lineWidthPx = (int) Math.max(1, Math.round(regionPx / Math.max(0.001, scale)));
            display.setLineWidth(lineWidthPx);
            display.text(component);
            try {
                display.setTextOpacity((byte) 0xFF);
            } catch (Throwable ignored) {
            }
        });
    }

    private void applyAnswerText(String boardId, DisplayKey key, Component component) {
        LayoutMetrics metrics = metricsFor(boardId);
        displayRegistry.resolveText(key).ifPresent(display -> {
            String plain = PlainTextComponentSerializer.plainText().serialize(component).trim();
            boolean multiWord = plain.contains(" ");
            display.setAlignment(TextDisplay.TextAlignment.LEFT);

            double regionBlocks = Math.max(0.01, metrics.answerWidth() - (2.0 * metrics.pad()));
            double regionPx = regionBlocks * metrics.pxPerBlock();

            double desiredScale = metrics.baseScale();
            if (!multiWord && !plain.isEmpty()) {
                double estimatedPx = plain.length() * 7.0;
                double maxScaleToFit = regionPx / Math.max(1.0, estimatedPx);
                desiredScale = Math.min(desiredScale, maxScaleToFit);
                desiredScale = Math.max(desiredScale, metrics.baseScale() * 0.45);
            }

            float scale = (float) desiredScale;
            setUniformScale(display, scale);

            int lineWidthPx = (int) Math.max(1, Math.round(regionPx / Math.max(0.001, scale)));
            display.setLineWidth(lineWidthPx);

            display.text(component);
            try {
                display.setTextOpacity((byte) 0xFF);
            } catch (Throwable ignored) {
            }
        });
    }

    private static void setUniformScale(TextDisplay display, float uniformScale) {
        try {
            Transformation current = display.getTransformation();
            display.setTransformation(new Transformation(
                new Vector3f(current.getTranslation()),
                new AxisAngle4f(current.getLeftRotation()),
                new Vector3f(uniformScale, uniformScale, uniformScale),
                new AxisAngle4f(current.getRightRotation())
            ));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public BoardInstance createDynamicBoard(String boardId, DynamicBoardLayout dynamicLayout) {
        if (boardId == null || boardId.isBlank() || dynamicLayout == null) {
            return null;
        }
        if (dynamicBoards.containsKey(boardId) || boards.containsKey(boardId)) {
            return null;
        }
        BoardInstance instance = DynamicDisplayBoardFactory.create(boardId, dynamicLayout, displayRegistry);
        if (instance != null) {
            dynamicBoards.put(boardId, instance);
            dynamicStore.saveLayout(boardId, dynamicLayout);
            metricsByBoardId.put(boardId, metricsFromDynamicLayout(dynamicLayout));
        }
        return instance;
    }

    @Override
    public java.util.Collection<String> listBoards() {
        java.util.Set<String> ids = new java.util.HashSet<>();
        ids.addAll(boards.keySet());
        ids.addAll(dynamicBoards.keySet());
        return ids;
    }

    private void loadDynamicBoards() {
        for (java.util.Map.Entry<String, DynamicBoardLayout> entry : dynamicStore.loadLayouts().entrySet()) {
            String boardId = entry.getKey();
            DynamicBoardLayout layout = entry.getValue();
            if (layout == null || layout.worldId() == null) {
                continue;
            }
            displayRegistry.removeByGroup("board", boardId);
            BoardInstance instance = DynamicDisplayBoardFactory.create(boardId, layout, displayRegistry);
            if (instance != null) {
                dynamicBoards.put(instance.boardId(), instance);
                metricsByBoardId.put(boardId, metricsFromDynamicLayout(layout));
            }
        }
    }

    //might no longer need
    private List<SlotInstance> buildDynamicSlots(String boardId) {
        List<SlotInstance> slots = new ArrayList<>(8);
        int idx = 0;
        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < 4; row++) {
                slots.add(buildSlot(boardId, idx));
                idx++;
            }
        }
        return slots;
    }

    private LayoutMetrics metricsFor(String boardId) {
        LayoutMetrics metrics = metricsByBoardId.get(boardId);
        if (metrics != null) {
            return metrics;
        }
        return metricsFromStaticLayout();
    }

    // genuinely just a fallback for above ^ if something goes wrong
    // just gets the default layout metrics
    private LayoutMetrics metricsFromStaticLayout() {
        BoardLayout layout = BoardLayout.defaultLayout();
        double cellW = layout.slotWidth();
        double cellH = layout.slotHeight();
        double pointsW = cellW * 0.25;
        double gapW = cellW * 0.03;
        double padW = cellW * 0.06;
        double answerW = cellW - pointsW - gapW;
        double textScale = layout.textScale();
        double pxPerBlock = 90.0;
        return new LayoutMetrics(answerW, pointsW, padW, pxPerBlock, textScale);
    }

    private LayoutMetrics metricsFromDynamicLayout(DynamicBoardLayout layout) {
        double cellW = layout.cellWidth();
        double cellH = layout.cellHeight();
        double pointsW = cellW * 0.25;
        double gapW = cellW * 0.03;
        double padW = cellW * 0.06;
        double answerW = cellW - pointsW - gapW;
        double textScale = Math.max(0.8, Math.min(6.0, cellH * 1.6));
        double pxPerBlock = 90.0;
        return new LayoutMetrics(answerW, pointsW, padW, pxPerBlock, textScale);
    }

    private record LayoutMetrics(double answerWidth, double pointsWidth, double pad, double pxPerBlock, double baseScale) {
    }
}
