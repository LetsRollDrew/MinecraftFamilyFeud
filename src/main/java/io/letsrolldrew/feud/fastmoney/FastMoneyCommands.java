package io.letsrolldrew.feud.fastmoney;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.DynamicBoardLayout;
import io.letsrolldrew.feud.board.display.fastmoney.FastMoneyBackdropPresenter;
import io.letsrolldrew.feud.board.display.fastmoney.FastMoneyBoardPresenter;
import io.letsrolldrew.feud.effects.fastmoney.FastMoneyPlayerBindService;
import io.letsrolldrew.feud.effects.timer.TimerService;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.messages.Msg;
import io.letsrolldrew.feud.messages.Placeholder;
import io.letsrolldrew.feud.survey.AnswerOption;
import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.util.Validation;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class FastMoneyCommands {
    private static final String DEFAULT_BOARD_ID = "board1";

    private final Messages messages;
    private final FastMoneyService service;
    private final FastMoneySurveySetStore surveySetStore;
    private final FastMoneyPlayerBindService bindService;
    private final DisplayBoardService displayBoardService;
    private final FastMoneyBoardPresenter boardPresenter;
    private final FastMoneyBackdropPresenter backdropPresenter;
    private final SurveyRepository surveyRepository;
    private final TimerService timerService;
    private final String hostPermission;
    private final String adminPermission;

    private String activeBoardId;

    public FastMoneyCommands(
            Messages messages,
            FastMoneyService service,
            FastMoneySurveySetStore surveySetStore,
            FastMoneyPlayerBindService bindService,
            DisplayBoardService displayBoardService,
            FastMoneyBoardPresenter boardPresenter,
            FastMoneyBackdropPresenter backdropPresenter,
            SurveyRepository surveyRepository,
            TimerService timerService,
            String hostPermission,
            String adminPermission) {
        this.messages = Objects.requireNonNull(messages, "messages");
        this.service = Objects.requireNonNull(service, "service");
        this.surveySetStore = Objects.requireNonNull(surveySetStore, "surveySetStore");
        this.bindService = Objects.requireNonNull(bindService, "bindService");
        this.displayBoardService = Objects.requireNonNull(displayBoardService, "displayBoardService");
        this.boardPresenter = Objects.requireNonNull(boardPresenter, "boardPresenter");
        this.backdropPresenter = Objects.requireNonNull(backdropPresenter, "backdropPresenter");
        this.surveyRepository = Objects.requireNonNull(surveyRepository, "surveyRepository");
        this.timerService = Objects.requireNonNull(timerService, "timerService");
        this.hostPermission = Validation.requireNonBlank(hostPermission, "hostPermission");
        this.adminPermission = Validation.requireNonBlank(adminPermission, "adminPermission");
    }

    public boolean handle(CommandSender sender, String[] args) {
        if (!isHost(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }

        if (args == null || args.length == 0) {
            return usage(sender);
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "set" -> set(sender, args);
            case "start" -> start(sender, args);
            case "stop" -> stop(sender, args);
            case "status" -> status(sender);
            case "next" -> next(sender, args);
            case "bind" -> bind(sender, args);
            case "answer" -> answer(sender, args);
            case "board" -> board(sender, args);
            default -> usage(sender);
        };
    }

    public boolean reveal(CommandSender sender, int questionIndex, int slot) {
        if (!isHost(sender)) {
            messages.error(sender, Msg.HOST_ONLY);
            return true;
        }

        if (questionIndex < 1 || slot < 1) {
            messages.error(sender, Msg.FAST_MONEY_QUESTION_AND_SLOT_MUST_BE_POSITIVE);
            return true;
        }

        if (resolveAwardedAnswer(questionIndex, slot).isEmpty()) {
            messages.error(
                    sender,
                    Msg.FAST_MONEY_SLOT_NOT_DEFINED,
                    Placeholder.of("question", questionIndex),
                    Placeholder.of("slot", slot));
            return true;
        }

        try {
            FastMoneyPhase phase = service.state().phase();
            if (phase == FastMoneyPhase.PLAYER1_TURN) {
                service.awardPlayer1(questionIndex, slot);
                refreshBoard();
                messages.success(
                        sender,
                        Msg.FAST_MONEY_AWARDED_P1,
                        Placeholder.of("question", questionIndex),
                        Placeholder.of("slot", slot));
                return true;
            }
            if (phase == FastMoneyPhase.PLAYER2_TURN) {
                service.awardPlayer2(questionIndex, slot);
                refreshBoard();
                messages.success(
                        sender,
                        Msg.FAST_MONEY_AWARDED_P2,
                        Placeholder.of("question", questionIndex),
                        Placeholder.of("slot", slot));
                return true;
            }
            messages.error(sender, Msg.FAST_MONEY_ROUND_NOT_ACTIVE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            sender.sendMessage(ex.getMessage());
        }
        return true;
    }

    public boolean acceptsResponseFrom(UUID playerId) {
        if (playerId == null) {
            return false;
        }

        FastMoneyRoundState state = service.state();
        if (state.phase() == FastMoneyPhase.PLAYER1_TURN && state.player1().isBound()) {
            return playerId.equals(state.player1().playerId());
        }
        if (state.phase() == FastMoneyPhase.PLAYER2_TURN && state.player2().isBound()) {
            return playerId.equals(state.player2().playerId());
        }
        return false;
    }

    public void captureChatAnswer(Player player, String answer) {
        captureAnswer(player, answer, false);
    }

    private boolean set(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_FAST_MONEY_SET);
            return true;
        }

        String setId = args[1];
        Optional<FastMoneySurveySet> setOpt = surveySetStore.findById(setId);
        if (setOpt.isEmpty()) {
            messages.error(sender, Msg.FAST_MONEY_SET_NOT_FOUND, Placeholder.of("setId", setId));
            return true;
        }

        FastMoneySurveySet set = setOpt.get();
        service.loadSurveySet(set.id(), set.surveyIds());
        timerService.reset(set.player1Seconds());
        refreshBoard();
        messages.success(sender, Msg.FAST_MONEY_SET_LOADED, Placeholder.of("setId", set.id()));

        return true;
    }

    private boolean start(CommandSender sender, String[] args) {
        String boardId = boardIdOrDefault(args, 1);
        try {
            service.startRound();
            timerService.start(currentSurveySet().map(FastMoneySurveySet::player1Seconds).orElse(0));
            showBoard(sender, boardId);
            messages.success(sender, Msg.FAST_MONEY_P1_TURN_STARTED);
        } catch (IllegalStateException ex) {
            sender.sendMessage(ex.getMessage());
        }

        return true;
    }

    private boolean stop(CommandSender sender, String[] args) {
        String boardId = boardIdOrDefault(args, 1);
        service.stop();
        timerService.stop();
        hideBoard(sender, boardId);
        messages.success(sender, Msg.FAST_MONEY_STOPPED);

        return true;
    }

    private boolean status(CommandSender sender) {
        FastMoneyRoundState state = service.state();
        messages.info(
                sender,
                Msg.FAST_MONEY_STATUS,
                Placeholder.of("phase", state.phase()),
                Placeholder.of("set", state.surveySetId()),
                Placeholder.of("question", state.activeQuestionIndex()),
                Placeholder.of("p1", totalPoints(state, true)),
                Placeholder.of("p2", totalPoints(state, false)),
                Placeholder.of("total", totalPoints(state)),
                Placeholder.of("target", currentSurveySet().map(FastMoneySurveySet::targetScore).orElse(0)));
        return true;
    }

    private boolean next(CommandSender sender, String[] args) {
        if (args.length > 1) {
            messages.usage(sender, Msg.USAGE_FAST_MONEY_NEXT);
            return true;
        }

        FastMoneyRoundState before = service.state();
        int totalQuestions = before.questions().size();

        try {
            if (before.phase() == FastMoneyPhase.PLAYER1_TURN || before.phase() == FastMoneyPhase.PLAYER2_TURN) {
                if (before.activeQuestionIndex() < totalQuestions) {
                    service.advanceQuestion();
                    refreshBoard();
                    messages.success(
                            sender,
                            Msg.FAST_MONEY_ADVANCED_TO_QUESTION,
                            Placeholder.of("question", service.state().activeQuestionIndex()));
                    return true;
                }

                if (before.phase() == FastMoneyPhase.PLAYER1_TURN) {
                    service.beginPlayer2Turn();
                    timerService.start(currentSurveySet().map(FastMoneySurveySet::player2Seconds).orElse(0));
                    refreshBoard();
                    messages.success(sender, Msg.FAST_MONEY_P2_TURN_STARTED);
                    return true;
                }

                service.completeRound();
                timerService.stop();
                refreshBoard();
                messages.success(
                        sender,
                        Msg.FAST_MONEY_COMPLETE,
                        Placeholder.of("total", totalPoints(service.state())),
                        Placeholder.of("target", currentSurveySet().map(FastMoneySurveySet::targetScore).orElse(0)));
                return true;
            }

            messages.error(sender, Msg.FAST_MONEY_ROUND_NOT_ACTIVE);
        } catch (IllegalStateException ex) {
            sender.sendMessage(ex.getMessage());
        }

        return true;
    }

    private boolean bind(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_FAST_MONEY_BIND);
            return true;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        if (target.equals("clear")) {
            bindService.clear();
            messages.success(sender, Msg.FAST_MONEY_BINDINGS_CLEARED);
            return true;
        }

        if (!(sender instanceof Player host)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return true;
        }

        if (target.equals("p1")) {
            bindService.armPlayer1(host.getUniqueId());
            messages.info(sender, Msg.FAST_MONEY_BIND_P1_ARMED);
            return true;
        }

        if (target.equals("p2")) {
            bindService.armPlayer2(host.getUniqueId());
            messages.info(sender, Msg.FAST_MONEY_BIND_P2_ARMED);
            return true;
        }

        messages.usage(sender, Msg.USAGE_FAST_MONEY_BIND);
        return true;
    }

    private boolean answer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.error(sender, Msg.PLAYER_ONLY);
            return true;
        }

        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_FAST_MONEY_ANSWER);
            return true;
        }

        String answer =
                String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        captureAnswer(player, answer, true);
        return true;
    }

    private boolean board(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messages.usage(sender, Msg.USAGE_FAST_MONEY_BOARD);
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        String boardId = boardIdOrDefault(args, 2);

        if (action.equals("show")) {
            showBoard(sender, boardId);
            return true;
        }

        if (action.equals("hide")) {
            hideBoard(sender, boardId);
            return true;
        }

        messages.usage(sender, Msg.USAGE_FAST_MONEY_BOARD);
        return true;
    }

    private void showBoard(CommandSender sender, String boardId) {
        DynamicBoardLayout layout = displayBoardService.resolveLayoutOrNull(boardId);
        if (layout == null) {
            messages.error(sender, Msg.FAST_MONEY_BOARD_NO_LAYOUT, Placeholder.of("boardId", boardId));
            return;
        }

        activeBoardId = normalizeBoardId(boardId);
        displayBoardService.showFastMoneyBoard(activeBoardId, layout, boardPresenter, backdropPresenter);
        refreshBoard();
        messages.success(sender, Msg.FAST_MONEY_BOARD_SHOWN, Placeholder.of("boardId", activeBoardId));
    }

    private void hideBoard(CommandSender sender, String boardId) {
        String normalizedBoardId = normalizeBoardId(boardId);
        displayBoardService.hideFastMoneyBoard(normalizedBoardId);
        if (normalizedBoardId.equals(activeBoardId)) {
            activeBoardId = null;
        }
        messages.success(sender, Msg.FAST_MONEY_BOARD_CLEARED, Placeholder.of("boardId", normalizedBoardId));
    }

    private void captureAnswer(Player player, String answer, boolean usageOnBlank) {
        if (player == null) {
            return;
        }

        String trimmed = answer == null ? "" : answer.trim();
        if (trimmed.isEmpty()) {
            if (usageOnBlank) {
                messages.usage(player, Msg.USAGE_FAST_MONEY_ANSWER);
            }
            return;
        }

        int question = service.state().activeQuestionIndex();
        try {
            service.submitAnswer(player.getUniqueId(), trimmed);
            refreshBoard();
            messages.success(player, Msg.FAST_MONEY_ANSWER_RECORDED, Placeholder.of("question", question));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            player.sendMessage(ex.getMessage());
        }
    }

    private void refreshBoard() {
        if (activeBoardId == null || activeBoardId.isBlank()) {
            return;
        }

        boardPresenter.render(activeBoardId, service.state(), surveyRepository);
    }

    private Optional<FastMoneySurveySet> currentSurveySet() {
        String surveySetId = service.state().surveySetId();
        if (surveySetId == null || surveySetId.isBlank()) {
            return Optional.empty();
        }
        return surveySetStore.findById(surveySetId);
    }

    private Optional<AnswerOption> resolveAwardedAnswer(int questionIndex, int slot) {
        if (questionIndex < 1 || questionIndex > service.state().questions().size()) {
            return Optional.empty();
        }

        FastMoneyQuestionState question = service.state().questions().get(questionIndex - 1);
        return surveyRepository.findById(question.surveyId())
                .map(Survey::answers)
                .filter(answers -> slot <= answers.size())
                .map(answers -> answers.get(slot - 1));
    }

    private int totalPoints(FastMoneyRoundState state) {
        return totalPoints(state, true) + totalPoints(state, false);
    }

    private int totalPoints(FastMoneyRoundState state, boolean player1) {
        int total = 0;
        for (FastMoneyQuestionState question : state.questions()) {
            int awardedSlot = player1 ? question.player1AwardedSlot() : question.player2AwardedSlot();
            total += surveyRepository.findById(question.surveyId())
                    .map(Survey::answers)
                    .filter(answers -> awardedSlot > 0 && awardedSlot <= answers.size())
                    .map(answers -> answers.get(awardedSlot - 1).points())
                    .orElse(0);
        }
        return total;
    }

    private boolean usage(CommandSender sender) {
        messages.usage(sender, Msg.FAST_MONEY_HELP);
        return true;
    }

    private boolean isHost(CommandSender sender) {
        if (sender == null) {
            return false;
        }

        if (sender.hasPermission(hostPermission)) {
            return true;
        }

        return sender.hasPermission(adminPermission);
    }

    private String boardIdOrDefault(String[] args, int index) {
        if (args != null && args.length > index) {
            String candidate = args[index];
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return DEFAULT_BOARD_ID;
    }

    private static String normalizeBoardId(String boardId) {
        if (boardId == null || boardId.isBlank()) {
            return DEFAULT_BOARD_ID;
        }
        return boardId;
    }
}
