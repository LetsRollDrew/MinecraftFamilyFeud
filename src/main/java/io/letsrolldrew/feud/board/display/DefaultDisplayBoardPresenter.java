package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DefaultDisplayBoardPresenter implements DisplayBoardPresenter {
    private static final int SLOT_COUNT = 8;
    private static final double SLOT_SPACING_Y = 0.45;
    private static final float TILE_SCALE = 2.2f;

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

        float yaw = computeYaw(anchor, facingReference.getLocation());
        List<UUID> backgrounds = new ArrayList<>(SLOT_COUNT);
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            Location slotLoc = anchor.clone().add(0, -slot * SLOT_SPACING_Y, 0);
            slotLoc.setYaw(yaw);
            UUID id = spawnBackground(world, slotLoc);
            if (id != null) {
                backgrounds.add(id);
            }
        }
        boards.put(boardId, new BoardInstance(backgrounds));
    }

    @Override
    public void destroyBoard(String boardId) {
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

    private UUID spawnBackground(World world, Location loc) {
        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(new ItemStack(Material.ECHO_SHARD));
            entity.setBillboard(Display.Billboard.VERTICAL);
            try {
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f(TILE_SCALE, TILE_SCALE, TILE_SCALE),
                    new AxisAngle4f(0, 0, 0, 0)
                ));
            } catch (Throwable ignored) {
            }
        });
        return display == null ? null : display.getUniqueId();
    }

    private float computeYaw(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    private record BoardInstance(List<UUID> backgrounds) {
    }
}
