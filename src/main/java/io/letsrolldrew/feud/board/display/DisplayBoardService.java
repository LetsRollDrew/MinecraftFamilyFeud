package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.TimerPanelPresenter;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import io.letsrolldrew.feud.effects.anim.AnimationService;
import io.letsrolldrew.feud.effects.anim.AnimationStep;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.kyori.adventure.text.Component;
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

public final class DisplayBoardService implements DisplayBoardPresenter {
    private static final float CMD_HIDDEN = 9001.0f;
    private static final float CMD_REVEALED = 9002.0f;
    private static final float CMD_FLASH = 9003.0f;

    // lineWidth does not wrap long words well, so keeping lineWidth huge
    // and treating "fit" as a way to scale it down or split it
    private static final int LINE_WIDTH_NO_WRAP = 4096;

    // lower charsPerBlock text shrinks sooner for the same region
    private static final double ANSWER_CHARS_PER_BLOCK = 5.0;
    private static final double POINTS_CHARS_PER_BLOCK = 7.0;

    private static final double MIN_SCALE_FACTOR_ANSWER = 0.45;
    private static final double MIN_SCALE_FACTOR_POINTS = 0.60;

    // this is genuinely just a brute force for large words its 4:31 AM bro
    // something is going wrong in the dynamic sizing that makes large words
    // clip out of the slot region, this file has definitely overcomplicated
    // things enough as is and I don't want to look at it anymore man consider
    // this my manifesto and genuine apology for anyone looking at this file
    private static final float ANSWER_RIGHT_NUDGE_BLOCKS = 0.18f;

    // long single words do not wrap well, so split them
    // - 2 words boom put on top/bottom
    // - 1 long word boom split mid and put on top/bottom
    private static final int LONG_WORD_SPLIT_THRESHOLD = 12;

    private static final double TEXT_FORWARD_NUDGE = 0.08;

    // keep the same static offsets so boards dont shift visually
    private static final double STATIC_ANSWER_CENTER_X = -1.3;
    private static final double STATIC_POINTS_CENTER_X = 1.4;

    private final Map<String, BoardInstance> instances = new HashMap<>();
    private final Set<String> dynamicIds = new HashSet<>();
    private final Map<String, LayoutMetrics> metricsByBoardId = new HashMap<>();

    private final BoardLayout layout = BoardLayout.defaultLayout();
    private final LayoutMetrics staticMetrics = metricsFromStaticLayout();

    private final DisplayRegistry displayRegistry;
    private final AnimationService animationService;
    private final DynamicBoardStore dynamicStore;
    private final ScorePanelPresenter scorePanelPresenter;
    private final TimerPanelPresenter timerPanelPresenter;

    public DisplayBoardService(DisplayRegistry displayRegistry, AnimationService animationService) {
        this(displayRegistry, animationService, null, null, null);
    }

