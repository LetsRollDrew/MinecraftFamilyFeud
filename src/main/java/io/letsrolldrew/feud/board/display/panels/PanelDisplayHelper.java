package io.letsrolldrew.feud.board.display.panels;

import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.display.DisplayKey;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.display.DisplayTags;
import net.kyori.adventure.text.Component;
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
import org.joml.Vector3d;
import org.joml.Vector3f;

final class PanelDisplayHelper {
    private PanelDisplayHelper() {}

    static ItemStack stackWithCmd(float cmd) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();
        cmdComponent.setFloats(java.util.List.of(cmd));
        meta.setCustomModelDataComponent(cmdComponent);
        stack.setItemMeta(meta);
        return stack;
    }

    static Location toLocation(World world, Vector3d center, float yaw) {
        return new Location(world, center.x, center.y, center.z, yaw, 0f);
    }

    static void spawnBackground(
            DisplayRegistry displayRegistry,
            DisplayKey key,
            World world,
            Location centerLoc,
            float yaw,
            double panelWidth,
            double panelHeight,
            ItemStack stack,
            String tagNamespace) {

        if (displayRegistry == null || key == null || world == null || centerLoc == null || stack == null) {
            return;
        }

        ItemDisplay display = world.spawn(centerLoc, ItemDisplay.class, entity -> {
            entity.setItemStack(stack);
            entity.setBillboard(Display.Billboard.FIXED);
            entity.setRotation(yaw, 0f);
            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) panelWidth, (float) panelHeight, 0.01f),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayTags.tag(display, tagNamespace, key.group());
        displayRegistry.register(key, display);
    }

    static void spawnText(
            DisplayRegistry displayRegistry,
            DisplayKey key,
            World world,
            Location baseLoc,
            float yaw,
            DynamicBoardLayout layout,
            int lineWidth,
            double textScale,
            double verticalNudge,
            double forwardNudge,
            String tagNamespace,
            boolean startVisible) {

        if (displayRegistry == null || key == null || world == null || baseLoc == null || layout == null) {
            return;
        }

        Location spawnLoc = baseLoc.clone()
                .add(
                        layout.facing().forwardX() * forwardNudge,
                        0,
                        layout.facing().forwardZ() * forwardNudge);

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

            entity.setViewRange(48f);
            entity.setAlignment(TextDisplay.TextAlignment.CENTER);
            entity.setLineWidth(lineWidth);
            entity.text(Component.empty());
            entity.setTextOpacity(startVisible ? (byte) 0xFF : (byte) 0x00);

            try {
                entity.setTransformation(new Transformation(
                        new Vector3f(0, (float) verticalNudge, 0),
                        new AxisAngle4f(0, 0, 0, 0),
                        new Vector3f((float) textScale, (float) textScale, (float) textScale),
                        new AxisAngle4f(0, 0, 0, 0)));
            } catch (Throwable ignored) {
            }
        });

        if (display == null) {
            return;
        }
        DisplayTags.tag(display, tagNamespace, key.group());
        displayRegistry.register(key, display);
    }

    static void setText(DisplayRegistry displayRegistry, DisplayKey key, Component text) {
        if (displayRegistry == null || key == null) {
            return;
        }
        displayRegistry.resolveText(key).ifPresent(display -> {
            display.text(text == null ? Component.empty() : text);
            display.setTextOpacity((byte) 0xFF);
        });
    }

    static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
