package io.letsrolldrew.feud.board.display;

import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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
import org.joml.Vector3f;

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

        // slot list needs to be in numeric order
        List<SlotInstance> slots = new ArrayList<>(Collections.nCopies(8, null));

        // find the world positions of the two columns
        Location col0Center = BoardSpace.atCellCenter(anchor, layout.facing(), 0, 0, layout);
        Location col1Center = BoardSpace.atCellCenter(anchor, layout.facing(), 1, 0, layout);

        // compute the direction from col0 to col1
        double dx = col1Center.getX() - col0Center.getX();
        double dz = col1Center.getZ() - col0Center.getZ();

        // how much that direction is aligned with board-right
        double dot = (dx * layout.facing().rightX()) + (dz * layout.facing().rightZ());

        // flip=true means physical col 0 gets slots 5–8 and physical col 1 gets slots 1–4
        boolean flip = dot > 0;

        // 75/25-ish split
        double cellW = layout.cellWidth();
        double cellH = layout.cellHeight();
        double pointsW = cellW * 0.25;
        double gapW = cellW * 0.03;
        double padW = cellW * 0.06;
        double answerW = cellW - pointsW - gapW;

        double textScale = Math.max(0.8, Math.min(6.0, cellH * 1.6));
        double verticalNudge = -cellH * 0.05;

        // initial spawn line widths (DisplayBoardService will override on write)
        double pxPerBlock = 30.0;
        int answerLineWidth = (int) Math.max(1, Math.round((answerW - (2 * padW)) * pxPerBlock / textScale));
        int pointsLineWidth = (int) Math.max(1, Math.round((pointsW - (2 * padW)) * pxPerBlock / textScale));

        // distance between stacked lines (world blocks)
        double lineGap = cellH * 0.32;
        double halfGap = lineGap / 2.0;

        for (int col = 0; col < 2; col++) {
            for (int row = 0; row < 4; row++) {
                int slotCol = flip ? 1 - col : col;
                int slotIndex = (slotCol * 4) + row;

                SlotInstance slot = buildSlot(boardId, slotIndex);
                slots.set(slotIndex, slot);

                Location bgLoc = BoardSpace.atCellCenter(anchor, layout.facing(), col, row, layout);
                spawnBackground(slot.backgroundKey(), world, bgLoc, hiddenStack, yaw, layout, registry);

                double visualSign = flip ? -1.0 : 1.0;

                // IMPORTANT:
                // TextDisplay position behaves like the CENTER of the lineWidth box.
                // So we must place the entity at the CENTER of the answer/points regions,
                // then use lineWidth + alignment to control left/right padding.
                double answerOffset = (-(pointsW / 2.0) - (gapW / 2.0)) * visualSign;
                double pointsOffset = ((answerW / 2.0) + (gapW / 2.0)) * visualSign;

                Location answerLoc = bgLoc.clone().add(
                    layout.facing().rightX() * answerOffset,
                    0,
                    layout.facing().rightZ() * answerOffset
                );

                Location pointsLoc = bgLoc.clone().add(
                    layout.facing().rightX() * pointsOffset,
                    0,
                    layout.facing().rightZ() * pointsOffset
                );

                // spawn top at CENTER, bottom at CENTER - halfGap
                Location answerTopLoc = answerLoc.clone();
                Location answerBotLoc = answerLoc.clone().add(0, -halfGap, 0);

                spawnText(
                    slot.answerTopKey(), world, answerTopLoc, yaw, layout, registry,
                    textScale, answerLineWidth, verticalNudge, TextDisplay.TextAlignment.LEFT
                );
                spawnText(
                    slot.answerBottomKey(), world, answerBotLoc, yaw, layout, registry,
                    textScale, answerLineWidth, verticalNudge, TextDisplay.TextAlignment.LEFT
                );
                spawnText(
                    slot.pointsKey(), world, pointsLoc, yaw, layout, registry,
                    textScale, pointsLineWidth, verticalNudge, TextDisplay.TextAlignment.RIGHT
                );
            }
        }

        if (slots.contains(null)) {
            Bukkit.getLogger().warning("[feud] dynamic board " + boardId + " has unassigned slots");
        }

        return new BoardInstance(boardId, anchor, yaw, List.copyOf(slots));
    }

    private static SlotInstance buildSlot(String boardId, int slotIndex) {
        String slotId = "slot" + (slotIndex + 1);
        DisplayKey bgKey = new DisplayKey("board", boardId, slotId, "bg");
        DisplayKey ansTopKey = new DisplayKey("board", boardId, slotId, "answer_top");
        DisplayKey ansBotKey = new DisplayKey("board", boardId, slotId, "answer_bot");
        DisplayKey ptsKey = new DisplayKey("board", boardId, slotId, "points");
        return new SlotInstance(bgKey, ansTopKey, ansBotKey, ptsKey);
    }

    private static void spawnBackground(
        DisplayKey key,
        World world,
        Location loc,
        ItemStack stack,
        float yaw,
        DynamicBoardLayout layout,
        DisplayRegistry registry
    ) {
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

    private static void spawnText(
        DisplayKey key,
        World world,
        Location loc,
        float yaw,
        DynamicBoardLayout layout,
        DisplayRegistry registry,
        double textScale,
        int lineWidth,
        double verticalNudge,
        TextDisplay.TextAlignment alignment
    ) {
        Location spawnLoc = loc.clone();

        // BoardSpace.atCellCenter already applied forwardOffset via bgLoc,
        // and answerLoc/pointsLoc are derived from that, so only a tiny nudge is needed.
        double forwardNudge = 0.08;
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
            entity.setAlignment(alignment);
            entity.setLineWidth(lineWidth);
            try {
                entity.setTransformation(new Transformation(
                    new Vector3f(0, (float) verticalNudge, 0),
                    new AxisAngle4f(0, 0, 0, 0),
                    new Vector3f((float) textScale, (float) textScale, (float) textScale),
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
