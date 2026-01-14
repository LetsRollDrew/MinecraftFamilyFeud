package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.render.SlotRevealPainter;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.ui.BookTagger;
import io.letsrolldrew.feud.ui.DisplayHostRemoteBookBuilder;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteKind;
import io.letsrolldrew.feud.ui.HostRemoteService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public final class HostBookService {
    private final Messages messages;
    private final GameController gameController;
    private final HostBookUiBuilder hostBookUiBuilder;
    private final HostRemoteService hostRemoteService;
    private final SurveyRepository surveyRepository;
    private final SlotRevealPainter slotRevealPainter;

    public HostBookService(
            Messages messages,
            GameController gameController,
            HostBookUiBuilder hostBookUiBuilder,
            HostRemoteService hostRemoteService,
            SurveyRepository surveyRepository,
            SlotRevealPainter slotRevealPainter) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.gameController = Objects.requireNonNull(gameController, "gameController");
        this.hostBookUiBuilder = Objects.requireNonNull(hostBookUiBuilder, "hostBookUiBuilder");
        this.hostRemoteService = Objects.requireNonNull(hostRemoteService, "hostRemoteService");
        this.surveyRepository = Objects.requireNonNull(surveyRepository, "surveyRepository");
        this.slotRevealPainter = Objects.requireNonNull(slotRevealPainter, "slotRevealPainter");
    }

    public void giveOrReplaceHostBook(Player player) {
        if (player == null) {
            return;
        }
        ItemStack fresh = hostBookUiBuilder.createBookFor(
                player,
                gameController.slotHoverTexts(),
                gameController.getActiveSurvey(),
                gameController.revealedSlots(),
                gameController.strikeCount(),
                gameController.maxStrikes(),
                gameController.roundPoints(),
                gameController.controllingTeam());
        hostRemoteService.giveOrReplace(player, fresh);
    }

    public void renderReveal(int slot) {
        var survey = gameController.getActiveSurvey();
        if (survey == null) {
            return;
        }
        if (slot < 1 || slot > survey.answers().size()) {
            return;
        }
        var answer = survey.answers().get(slot - 1);
        slotRevealPainter.reveal(slot, answer.text(), answer.points());
    }

    public void giveSelectorBook(Player player) {
        if (player == null) {
            return;
        }
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        BookTagger.tagHostRemote(meta, hostBookUiBuilder.getHostKey(), HostRemoteKind.SELECTOR);
        meta.setTitle("Remote Selector");
        meta.setAuthor("FamilyFeud");
        Component page = Component.text()
                .append(Component.text("Select Board Remote:", NamedTextColor.GOLD))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Map Board Remote", "/feud host book map"))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Display Board Remote", "/feud host book display"))
                .append(Component.newline())
                .append(Component.newline())
                .append(buttonUnderlined("Cleanup Remote", "/feud host book cleanup"))
                .build();
        var bookPages = List.of(page);
        meta.pages(bookPages);
        book.setItemMeta(meta);
        hostRemoteService.giveOrReplace(player, book);
        messages.success(player, Msg.HOST_REMOTE_SELECTOR_GIVEN);
    }

    public void giveDisplayBook(Player player, List<String> boardIds, String boardId) {
        if (player == null || boardIds == null) {
            return;
        }
        List<String> ids = new ArrayList<>(boardIds);
        Collections.sort(ids);

        String target = boardId == null ? "" : boardId.trim();
        if (target.isBlank() && !ids.isEmpty()) {
            target = ids.get(0);
        }

        ItemStack fresh = DisplayHostRemoteBookBuilder.create(
                target, ids, surveyRepository, hostBookUiBuilder.getHostKey(), gameController);
        hostRemoteService.giveOrReplace(player, fresh);
        if (ids.isEmpty()) {
            messages.info(player, Msg.DISPLAY_REMOTE_GIVEN_EMPTY);
            return;
        }
        messages.info(player, Msg.DISPLAY_REMOTE_GIVEN, Placeholder.of("boardId", target));
    }

    public void giveCleanupBook(Player player) {
        if (player == null) {
            return;
        }
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        BookTagger.tagHostRemote(meta, hostBookUiBuilder.getHostKey(), HostRemoteKind.CLEANUP);
        try {
            meta.title(Component.text("Cleanup Remote", NamedTextColor.GRAY));
            meta.author(Component.text("FamilyFeud", NamedTextColor.GRAY));
        } catch (Throwable ignored) {
            meta.setTitle("Cleanup Remote");
            meta.setAuthor("FamilyFeud");
        }
        Component page1 = Component.text()
                .append(button("Board Create (demo)", "/feud board create demo"))
                .append(Component.newline())
                .append(button("Board Destroy (demo)", "/feud board destroy demo"))
                .append(Component.newline())
                .append(button("Board Wand", "/feud board wand"))
                .append(Component.newline())
                .append(button("Board InitMaps", "/feud board initmaps"))
                .build();
        Component page2 = Component.text()
                .append(button("Holo Text Spawn", "/feud holo text spawn demo &fHELLO"))
                .append(Component.newline())
                .append(button("Holo Item Spawn", "/feud holo item spawn demo 9001"))
                .append(Component.newline())
                .append(button("Clear Displays", "/feud clear all"))
                .build();
        meta.pages(List.of(page1, page2));
        book.setItemMeta(meta);
        hostRemoteService.giveOrReplace(player, book);
        messages.success(player, Msg.CLEANUP_BOOK_GIVEN);
    }

    public void giveMapBook(Player player) {
        if (player == null) {
            return;
        }
        ItemStack fresh = hostBookUiBuilder.createBookFor(
                player,
                gameController.slotHoverTexts(),
                gameController.getActiveSurvey(),
                gameController.revealedSlots(),
                gameController.strikeCount(),
                gameController.maxStrikes(),
                gameController.roundPoints(),
                gameController.controllingTeam());
        if (fresh.getItemMeta() instanceof BookMeta meta) {
            meta.lore(java.util.List.of(Component.text("Map Based", NamedTextColor.GRAY)));
            try {
                meta.title(Component.text("Feud Host Book", NamedTextColor.GOLD));
                meta.author(Component.text("Family Feud", NamedTextColor.GOLD));
            } catch (Throwable ignored) {
                meta.setTitle("Feud Host Book");
                meta.setAuthor("Family Feud");
            }
            fresh.setItemMeta(meta);
        }
        hostRemoteService.giveOrReplace(player, fresh);
        messages.success(player, Msg.MAP_BOARD_REMOTE_GIVEN);
    }

    private Component button(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command));
    }

    private Component buttonUnderlined(String label, String command) {
        return Component.text(label, NamedTextColor.GOLD)
                .decorate(net.kyori.adventure.text.format.TextDecoration.UNDERLINED)
                .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand(command));
    }
}
