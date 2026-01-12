package io.letsrolldrew.feud.commands;

import io.letsrolldrew.feud.board.BoardBindingStore;
import io.letsrolldrew.feud.board.BoardWandService;
import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.panels.ScorePanelPresenter;
import io.letsrolldrew.feud.board.display.panels.ScorePanelStore;
import io.letsrolldrew.feud.board.display.panels.TimerPanelStore;
import io.letsrolldrew.feud.board.render.BoardRenderer;
import io.letsrolldrew.feud.board.render.MapIdStore;
import io.letsrolldrew.feud.board.render.SlotRevealPainter;
import io.letsrolldrew.feud.board.render.TileFramebufferStore;
import io.letsrolldrew.feud.commands.spec.CommandSpecificationNode;
import io.letsrolldrew.feud.commands.spec.SpecificationDispatcher;
import io.letsrolldrew.feud.display.DisplayRegistry;
import io.letsrolldrew.feud.effects.board.selection.DisplayBoardSelectionStore;
import io.letsrolldrew.feud.effects.buzz.BuzzerCommands;
import io.letsrolldrew.feud.effects.holo.HologramCommands;
import io.letsrolldrew.feud.effects.timer.TimerCommands;
import io.letsrolldrew.feud.fastmoney.FastMoneyCommands;
import io.letsrolldrew.feud.game.GameController;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.team.TeamCommands;
import io.letsrolldrew.feud.team.TeamService;
import io.letsrolldrew.feud.ui.HostBookAnchorStore;
import io.letsrolldrew.feud.ui.HostBookPage;
import io.letsrolldrew.feud.ui.HostBookUiBuilder;
import io.letsrolldrew.feud.ui.HostRemoteService;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Objects;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class FeudRootCommand implements CommandExecutor {
    private final Plugin plugin;
    private final Messages messages;
    private final UiCommand uiCommand;
    private final HologramCommands hologramCommands;
    private final BoardCommandEntry boardCommandEntry;
    private final HostBookCommandEntry hostBookCommandEntry;
    private final ClearCommandEntry clearCommandEntry;
    private final SurveyCommands surveyCommands;
    private final TeamCommands teamCommands;
    private final TimerCommands timerCommands;
    private final BuzzerCommands buzzerCommands;
    private final FastMoneyCommands fastMoneyCommands;
    private final HostBookAnchorStore hostBookAnchorStore;
    private final HostBookActionRouter hostBookActionRouter;
    private final HostBookService hostBookService;
    private final SpecificationDispatcher dispatcher;

    public FeudRootCommand(
            Plugin plugin,
            Messages messages,
            SurveyRepository surveyRepository,
            HostBookUiBuilder hostBookUiBuilder,
            HostRemoteService hostRemoteService,
            String hostPermission,
            String adminPermission,
            GameController gameController,
            BoardWandService boardWandService,
            BoardBindingStore boardBindingStore,
            MapIdStore mapIdStore,
            TileFramebufferStore framebufferStore,
            BoardRenderer boardRenderer,
            SlotRevealPainter slotRevealPainter,
            CommandModules commandModules,
            CommandSpecificationNode commandSpec,
            io.letsrolldrew.feud.effects.holo.HologramService hologramService,
            DisplayBoardService displayBoardPresenter,
            TeamService teamService,
            ScorePanelPresenter scorePanelPresenter,
            DisplayRegistry displayRegistry,
            ScorePanelStore scorePanelStore,
            TimerPanelStore timerPanelStore,
            HostBookAnchorStore hostBookAnchorStore,
            DisplayBoardSelectionStore displayBoardSelectionStore) {
        this.plugin = plugin;
        this.messages = Objects.requireNonNull(messages, "messages");
        this.hologramCommands = commandModules.hologramCommands();
        DisplayBoardCommands boardCommands = commandModules.displayBoardCommands();
        this.surveyCommands = commandModules.surveyCommands();
        this.teamCommands = commandModules.teamCommands();
        this.timerCommands = commandModules.timerCommands();
        this.buzzerCommands = commandModules.buzzerCommands();
        this.fastMoneyCommands = commandModules.fastMoneyCommands();
        this.hostBookAnchorStore = hostBookAnchorStore;
        this.hostBookActionRouter =
                new HostBookActionRouter(commandModules.fastMoneyCommands(), displayBoardSelectionStore);
        this.hostBookService = new HostBookService(
                gameController, hostBookUiBuilder, hostRemoteService, surveyRepository, slotRevealPainter);
        this.boardCommandEntry = new BoardCommandEntry(
                messages,
                boardCommands,
                adminPermission,
                boardWandService,
                boardBindingStore,
                mapIdStore,
                framebufferStore,
                boardRenderer);
        this.hostBookCommandEntry =
                new HostBookCommandEntry(messages, hostPermission, hostBookService, displayBoardPresenter);
        this.clearCommandEntry = new ClearCommandEntry(
                messages,
                plugin,
                adminPermission,
                displayRegistry,
                displayBoardPresenter,
                hologramService,
                scorePanelStore,
                timerPanelStore);
        this.dispatcher = new SpecificationDispatcher(commandSpec);
        this.uiCommand = new UiCommand(
                messages,
                gameController,
                hostPermission,
                hostBookService::giveOrReplaceHostBook,
                hostBookService::renderReveal,
                teamService,
                scorePanelPresenter,
                scorePanelStore,
                displayBoardPresenter);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        SpecificationDispatcher.DispatchResult result = dispatcher.dispatch(sender, label, args);
        if (result.requirementHandled()) {
            return true;
        }
        if (!result.matched()) {
            return handleHelp(sender);
        }

        if (args == null || args.length == 0) {
            return handleVersion(sender);
        }

        String head = args[0].toLowerCase();
        String[] tail = CommandArgs.tail(args, 1);

        switch (head) {
            case "help":
                return handleHelp(sender);
            case "version":
                return handleVersion(sender);
            case "ui":
                return handleUi(sender, tail);
            case "holo":
                return hologramCommands.handle(sender, tail);
            case "board":
                return boardCommandEntry.handle(sender, tail);
            case "survey":
                return surveyCommands.handle(sender, tail);
            case "team":
                return teamCommands.handle(sender, tail);
            case "buzz":
                return buzzerCommands.handleBuzzReset(sender);
            case "timer":
                return timerCommands.handle(sender, tail);
            case "fastmoney":
                return fastMoneyCommands.handle(sender, tail);
            case "host":
                return hostBookCommandEntry.handle(sender, tail);
            case "clear":
                return clearCommandEntry.handle(sender, tail);
            default:
                return handleHelp(sender);
        }
    }

    private boolean handleUi(CommandSender sender, String[] args) {
        if (args != null && args.length > 0 && "click".equalsIgnoreCase(args[0])) {
            return handleUiClick(sender, args);
        }
        return uiCommand.handle(sender, args);
    }

    private boolean handleUiClick(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return true;
        }
        if (args.length < 4 || !"action".equalsIgnoreCase(args[2])) {
            sender.sendMessage("Usage: /feud ui click <page> action <actionId>");
            return true;
        }

        HostBookPage page = HostBookPage.fromToken(args[1]);
        if (page != null) {
            hostBookAnchorStore.set(player.getUniqueId(), page);
        }

        String actionId = CommandArgs.joinTail(args, 3).trim();
        if (actionId.isBlank()) {
            sender.sendMessage("Usage: /feud ui click <page> action <actionId>");
            return true;
        }

        if (hostBookActionRouter.handle(player, actionId)) {
            hostBookService.giveOrReplaceHostBook(player);
            return true;
        }

        sender.sendMessage("Unknown UI action: " + actionId);
        hostBookService.giveOrReplaceHostBook(player);
        return true;
    }

    public int dispatchFromBrigadier(CommandSourceStack source, java.util.List<String> args) {
        CommandSender sender = source.getSender();
        boolean handled = onCommand(sender, null, "feud", args.toArray(new String[0]));
        return handled ? 1 : 0;
    }

    @SuppressWarnings("deprecation") // Plugin#getDescription is deprecated, fix later
    private boolean handleVersion(CommandSender sender) {
        String version = plugin.getDescription().getVersion();
        sender.sendMessage("FamilyFeud v" + version + " - game state: not started");
        sender.sendMessage("Use /feud help for commands.");
        return true;
    }

    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage("FamilyFeud commands:");
        sender.sendMessage("/feud - show version");
        sender.sendMessage("/feud help - this help");
        sender.sendMessage("/feud version - show version");
        sender.sendMessage("/feud survey ...");
        sender.sendMessage("/feud team info - show teams");
        sender.sendMessage("/feud team reset - reset teams");
        sender.sendMessage("/feud team set <red|blue> name <new-name...>");
        sender.sendMessage("/feud team buzzer bind|clear|test <red|blue>");
        sender.sendMessage("/feud buzz reset");
        sender.sendMessage("/feud host book - give host remote");
        sender.sendMessage("/feud ui reveal <1-8> - reveal slot");
        sender.sendMessage("/feud ui strike - add a strike");
        sender.sendMessage("/feud ui clearstrikes - clear strikes");
        sender.sendMessage("/feud ui add <points> - add points to round");
        sender.sendMessage("/feud board wand - get Display Selector (admin)");
        sender.sendMessage("/feud board initmaps - assign maps to board frames (admin)");
        sender.sendMessage("/feud holo text spawn|set|move|remove ...");
        sender.sendMessage("/feud holo item spawn|move|remove ...");
        sender.sendMessage("/feud holo list");
        sender.sendMessage("/feud clear all - remove all display entities");
        sender.sendMessage("/feud host book cleanup - cleanup remote");
        sender.sendMessage("/feud timer start|stop|reset|status");
        return true;
    }
}
