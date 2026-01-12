package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class RequirementsTest {

    @Test
    void permissionPassesWhenSenderHasPermission() {
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("perm.node")).thenReturn(true);

        Requirement requirement = Requirements.permission("perm.node");

        assertTrue(requirement.test(sender));
        assertTrue(requirement.message().isPresent());
    }

    @Test
    void permissionFailsWhenSenderLacksPermission() {
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("perm.node")).thenReturn(false);

        Requirement requirement = Requirements.permission("perm.node");

        assertFalse(requirement.test(sender));
    }

    @Test
    void playerOnlyPassesForPlayers() {
        CommandSender sender = Mockito.mock(Player.class);

        Requirement requirement = Requirements.playerOnly();

        assertTrue(requirement.test(sender));
        assertTrue(requirement.message().isPresent());
    }

    @Test
    void playerOnlyFailsForNonPlayers() {
        CommandSender sender = Mockito.mock(CommandSender.class);

        Requirement requirement = Requirements.playerOnly();

        assertFalse(requirement.test(sender));
    }

    @Test
    void anyOfPassesWhenOneRequirementPasses() {
        Requirement allow = fixedRequirement(true);
        Requirement deny = fixedRequirement(false);

        Requirement requirement = Requirements.anyOf(deny, allow);

        assertTrue(requirement.test(Mockito.mock(CommandSender.class)));
    }

    @Test
    void allOfFailsWhenAnyRequirementFails() {
        Requirement allow = fixedRequirement(true);
        Requirement deny = fixedRequirement(false);

        Requirement requirement = Requirements.allOf(allow, deny);

        assertFalse(requirement.test(Mockito.mock(CommandSender.class)));
    }

    @Test
    void combinatorsUseFirstAvailableMessage() {
        Requirement withMessage = new Requirement() {
            @Override
            public boolean test(CommandSender sender) {
                return false;
            }

            @Override
            public Optional<String> message() {
                return Optional.of("first");
            }
        };

        Requirement withoutMessage = fixedRequirement(true);

        Requirement combined = Requirements.anyOf(withMessage, withoutMessage);

        assertTrue(combined.message().isPresent());
    }

    private Requirement fixedRequirement(boolean result) {
        return new Requirement() {
            @Override
            public boolean test(CommandSender sender) {
                return result;
            }

            @Override
            public Optional<String> message() {
                return Optional.empty();
            }
        };
    }
}
