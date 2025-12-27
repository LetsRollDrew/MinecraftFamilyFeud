package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.effects.anim.AnimationService;
import io.letsrolldrew.feud.effects.anim.AnimationStep;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
    private final BoardLayout layout = BoardLayout.defaultLayout();
    private final DisplayRegistry displayRegistry;
    private final AnimationService animationService;

    public DisplayBoardService(DisplayRegistry displayRegistry, AnimationService animationService) {
        this.displayRegistry = displayRegistry;
        this.animationService = animationService;
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
                spawnText(slot.answerKey(), world, ansLoc, layout.textScale(), facing.yaw());

                Location ptsLoc = space.at(colX + 1.4, yOffset, layout.textZOffset());
                spawnText(slot.pointsKey(), world, ptsLoc, layout.textScale(), facing.yaw());

                slots.add(slot);
                slotIndex++;
            }
        }

        boards.put(boardId, new BoardInstance(boardId, anchor.clone(), facing.yaw(), slots));
    }

    @Override
    public void destroyBoard(String boardId) {
        BoardInstance instance = boards.remove(boardId);
        if (instance != null) {
            removeSlotEntities(instance.slots());
            return;
        }
        BoardInstance dyn = dynamicBoards.remove(boardId);
        if (dyn != null) {
            removeSlotEntities(dyn.slots());
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
            new AnimationStep(2, () -> {
                setBackgroundCmd(slot.backgroundKey(), CMD_REVEALED);
                setAnswerText(slot.answerKey(), answer);
                setPointsText(slot.pointsKey(), points);
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

        displayRegistry.register(key, display);
    }

    private void spawnText(DisplayKey key, World world, Location loc, float scale, float yaw) {
        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);

            entity.setShadowed(false);
            entity.setSeeThrough(true);
            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }

            entity.text(Component.empty().font(Key.key("feud", "feud")));

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

    private void setAnswerText(DisplayKey key, String text) {
        Component value = text == null ? Component.empty() : Component.text(text);
        setText(key, value);
    }

    private void setPointsText(DisplayKey key, Integer points) {
        String value = points == null ? "" : Integer.toString(points);
        setText(key, Component.text(value));
    }

    private void clearText(DisplayKey key) {
        setText(key, Component.empty());
    }

    private void setText(DisplayKey key, Component component) {
        displayRegistry.resolveText(key).ifPresent(display -> display.text(component.font(Key.key("feud", "feud"))));
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
}
