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

        float yaw = snapYawToCardinal(facingReference.getLocation().getYaw());
        ItemStack hiddenStack = stackWithCmd(CMD_HIDDEN);

        List<SlotInstance> slots = new ArrayList<>(layout.slotRows() * layout.slotCols());
        int slotIndex = 0;
        for (int col = 0; col < layout.slotCols(); col++) {
            double colX = col == 0 ? 0 : layout.slotWidth() + layout.columnGap();
            for (int row = 0; row < layout.slotRows(); row++) {
                double yOffset = -(row * (layout.slotHeight() + layout.rowGap()));

                Location slotLoc = offsetRelativeToYaw(anchor, yaw, colX, yOffset, 0);
                slotLoc.setYaw(yaw);
                slotLoc.setPitch(0f);

                SlotInstance slot = buildSlot(boardId, slotIndex);

                UUID bg = spawnBackground(world, slotLoc, hiddenStack, yaw);
                slot.setBackgroundId(bg);

                Location ansLoc = offsetRelativeToYaw(slotLoc, yaw, -1.3, 0, layout.textZOffset());
                ansLoc.setYaw(yaw);
                ansLoc.setPitch(0f);
                UUID ans = spawnText(world, ansLoc, layout.textScale(), yaw);
                slot.setAnswerId(ans);

                Location ptsLoc = offsetRelativeToYaw(slotLoc, yaw, 1.4, 0, layout.textZOffset());
                ptsLoc.setYaw(yaw);
                ptsLoc.setPitch(0f);
                UUID pts = spawnText(world, ptsLoc, layout.textScale(), yaw);
                slot.setPointsId(pts);

                slots.add(slot);
                slotIndex++;
            }
        }

        boards.put(boardId, new BoardInstance(boardId, anchor.clone(), yaw, slots));
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

    private static float snapYawToCardinal(float yaw) {
        float y = yaw % 360f;
        if (y < 0) {
            y += 360f;
        }
        float snapped = Math.round(y / 90f) * 90f;
        return snapped % 360f;
    }

    /***********************************************************************
    * Apply an offset in board space relative to yaw (user camera angle)
    * - x is "to the right of the board"
    * - z is "forward out of the board"
    * This keeps board aligned to world axis, idea of 0/90/180/270 degrees only
    * to prevent awkward diagonal orientations.
    * TODO: Potentially enable 45/135/225/315 angles later
    ************************************************************************/
    private static Location offsetRelativeToYaw(Location base, float yawDeg, double x, double y, double z) {
        Location out = base.clone();

        double rad = Math.toRadians(yawDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        double rx = (x * cos) - (z * sin);
        double rz = (x * sin) + (z * cos);

        out.add(rx, y, rz);
        return out;
    }
}
