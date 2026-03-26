package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.effects.lighting.StageLightingService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class LightingRemoteBookBuilder {
    private static final int PAGE_SIZE = 8;

    private LightingRemoteBookBuilder() {}

    public static ItemStack create(StageLightingService lightingService, NamespacedKey hostKey) {
        if (lightingService == null || hostKey == null) {
            throw new IllegalArgumentException("lightingService/hostKey required");
        }

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        BookTagger.tagHostRemote(meta, hostKey, HostRemoteKind.LIGHTING);
        meta.lore(List.of(
                Component.text("Lighting Console", NamedTextColor.GRAY),
                Component.text("Manual Operator Remote", NamedTextColor.DARK_GRAY)));
        try {
            meta.title(Component.text("Lighting Remote", NamedTextColor.AQUA));
            meta.author(Component.text("Family Feud", NamedTextColor.AQUA));
        } catch (Throwable ignored) {
            meta.setTitle("Lighting Remote");
            meta.setAuthor("Family Feud");
        }

        List<Component> pages = new ArrayList<>();
        pages.add(statusPage(lightingService));
        pages.addAll(listPages("Modes", lightingService.modeIds(), "/feud lighting mode "));
        pages.addAll(listPages("Animations", lightingService.animationIds(), "/feud lighting animation "));
        pages.addAll(listPages("Jingles", lightingService.jingleIds(), "/feud lighting jingle "));
        pages.add(columnsPage(lightingService.columnIds()));
        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private static Component statusPage(StageLightingService lightingService) {
        StageLightingService.LightingStatus status = lightingService.status();
        return Component.text()
                .append(Component.text("Lighting", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("Center: " + status.centerOrNone(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Axis: " + status.axis(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Mode: " + status.activeModeOrNone(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Animation: " + status.activeAnimationOrNone(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Columns: " + status.columns(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Palettes: " + status.palettes(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Modes: " + status.modes(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Animations: " + status.animations(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.text("Jingles: " + status.jingles(), NamedTextColor.GRAY))
                .append(Component.newline())
                .append(Component.newline())
                .append(button("Status", "/feud lighting status"))
                .append(Component.newline())
                .append(button("Center Status", "/feud lighting center status"))
                .append(Component.newline())
                .append(button("List Columns", "/feud lighting column list"))
                .append(Component.newline())
                .append(button("Stop Animation", "/feud lighting stop"))
                .build();
    }

    private static List<Component> listPages(String title, Collection<String> ids, String commandPrefix) {
        List<String> sorted = new ArrayList<>(ids == null ? List.of() : ids);
        Collections.sort(sorted);
        if (sorted.isEmpty()) {
            return List.of(Component.text()
                    .append(Component.text(title, NamedTextColor.GOLD))
                    .append(Component.newline())
                    .append(Component.newline())
                    .append(Component.text("None configured", NamedTextColor.GRAY))
                    .build());
        }

        List<Component> pages = new ArrayList<>();
        for (int start = 0; start < sorted.size(); start += PAGE_SIZE) {
            List<Component> lines = new ArrayList<>();
            lines.add(Component.text(title, NamedTextColor.GOLD));
            lines.add(Component.text("Click to run", NamedTextColor.DARK_GRAY));
            lines.add(Component.text(" "));
            int end = Math.min(sorted.size(), start + PAGE_SIZE);
            for (int i = start; i < end; i++) {
                String id = sorted.get(i);
                lines.add(button(id, commandPrefix + id));
            }
            pages.add(Component.join(JoinConfiguration.separator(Component.newline()), lines));
        }
        return pages;
    }

    private static Component columnsPage(Collection<String> columnIds) {
        List<String> sorted = new ArrayList<>(columnIds == null ? List.of() : columnIds);
        Collections.sort(sorted);
        Component body = sorted.isEmpty()
                ? Component.text("None scanned", NamedTextColor.GRAY)
                : Component.text(String.join(", ", sorted), NamedTextColor.GRAY);
        return Component.text()
                .append(Component.text("Columns", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(body)
                .build();
    }

    private static Component button(String label, String command) {
        return Component.text(label, NamedTextColor.AQUA).clickEvent(ClickEvent.runCommand(command));
    }
}