    public DisplayBoardService(
            DisplayRegistry displayRegistry,
            AnimationService animationService,
            File dynamicStoreFile,
            ScorePanelPresenter scorePanelPresenter,
            TimerPanelPresenter timerPanelPresenter) {
        this.displayRegistry = displayRegistry;
        this.animationService = animationService;
        this.dynamicStore = new DynamicBoardStore(dynamicStoreFile);
        this.scorePanelPresenter = scorePanelPresenter;
        this.timerPanelPresenter = timerPanelPresenter;
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

        if (instances.containsKey(boardId)) {
            return;
        }

        BoardFacing facing = BoardFacing.fromYaw(facingReference.getLocation().getYaw());
        BoardSpace space = new BoardSpace(anchor.clone(), facing);

        ItemStack hiddenStack = stackWithCmd(CMD_HIDDEN);

        List<SlotInstance> slots = new ArrayList<>(layout.slotRows() * layout.slotCols());
        int slotIndex = 0;

        for (int col = 0; col < layout.slotCols(); col++) {
            double colX = col == 0 ? 0.0 : layout.slotWidth() + layout.columnGap();

            for (int row = 0; row < layout.slotRows(); row++) {
                double yOffset = -(row * (layout.slotHeight() + layout.rowGap()));

                Location bgLoc = space.at(colX, yOffset, 0.0);
                SlotInstance slot = buildSlot(boardId, slotIndex);

                spawnBackground(slot.backgroundKey(), world, bgLoc, hiddenStack, facing.yaw());

                // static answer/points locations (kept identical)
                Location answerCenter = space.at(colX + STATIC_ANSWER_CENTER_X, yOffset, layout.textZOffset());
                Location pointsCenter = space.at(colX + STATIC_POINTS_CENTER_X, yOffset, layout.textZOffset());

                double halfGap = staticMetrics.answerHalfGapBlocks();
                Location answerTopLoc = answerCenter.clone();
                Location answerBotLoc = answerCenter.clone().add(0, -halfGap, 0);

                spawnText(
                        slot.answerTopKey(),
                        world,
                        answerTopLoc,
                        facing.yaw(),
                        facing,
                        staticMetrics,
                        TextDisplay.TextAlignment.LEFT);
                spawnText(
                        slot.answerBottomKey(),
                        world,
                        answerBotLoc,
                        facing.yaw(),
                        facing,
                        staticMetrics,
                        TextDisplay.TextAlignment.LEFT);
                spawnText(
                        slot.pointsKey(),
                        world,
                        pointsCenter,
                        facing.yaw(),
                        facing,
                        staticMetrics,
                        TextDisplay.TextAlignment.RIGHT);

                slots.add(slot);
                slotIndex++;
            }
        }

        instances.put(boardId, new BoardInstance(boardId, anchor.clone(), facing.yaw(), List.copyOf(slots)));
    }

