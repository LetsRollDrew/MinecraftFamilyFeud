package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;

// Spawns a display-based board from a DynamicBoardLayout and registers associated entities

public final class DynamicDisplayBoardFactory {

    // hidden background CMD value
    private static final float CMD_HIDDEN = 9001.0f;

    private DynamicDisplayBoardFactory() {
    }

    public static BoardInstance create(String boardId, DynamicBoardLayout layout, DisplayRegistry registry) {
        if (boardId == null || boardId.isBlank() || layout == null || registry == null) {
            return null;
        }
        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return null;
        }

        Location anchor = new Location(world, layout.anchor().x, layout.anchor().y, layout.anchor().z);
        float yaw = layout.facing().yaw();

        ItemStack hiddenStack = stackWithCmd(CMD_HIDDEN);

        List<SlotInstance> slots = new ArrayList<>();
        int slotIndex = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 2; col++) {
                SlotInstance slot = buildSlot(boardId, slotIndex);
                slots.add(slot);
                Location bgLoc = BoardSpace.atCellCenter(anchor, layout.facing(), col, row, layout);
                Location textLoc = bgLoc.clone();
                spawnBackground(slot.backgroundKey(), world, bgLoc, hiddenStack, yaw, layout, registry);
                spawnText(slot.answerKey(), world, textLoc, yaw, layout, registry);
                // optional points display could be placed with a slight vertical offset
                Location pointsLoc = textLoc.clone().add(0, -layout.cellHeight() * 0.25, 0);
                spawnText(slot.pointsKey(), world, pointsLoc, yaw, layout, registry);
                slotIndex++;
            }
        }

        return new BoardInstance(boardId, anchor, yaw, slots);
    }

    private static SlotInstance buildSlot(String boardId, int slotIndex) {
        String slotId = "slot" + (slotIndex + 1);
        DisplayKey bgKey = new DisplayKey("board", boardId, slotId, "bg");
        DisplayKey ansKey = new DisplayKey("board", boardId, slotId, "answer");
        DisplayKey ptsKey = new DisplayKey("board", boardId, slotId, "points");
        return new SlotInstance(bgKey, ansKey, ptsKey);
    }

    private static void spawnBackground(DisplayKey key, World world, Location loc, ItemStack stack, float yaw, DynamicBoardLayout layout, DisplayRegistry registry) {
        ItemDisplay display = world.spawn(loc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            try {
                entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f((float) layout.cellWidth(), (float) layout.cellHeight(), 0.01f),
                    new AxisAngle4f(0, 0, 0, 0)
                ));
            } catch (Throwable ignored) {
            }
        });
        if (display == null) {
            return;
        }
        DisplayTags.tag(display, "board", key.group());
        registry.register(key, display);
    }

    private static void spawnText(DisplayKey key, World world, Location loc, float yaw, DynamicBoardLayout layout, DisplayRegistry registry) {
        Location spawnLoc = loc.clone();
        double forwardNudge = layout.forwardOffset() + 0.08;
        spawnLoc.add(layout.facing().forwardX() * forwardNudge, 0, layout.facing().forwardZ() * forwardNudge);

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
            entity.setLineWidth((int) Math.max(40, layout.cellWidth() * 14));
        });
        if (display == null) {
            return;
        }
        DisplayTags.tag(display, "board", key.group());
        registry.register(key, display);
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
}
