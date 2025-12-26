package io.letsrolldrew.feud.board.display;

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
    private static final int SLOT_ROWS = 4;
    private static final int SLOT_COLS = 2;
    private static final double SLOT_WIDTH = 3.8;
    private static final double SLOT_HEIGHT = 0.85;
    private static final double COLUMN_GAP = 0.45;
    private static final double ROW_GAP = 0.25;
    private static final float BACKGROUND_SCALE_X = 3.2f;
    private static final float BACKGROUND_SCALE_Y = 1.0f;
    private static final float BACKGROUND_SCALE_Z = 1.0f;
    private static final float TEXT_SCALE = 0.7f;
    private static final double TEXT_Z_OFFSET = 0.02;

    private static final float CMD_HIDDEN = 9001.0f;
    private static final float CMD_REVEALED = 9002.0f;
    private static final float CMD_FLASH = 9003.0f;

    private final Map<String, BoardInstance> boards = new HashMap<>();

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

        int slotCount = SLOT_ROWS * SLOT_COLS;
        List<UUID> backgrounds = new ArrayList<>(slotCount);
        List<UUID> answers = new ArrayList<>(slotCount);
        List<UUID> points = new ArrayList<>(slotCount);

        ItemStack hiddenStack = stackWithCmd(CMD_HIDDEN);

        for (int col = 0; col < SLOT_COLS; col++) {
            double colX = col == 0 ? 0 : SLOT_WIDTH + COLUMN_GAP;

            for (int row = 0; row < SLOT_ROWS; row++) {
                double yOffset = -(row * (SLOT_HEIGHT + ROW_GAP));

                Location slotLoc = offsetRelativeToYaw(anchor, yaw, colX, yOffset, 0);
                slotLoc.setYaw(yaw);
                slotLoc.setPitch(0f); // keep panels upright

                UUID bg = spawnBackground(world, slotLoc, hiddenStack, yaw);
                if (bg != null) {
                    backgrounds.add(bg);
                }

                Location ansLoc = offsetRelativeToYaw(slotLoc, yaw, -1.3, 0, TEXT_Z_OFFSET);
                ansLoc.setYaw(yaw);
                ansLoc.setPitch(0f);

                UUID ans = spawnText(world, ansLoc, TEXT_SCALE, yaw);
                if (ans != null) {
                    answers.add(ans);
                }

                Location ptsLoc = offsetRelativeToYaw(slotLoc, yaw, 1.4, 0, TEXT_Z_OFFSET);
                ptsLoc.setYaw(yaw);
                ptsLoc.setPitch(0f);

                UUID pts = spawnText(world, ptsLoc, TEXT_SCALE, yaw);
                if (pts != null) {
                    points.add(pts);
                }
            }
        }

        boards.put(boardId, new BoardInstance(backgrounds, answers, points));
    }

    @Override
    public void destroyBoard(String boardId) {
        BoardInstance instance = boards.remove(boardId);
        if (instance == null) {
            return;
        }
        removeAll(instance.backgrounds());
        removeAll(instance.answers());
        removeAll(instance.points());
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
            removeAll(instance.backgrounds());
            removeAll(instance.answers());
            removeAll(instance.points());
        }
        boards.clear();
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
                        new Vector3f(BACKGROUND_SCALE_X, BACKGROUND_SCALE_Y, BACKGROUND_SCALE_Z),
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
            // important: FIXED means it won't camera-face
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

    private void removeAll(List<UUID> ids) {
        for (UUID id : ids) {
            var entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
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

    private record BoardInstance(
            List<UUID> backgrounds,
            List<UUID> answers,
            List<UUID> points) {
    }
}
