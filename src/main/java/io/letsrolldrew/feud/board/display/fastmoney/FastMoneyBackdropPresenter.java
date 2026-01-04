package io.letsrolldrew.feud.board.display.fastmoney;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

public final class FastMoneyBackdropPresenter {
    private static final float CMD_PANEL = 9005.0f;
    private static final String NAMESPACE = "fastmoney";

    private final DisplayRegistry displayRegistry;

    public FastMoneyBackdropPresenter(DisplayRegistry displayRegistry) {
        this.displayRegistry = Objects.requireNonNull(displayRegistry, "displayRegistry");
    }

    public void spawn(String boardId, DynamicBoardLayout layout) {
        if (layout == null) {
            return;
        }
        String group = normalizeBoardId(boardId);
        remove(group);

        World world = Bukkit.getWorld(layout.worldId());
        if (world == null) {
            return;
        }

        Location center = toCenter(world, layout);
        ItemStack stack = stackWithCmd(CMD_PANEL);
        float yaw = layout.facing().yaw();

        ItemDisplay display = world.spawn(center, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            try {
                entity.setTransformation(new org.bukkit.util.Transformation(
                        entity.getTransformation().getTranslation(),
                        entity.getTransformation().getLeftRotation(),
                        new org.joml.Vector3f((float) layout.totalWidth(), (float) layout.totalHeight(), 0.01f),
                        entity.getTransformation().getRightRotation()));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayTags.tag(display, NAMESPACE, group);
        displayRegistry.register(new DisplayKey(NAMESPACE, group, "backdrop", "bg"), display);
    }

    public void remove(String boardId) {
        String group = normalizeBoardId(boardId);
        displayRegistry.removeByGroup(NAMESPACE, group);
    }

    private static ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        var meta = stack.getItemMeta();
        var cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    private Location toCenter(World world, DynamicBoardLayout layout) {
        double centerX = (layout.minCorner().x + layout.maxCorner().x) / 2.0;
        double centerY = (layout.minCorner().y + layout.maxCorner().y) / 2.0;
        double centerZ = (layout.minCorner().z + layout.maxCorner().z) / 2.0;
        return new Location(world, centerX, centerY, centerZ);
    }

    private String normalizeBoardId(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return "board1";
        }
        return boardId;
    }
}
