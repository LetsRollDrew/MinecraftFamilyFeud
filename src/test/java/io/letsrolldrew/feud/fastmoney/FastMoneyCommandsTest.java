package io.letsrolldrew.feud.fastmoney;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.letsrolldrew.feud.board.display.DisplayBoardService;
import io.letsrolldrew.feud.board.display.fastmoney.FastMoneyBackdropPresenter;
import io.letsrolldrew.feud.board.display.fastmoney.FastMoneyBoardPresenter;
import io.letsrolldrew.feud.effects.fastmoney.FastMoneyPlayerBindService;
import io.letsrolldrew.feud.effects.timer.TimerService;
import io.letsrolldrew.feud.messages.Messages;
import io.letsrolldrew.feud.survey.SurveyRepository;
import java.util.List;
import java.util.UUID;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FastMoneyCommandsTest {
    private static final List<String> SURVEY_IDS = List.of("q1", "q2", "q3", "q4", "q5");

    private FastMoneyService service;
    private FastMoneyCommands commands;
    private TimerService timerService;
    private CommandSender host;
    private Player player1;
    private Player player2;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() throws InvalidConfigurationException {
        service = new FastMoneyService();
        SurveyRepository surveyRepository = surveyRepository();
        FastMoneySurveySetStore surveySetStore = surveySetStore(surveyRepository);
        timerService = mock(TimerService.class);
        host = mock(CommandSender.class);
        player1 = mock(Player.class);
        player2 = mock(Player.class);
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();

        when(host.hasPermission(anyString())).thenReturn(true);
        when(player1.getUniqueId()).thenReturn(player1Id);
        when(player2.getUniqueId()).thenReturn(player2Id);

        commands = new FastMoneyCommands(
                new Messages("FamilyFeud"),
                service,
                surveySetStore,
                new FastMoneyPlayerBindService(service),
                mock(DisplayBoardService.class),
                mock(FastMoneyBoardPresenter.class),
                mock(FastMoneyBackdropPresenter.class),
                surveyRepository,
                timerService,
                "familyfeud.host",
                "familyfeud.admin");
    }

    @Test
    void setLoadsSurveyPackAndResetsTimerToPlayerOneSeconds() {
        boolean handled = commands.handle(host, new String[] {"set", "s1"});

        assertTrue(handled);
        assertEquals(FastMoneyPhase.READY, service.state().phase());
        assertEquals("s1", service.state().surveySetId());
        verify(timerService).reset(20);
    }

    @Test
    void nextAdvancesWithinPlayerTurnWithoutTouchingTimer() {
        startPlayer1Turn();

        boolean handled = commands.handle(host, new String[] {"next"});

        assertTrue(handled);
        assertEquals(2, service.state().activeQuestionIndex());
        verifyNoInteractions(timerService);
    }

    @Test
    void nextStartsPlayerTwoTurnUsingConfiguredSeconds() {
        startPlayer1Turn();
        advanceToLastQuestion();

        boolean handled = commands.handle(host, new String[] {"next"});

        assertTrue(handled);
        assertEquals(FastMoneyPhase.PLAYER2_TURN, service.state().phase());
        assertEquals(1, service.state().activeQuestionIndex());
        verify(timerService).start(25);
    }

    @Test
    void nextCompletesRoundAndStopsTimer() {
        startPlayer2LastQuestion();

        boolean handled = commands.handle(host, new String[] {"next"});

        assertTrue(handled);
        assertEquals(FastMoneyPhase.COMPLETE, service.state().phase());
        verify(timerService).stop();
    }

    @Test
    void chatCaptureStoresTrimmedAnswerForCurrentResponder() {
        startPlayer1Turn();

        commands.captureChatAnswer(player1, "  apples  ");

        assertEquals("apples", service.state().questions().get(0).player1RawAnswer());
    }

    private void startPlayer1Turn() {
        service.loadSurveySet("s1", SURVEY_IDS);
        service.bindPlayer1(player1Id, "P1");
        service.bindPlayer2(player2Id, "P2");
        service.startRound();
    }

    private void startPlayer2LastQuestion() {
        startPlayer1Turn();
        advanceToLastQuestion();
        service.beginPlayer2Turn();
        advanceToLastQuestion();
    }

    private void advanceToLastQuestion() {
        service.advanceQuestion();
        service.advanceQuestion();
        service.advanceQuestion();
        service.advanceQuestion();
    }

    private static SurveyRepository surveyRepository() throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
                surveys:
                  q1:
                    question: "Q1"
                    answers:
                      - text: "A1"
                        points: 30
                      - text: "A2"
                        points: 20
                      - text: "A3"
                        points: 10
                  q2:
                    question: "Q2"
                    answers:
                      - text: "B1"
                        points: 30
                      - text: "B2"
                        points: 20
                      - text: "B3"
                        points: 10
                  q3:
                    question: "Q3"
                    answers:
                      - text: "C1"
                        points: 30
                      - text: "C2"
                        points: 20
                      - text: "C3"
                        points: 10
                  q4:
                    question: "Q4"
                    answers:
                      - text: "D1"
                        points: 30
                      - text: "D2"
                        points: 20
                      - text: "D3"
                        points: 10
                  q5:
                    question: "Q5"
                    answers:
                      - text: "E1"
                        points: 30
                      - text: "E2"
                        points: 20
                      - text: "E3"
                        points: 10
                """);
        return SurveyRepository.load(config);
    }

    private static FastMoneySurveySetStore surveySetStore(SurveyRepository surveyRepository)
            throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        config.loadFromString("""
                fastMoney:
                  packs:
                    s1:
                      targetScore: 200
                      player1Seconds: 20
                      player2Seconds: 25
                      surveys: ["q1", "q2", "q3", "q4", "q5"]
                """);
        return FastMoneySurveySetStore.load(config, surveyRepository);
    }
}