    @Override
    public void destroyBoard(String boardId) {
        if (boardId == null) {
            return;
        }

        BoardInstance instance = instances.remove(boardId);
        if (instance == null) {
            return;
        }

        removeSlotEntities(instance.slots());
        removePanels(instance.boardId());
        metricsByBoardId.remove(boardId);

        if (dynamicIds.remove(boardId)) {
            dynamicStore.removeLayout(boardId);
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
                    setAnswerText(boardId, slot, answer);
                    setPointsText(boardId, slot.pointsKey(), points);
                }));

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

        clearText(slot.answerTopKey());
        clearText(slot.answerBottomKey());
        clearText(slot.pointsKey());
    }

    @Override
    public void clearAll() {
        for (BoardInstance instance : instances.values()) {
            removeSlotEntities(instance.slots());
            removePanels(instance.boardId());
        }
        instances.clear();

        dynamicIds.clear();
        metricsByBoardId.clear();

        dynamicStore.clear();
    }

    @Override
    public BoardInstance createDynamicBoard(String boardId, DynamicBoardLayout dynamicLayout) {
        if (boardId == null || boardId.isBlank() || dynamicLayout == null) {
            return null;
        }
        if (instances.containsKey(boardId)) {
            return null;
        }

        BoardInstance instance = DynamicDisplayBoardFactory.create(boardId, dynamicLayout, displayRegistry);
        if (instance == null) {
            return null;
        }

        instances.put(boardId, instance);
        dynamicIds.add(boardId);

        dynamicStore.saveLayout(boardId, dynamicLayout);
        metricsByBoardId.put(boardId, metricsFromDynamicLayout(dynamicLayout));

        return instance;
    }

    @Override
    public java.util.Collection<String> listBoards() {
        return java.util.Set.copyOf(instances.keySet());
    }

    // *********************************
    // keys + lookup
    // *********************************

    private SlotInstance buildSlot(String boardId, int slotIndex) {
        String slotId = "slot" + (slotIndex + 1);
        DisplayKey bgKey = new DisplayKey("board", boardId, slotId, "bg");
        DisplayKey ansTopKey = new DisplayKey("board", boardId, slotId, "answer_top");
        DisplayKey ansBotKey = new DisplayKey("board", boardId, slotId, "answer_bot");
        DisplayKey ptsKey = new DisplayKey("board", boardId, slotId, "points");
        return new SlotInstance(bgKey, ansTopKey, ansBotKey, ptsKey);
    }

    private SlotInstance slotFor(String boardId, int slotIndex) {
        BoardInstance board = instances.get(boardId);
        if (board == null) {
            return null;
        }

        int idx = slotIndex - 1;
        if (idx < 0 || idx >= board.slots().size()) {
            return null;
        }

        return board.slots().get(idx);
    }

    // *********************************
    // spawn + registry
    // *********************************

    private void spawnBackground(DisplayKey key, World world, Location loc, ItemStack stack, float yaw) {
        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);

            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f(layout.backgroundScaleX(), layout.backgroundScaleY(), layout.backgroundScaleZ()),
                    new AxisAngle4f(0, 0, 0, 0)));
        });

        if (display == null) {
            return;
        }

        DisplayTags.tag(display, "board", key.group());
        displayRegistry.register(key, display);
    }

    private void spawnText(
            DisplayKey key,
            World world,
            Location loc,
            float yaw,
            BoardFacing facing,
            LayoutMetrics metrics,
            TextDisplay.TextAlignment alignment) {
        Location spawnLoc =
                loc.clone().add(facing.forwardX() * TEXT_FORWARD_NUDGE, 0, facing.forwardZ() * TEXT_FORWARD_NUDGE);

        TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);

            entity.setShadowed(true);
            entity.setSeeThrough(false);
            entity.setBackgroundColor(Color.fromARGB(0));
            entity.setBrightness(new Display.Brightness(15, 15));
            entity.setViewRange(64f);

            entity.setAlignment(alignment);
            entity.setLineWidth(LINE_WIDTH_NO_WRAP);

            // deterministic spawn translation
            entity.setTransformation(new Transformation(
                    new Vector3f(0, (float) metrics.spawnTyBlocks(), 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f((float) metrics.baseScale(), (float) metrics.baseScale(), (float) metrics.baseScale()),
                    new AxisAngle4f(0, 0, 0, 0)));

            entity.text(Component.empty());
            entity.setTextOpacity((byte) 0);
        });

        if (display == null) {
            return;
        }

        DisplayTags.tag(display, "board", key.group());
        displayRegistry.register(key, display);
    }

    private void removeSlotEntities(List<SlotInstance> slots) {
        for (SlotInstance slot : slots) {
            animationService.cancel(slot.backgroundKey());
            displayRegistry.remove(slot.backgroundKey());
            displayRegistry.remove(slot.answerTopKey());
            displayRegistry.remove(slot.answerBottomKey());
            displayRegistry.remove(slot.pointsKey());
        }
    }

    // *********************************
    // background cmd
    // *********************************

    private static ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    private static ItemStack withCmd(ItemStack base, float cmd) {
        ItemStack stack = (base == null ? new ItemStack(Material.PAPER) : base.clone());
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    private void setBackgroundCmd(DisplayKey key, float cmd) {
        displayRegistry.resolveItem(key).ifPresent(display -> {
            ItemStack current = display.getItemStack();
            display.setItemStack(withCmd(current, cmd));
        });
    }

    // *********************************
    // text fit policy
    // *********************************

    private enum Region {
        ANSWER,
        POINTS
    }

    private record FitPolicy(
            double charsPerBlock, double minScaleFactor, float txBlocks, TextDisplay.TextAlignment alignment) {}

    private static final FitPolicy ANSWER_FIT = new FitPolicy(
            ANSWER_CHARS_PER_BLOCK, MIN_SCALE_FACTOR_ANSWER, ANSWER_RIGHT_NUDGE_BLOCKS, TextDisplay.TextAlignment.LEFT);

    private static final FitPolicy POINTS_FIT =
            new FitPolicy(POINTS_CHARS_PER_BLOCK, MIN_SCALE_FACTOR_POINTS, 0f, TextDisplay.TextAlignment.RIGHT);

    // *********************************
    // write text
    // *********************************

    private void setAnswerText(String boardId, SlotInstance slot, String raw) {
        String plain = raw == null ? "" : raw.trim();
        if (plain.isEmpty()) {
            clearText(slot.answerTopKey());
            clearText(slot.answerBottomKey());
            return;
        }

        String[] parts = plain.split("\\s+");

        // 2 words go top/bottom
        if (parts.length == 2) {
            positionAnswerDisplays(boardId, slot, true);
            applyFittedText(
                    boardId, slot.answerTopKey(), Component.text(parts[0]), parts[0], ANSWER_FIT, Region.ANSWER);
            applyFittedText(
                    boardId, slot.answerBottomKey(), Component.text(parts[1]), parts[1], ANSWER_FIT, Region.ANSWER);
            return;
        }

        // 1 long word else split mid top/bottom
        if (!plain.contains(" ") && plain.length() >= LONG_WORD_SPLIT_THRESHOLD) {
            int mid = plain.length() / 2;
            String top = plain.substring(0, mid);
            String bot = plain.substring(mid);

            positionAnswerDisplays(boardId, slot, true);
            applyFittedText(boardId, slot.answerTopKey(), Component.text(top), top, ANSWER_FIT, Region.ANSWER);
            applyFittedText(boardId, slot.answerBottomKey(), Component.text(bot), bot, ANSWER_FIT, Region.ANSWER);
            return;
        }

        // default single line go top only
        positionAnswerDisplays(boardId, slot, false);
        applyFittedText(boardId, slot.answerTopKey(), Component.text(plain), plain, ANSWER_FIT, Region.ANSWER);
        clearText(slot.answerBottomKey());
    }

    private void setPointsText(String boardId, DisplayKey key, Integer points) {
        String s = (points == null ? "" : Integer.toString(points));
        Component c = Component.text(s);
        applyFittedText(boardId, key, c, s, POINTS_FIT, Region.POINTS);
    }

    private void clearText(DisplayKey key) {
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.text(Component.empty());
            display.setTextOpacity((byte) 0);
        });
    }

    private void applyFittedText(
            String boardId, DisplayKey key, Component component, String widthSample, FitPolicy fit, Region region) {
        LayoutMetrics m = metricsFor(boardId);

        displayRegistry.resolveText(key).ifPresent(display -> {
            display.setAlignment(fit.alignment());
            display.setLineWidth(LINE_WIDTH_NO_WRAP);

            String sample = widthSample == null ? "" : widthSample.trim();
            if (sample.isEmpty()) {
                display.text(Component.empty());
                display.setTextOpacity((byte) 0);
                return;
            }

            double regionBlocks = (region == Region.ANSWER) ? m.answerWidth() : m.pointsWidth();
            regionBlocks = Math.max(0.01, regionBlocks - (2.0 * m.pad()));

            double targetChars = regionBlocks * fit.charsPerBlock();

            double shrink = targetChars / Math.max(1.0, sample.length());
            shrink = Math.min(1.0, shrink);

            double scale = m.baseScale() * shrink;
            scale = Math.max(scale, m.baseScale() * fit.minScaleFactor());

            float tx = fit.txBlocks();
            float ty = (float) (m.spawnTyBlocks() + m.baselineNudgeBlocks());

            setScaleAndTxTy(display, (float) scale, tx, ty);

            display.text(component);
            display.setTextOpacity((byte) 0xFF);
        });
    }

    // *********************************
    // answer positioning
    // *********************************

    private void positionAnswerDisplays(String boardId, SlotInstance slot, boolean twoLines) {
        LayoutMetrics m = metricsFor(boardId);
        double halfGap = m.answerHalfGapBlocks();

        displayRegistry.resolveText(slot.answerBottomKey()).ifPresent(bottom -> {
            Location bottomLoc = bottom.getLocation();
            Location center = bottomLoc.clone().add(0, halfGap, 0);

            Location desiredBottom = center.clone().add(0, -halfGap, 0);
            teleportIfNeeded(bottom, desiredBottom);

            displayRegistry.resolveText(slot.answerTopKey()).ifPresent(top -> {
                Location desiredTop = center.clone().add(0, twoLines ? halfGap : 0.0, 0);
                teleportIfNeeded(top, desiredTop);
            });
        });
    }

    private static void teleportIfNeeded(TextDisplay display, Location target) {
        Location cur = display.getLocation();
        if (cur.getWorld() == null || target.getWorld() == null) {
            return;
        }
        if (!cur.getWorld().equals(target.getWorld())) {
            return;
        }

        target.setYaw(cur.getYaw());
        target.setPitch(cur.getPitch());

        double dx = cur.getX() - target.getX();
        double dy = cur.getY() - target.getY();
        double dz = cur.getZ() - target.getZ();

        if ((dx * dx) + (dy * dy) + (dz * dz) < 1.0e-8) {
            return;
        }

        display.teleport(target);
    }

    private static void setScaleAndTxTy(TextDisplay display, float scale, float tx, float ty) {
        Transformation current = display.getTransformation();
        Vector3f t = new Vector3f(current.getTranslation());
        t.x = tx;
        t.y = ty;

        display.setTransformation(new Transformation(
                t,
                new AxisAngle4f(current.getLeftRotation()),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(current.getRightRotation())));
    }

    // *********************************
    // metrics
    // *********************************

    private LayoutMetrics metricsFor(String boardId) {
        LayoutMetrics dynamic = metricsByBoardId.get(boardId);
        return (dynamic != null) ? dynamic : staticMetrics;
    }

    private LayoutMetrics metricsFromStaticLayout() {
        double cellW = layout.slotWidth();
        double cellH = layout.slotHeight();

        // 75/25-ish split
        double pointsW = cellW * 0.25;
        double gapW = cellW * 0.03;
        double padW = cellW * 0.06;
        double answerW = cellW - pointsW - gapW;

        double halfGap = (cellH * 0.32) / 2.0;

        // these are the only two "centering" knobs that ever matter
        double spawnTy = 0.0;
        double baselineNudge = -cellH * 0.10;

        return new LayoutMetrics(answerW, pointsW, padW, layout.textScale(), spawnTy, halfGap, baselineNudge);
    }

    private LayoutMetrics metricsFromDynamicLayout(DynamicBoardLayout layout) {
        double cellW = layout.cellWidth();
        double cellH = layout.cellHeight();

        double pointsW = cellW * 0.25;
        double gapW = cellW * 0.03;
        double padW = cellW * 0.06;
        double answerW = cellW - pointsW - gapW;

        double baseScale = Math.max(0.8, Math.min(6.0, cellH * 1.6));

        // match the factory spawn translation so "write-time" transforms aren't
        // fighting spawn-time transforms
        double spawnTy = -cellH * 0.05;
        double baselineNudge = -cellH * 0.10;

        double halfGap = (cellH * 0.32) / 2.0;

        return new LayoutMetrics(answerW, pointsW, padW, baseScale, spawnTy, halfGap, baselineNudge);
    }

    private record LayoutMetrics(
            double answerWidth,
            double pointsWidth,
            double pad,
            double baseScale,
            double spawnTyBlocks,
            double answerHalfGapBlocks,
            double baselineNudgeBlocks) {}

    // *********************************
    // dynamic load
    // *********************************

    private void loadDynamicBoards() {
        for (Map.Entry<String, DynamicBoardLayout> entry :
                dynamicStore.loadLayouts().entrySet()) {
            String boardId = entry.getKey();
            DynamicBoardLayout layout = entry.getValue();
            if (layout == null || layout.worldId() == null) {
                continue;
            }

            // purge old entities for this group id first
            displayRegistry.removeByGroup("board", boardId);

            BoardInstance instance = DynamicDisplayBoardFactory.create(boardId, layout, displayRegistry);
            if (instance != null) {
                instances.put(instance.boardId(), instance);
                dynamicIds.add(instance.boardId());
                metricsByBoardId.put(boardId, metricsFromDynamicLayout(layout));
            }
        }
    }

    // Keeps score/timer panel lifecycle aligned with each associated dynamic board
    // probably will revamp later, but for now easiest for rapid testing
    private void spawnPanels(String boardId, DynamicBoardLayout layout) {
        if (scorePanelPresenter != null) {
            scorePanelPresenter.spawnForBoard(boardId, layout);
        }
        if (timerPanelPresenter != null) {
            timerPanelPresenter.spawnForBoard(boardId, layout);
        }
    }

    private void removePanels(String boardId) {
        if (scorePanelPresenter != null) {
            scorePanelPresenter.removeForBoard(boardId);
        }
        if (timerPanelPresenter != null) {
            timerPanelPresenter.removeForBoard(boardId);
        }
    }
}
