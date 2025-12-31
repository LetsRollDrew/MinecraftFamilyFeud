package io.letsrolldrew.feud.team;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class TeamServiceTest {

    @Test
    void defaultsArePresent() {
        TeamService service = new TeamService();

        assertEquals("Red", service.getName(TeamId.RED));
        assertEquals("Blue", service.getName(TeamId.BLUE));
        assertEquals(0, service.getScore(TeamId.RED));
        assertEquals(0, service.getScore(TeamId.BLUE));
        assertNull(service.getBuzzer(TeamId.RED));
        assertNull(service.getBuzzer(TeamId.BLUE));
    }

    @Test
    void setNameIgnoresBlank() {
        TeamService service = new TeamService();

        assertFalse(service.setName(TeamId.RED, "   "));
        assertEquals("Red", service.getName(TeamId.RED));
    }

    @Test
    void setNameTrimsAndUpdates() {
        TeamService service = new TeamService();

        assertTrue(service.setName(TeamId.BLUE, "  Blueberries  "));
        assertEquals("Blueberries", service.getName(TeamId.BLUE));
    }

    @Test
    void addScoreClampsAtZero() {
        TeamService service = new TeamService();

        assertTrue(service.addScore(TeamId.RED, 25));
        assertEquals(25, service.getScore(TeamId.RED));

        assertTrue(service.addScore(TeamId.RED, -1000));
        assertEquals(0, service.getScore(TeamId.RED));
    }

    @Test
    void setAndClearBuzzer() {
        TeamService service = new TeamService();

        BlockRef ref = new BlockRef(UUID.randomUUID(), 10, 64, -5);

        assertTrue(service.setBuzzer(TeamId.BLUE, ref));
        assertEquals(ref, service.getBuzzer(TeamId.BLUE));

        assertTrue(service.clearBuzzer(TeamId.BLUE));
        assertNull(service.getBuzzer(TeamId.BLUE));
    }

    @Test
    void resetRestoresDefaults() {
        TeamService service = new TeamService();

        service.setName(TeamId.RED, "Reds");
        service.addScore(TeamId.RED, 10);
        service.setBuzzer(TeamId.RED, new BlockRef(UUID.randomUUID(), 1, 2, 3));

        service.reset();

        assertEquals("Red", service.getName(TeamId.RED));
        assertEquals(0, service.getScore(TeamId.RED));
        assertNull(service.getBuzzer(TeamId.RED));
    }
}
