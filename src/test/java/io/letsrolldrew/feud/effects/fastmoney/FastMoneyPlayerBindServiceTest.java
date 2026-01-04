package io.letsrolldrew.feud.effects.fastmoney;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.letsrolldrew.feud.fastmoney.FastMoneyService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

final class FastMoneyPlayerBindServiceTest {
    private FastMoneyService service;
    private FastMoneyPlayerBindService bindService;
    private UUID hostId;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        service = new FastMoneyService();
        service.loadSurveySet("s1", java.util.List.of("a", "b", "c", "d", "e"));
        bindService = new FastMoneyPlayerBindService(service);
        hostId = UUID.randomUUID();
        playerId = UUID.randomUUID();
    }

    @Test
    void armsAndBindsPlayer1ThenClears() {
        bindService.armPlayer1(hostId);

        boolean bound = bindService.bindIfArmed(hostId, playerId, "PlayerOne");

        assertTrue(bound);
        assertFalse(bindService.isArmed(hostId));
    }

    @Test
    void wrongHostCannotBind() {
        bindService.armPlayer1(hostId);

        boolean bound = bindService.bindIfArmed(UUID.randomUUID(), playerId, "Other");

        assertFalse(bound);
        assertTrue(bindService.isArmed(hostId));
    }

    @Test
    void clearDisarmsBinding() {
        bindService.armPlayer2(hostId);
        bindService.clear();

        assertFalse(bindService.bindIfArmed(hostId, playerId, "Nope"));
        assertFalse(bindService.isArmed(hostId));
    }
}
