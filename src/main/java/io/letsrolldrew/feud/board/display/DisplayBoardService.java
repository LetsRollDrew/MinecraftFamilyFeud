package io.letsrolldrew.feud.board.display;

import org.bukkit.Location;
import org.bukkit.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DisplayBoardService implements DisplayBoardPresenter {
    // board is 10 wide by 6 tall counting from the top row.
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 6;
    private static final double TILE_SPACING_X = 0.35;
    private static final double TILE_SPACING_Y = 0.35;
    private static final float TILE_SCALE = 0.55f;

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

        float yaw = facingReference.getLocation().getYaw();
        List<UUID> backgrounds = new ArrayList<>(BOARD_WIDTH * BOARD_HEIGHT);

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                Location slotLoc = anchor.clone().add(x * TILE_SPACING_X, -y * TILE_SPACING_Y, 0);
                slotLoc.setYaw(yaw);
                slotLoc.setPitch(0f); // keep panels upright
                UUID id = spawnBackground(world, slotLoc);
                if (id != null) {
                    backgrounds.add(id);
                }
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

    @Override
    public void clearAll() {
        for (BoardInstance instance : boards.values()) {
            for (UUID id : instance.backgrounds()) {
                var entity = org.bukkit.Bukkit.getEntity(id);
                if (entity != null) {
                    entity.remove();
                }
            }
        }
        boards.clear();
    }

    private UUID spawnBackground(World world, Location loc) {
        TextDisplay display = world.spawn(loc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setShadowed(false);
            entity.setSeeThrough(true);
            try {
                entity.setBackgroundColor(Color.fromARGB(0));
            } catch (Throwable ignored) {
            }
            entity.text(Component.text("\uE000").font(Key.key("feud", "feud")));
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

    // yaw = rotation around the vertical axis, 0 = south, 90 = west, 180 = north, 270 = east
    private float computeYaw(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(-dx, dz));
    }

    private record BoardInstance(List<UUID> backgrounds) {
    }
}
