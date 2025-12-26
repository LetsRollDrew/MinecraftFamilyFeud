package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
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
import java.util.UUID;

public final class DisplayBoardService implements DisplayBoardPresenter {
    private static final float CMD_HIDDEN = 9001.0f;
    private static final float CMD_REVEALED = 9002.0f;
    private static final float CMD_FLASH = 9003.0f;

    private final Map<String, BoardInstance> boards = new HashMap<>();
    private final BoardLayout layout = BoardLayout.defaultLayout();

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

                UUID bg = spawnBackground(world, slotLoc, hiddenStack, facing.yaw());
                slot.setBackgroundId(bg);

                Location ansLoc = space.at(colX - 1.3, yOffset, layout.textZOffset());
                UUID ans = spawnText(world, ansLoc, layout.textScale(), facing.yaw());
                slot.setAnswerId(ans);

                Location ptsLoc = space.at(colX + 1.4, yOffset, layout.textZOffset());
                UUID pts = spawnText(world, ptsLoc, layout.textScale(), facing.yaw());
                slot.setPointsId(pts);

                slots.add(slot);
                slotIndex++;
            }
        }

        boards.put(boardId, new BoardInstance(boardId, anchor.clone(), facing.yaw(), slots));
    }

    @Override
    public void destroyBoard(String boardId) {
        BoardInstance instance = boards.remove(boardId);
        if (instance == null) {
            return;
        }
        removeSlotEntities(instance.slots());
    }

    @Override
    public void setSlot(String boardId, int slotIndex, String answer, Integer points, boolean revealed) {
    }

    @Override
    public void revealSlot(String boardId, int slotIndex, String answer, int points) {
    }

    @Override
    public void hideSlot(String boardId, int slotIndex) {
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

    private UUID spawnBackground(World world, Location loc, ItemStack stack, float yaw) {
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
            return null;
        }

        try {
            display.setRotation(yaw, 0f);
        } catch (Throwable ignored) {
        }

        return display.getUniqueId();
    }

    private UUID spawnText(World world, Location loc, float scale, float yaw) {
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
            return null;
        }

        try {
            display.setRotation(yaw, 0f);
        } catch (Throwable ignored) {
        }

        return display.getUniqueId();
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
            removeById(slot.backgroundId());
            removeById(slot.answerId());
            removeById(slot.pointsId());
        }
    }

    private void removeById(UUID id) {
        if (id == null) {
            return;
        }
        var entity = Bukkit.getEntity(id);
        if (entity != null) {
            entity.remove();
        }
    }
}
