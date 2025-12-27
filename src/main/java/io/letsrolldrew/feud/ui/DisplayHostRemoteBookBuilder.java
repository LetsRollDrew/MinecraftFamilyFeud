package io.letsrolldrew.feud.ui;

import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.survey.SurveyRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DisplayHostRemoteBookBuilder {

    private DisplayHostRemoteBookBuilder() {
    }

    public static ItemStack create(
        String boardId,
        Collection<String> boardIds,
        SurveyRepository surveyRepository,
        NamespacedKey hostKey,
        GameController controller
    ) {
        if (boardId == null || boardId.isBlank() || surveyRepository == null || hostKey == null || controller == null) {
            throw new IllegalArgumentException("boardId/surveyRepository/hostKey/controller required");
        }

        HostBookUiBuilder base = new HostBookUiBuilder(
            "/feud board display remote " + boardId,
            surveyRepository,
            null,
            hostKey
        );

        ItemStack book = base.createBook(
            controller.slotHoverTexts(),
            controller.getActiveSurvey(),
            controller.revealedSlots(),
            controller.strikeCount(),
            controller.maxStrikes(),
            controller.roundPoints(),
            controller.controllingTeam()
        );

        if (!(book.getItemMeta() instanceof BookMeta meta)) {
            return book;
        }

        meta.lore(List.of(
            Component.text("Display Based", NamedTextColor.GRAY),
            Component.text("Board: " + boardId, NamedTextColor.GRAY)
        ));

        try {
            meta.title(Component.text("Feud Host Book", NamedTextColor.AQUA));
            meta.author(Component.text("Family Feud", NamedTextColor.AQUA));
        } catch (Throwable ignored) {
            meta.setTitle("Feud Host Book");
            meta.setAuthor("Family Feud");
        }

        List<Component> pages = new ArrayList<>(meta.pages());
        pages.add(boardListPage(boardId, boardIds));
        meta.pages(pages);
        book.setItemMeta(meta);
        return book;
    }

    private static Component boardListPage(String activeId, Collection<String> boardIds) {
        Component header = Component.text("Display Boards", NamedTextColor.GOLD);
        if (boardIds == null || boardIds.isEmpty()) {
            return Component.text()
                .append(header)
                .append(Component.newline())
                .append(Component.newline())
                .append(Component.text("No boards found", NamedTextColor.GRAY))
                .build();
        }

        List<Component> lines = new ArrayList<>();
        lines.add(header);
        lines.add(Component.text("Active: " + activeId, NamedTextColor.GRAY));
        lines.add(Component.text(" "));

        for (String id : boardIds) {
            if (id == null || id.isBlank()) {
                continue;
            }
            NamedTextColor color = id.equals(activeId) ? NamedTextColor.GREEN : NamedTextColor.AQUA;
            Component line = Component.text(id, color)
                .clickEvent(ClickEvent.runCommand("/feud host book display " + id));
            lines.add(line);
        }

        return Component.join(JoinConfiguration.separator(Component.newline()), lines);
    }
}
