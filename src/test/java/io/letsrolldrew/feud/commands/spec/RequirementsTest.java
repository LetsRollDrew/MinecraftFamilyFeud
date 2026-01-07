package io.letsrolldrew.feud.commands.spec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RequirementsTest {

    @Test
    void permissionPassesWhenSenderHasPermission() {
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("familyfeud.admin")).thenReturn(true);

        Requirement requirement = Requirements.permission("familyfeud.admin");

        assertTrue(requirement.test(sender));
        assertTrue(requirement.message().isPresent());
        assertTrue(requirement.message().get().contains("familyfeud.admin"));
    }

    @Test
    void permissionFailsWhenSenderLacksPermission() {
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("familyfeud.admin")).thenReturn(false);

        Requirement requirement = Requirements.permission("familyfeud.admin");

        assertFalse(requirement.test(sender));
    }

    @Test
    void playerOnlyPassesForPlayerSender() {
        Player sender = Mockito.mock(Player.class);

        Requirement requirement = Requirements.playerOnly();

        assertTrue(requirement.test(sender));
        assertTrue(requirement.message().isPresent());
        assertTrue(requirement.message().get().contains("player"));
    }

    @Test
    void playerOnlyFailsForNonPlayerSender() {
        CommandSender sender = Mockito.mock(CommandSender.class);

        Requirement requirement = Requirements.playerOnly();

        assertFalse(requirement.test(sender));
    }

    @Test
    void anyOfPassesWhenAnyRequirementPasses() {
        Requirement needsAdmin = Requirements.permission("familyfeud.admin");
        Requirement needsHost = Requirements.permission("familyfeud.host");
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("familyfeud.host")).thenReturn(true);
        Mockito.when(sender.hasPermission("familyfeud.admin")).thenReturn(false);

        Requirement any = Requirements.anyOf(needsAdmin, needsHost);

        assertTrue(any.test(sender));
        assertTrue(any.message().isPresent());
        assertTrue(any.message().get().contains("familyfeud.admin"));
    }

    @Test
    void allOfFailsWhenAnyRequirementFails() {
        Requirement needsAdmin = Requirements.permission("familyfeud.admin");
        Requirement needsHost = Requirements.permission("familyfeud.host");
        CommandSender sender = Mockito.mock(CommandSender.class);
        Mockito.when(sender.hasPermission("familyfeud.host")).thenReturn(true);
        Mockito.when(sender.hasPermission("familyfeud.admin")).thenReturn(false);

        Requirement all = Requirements.allOf(needsAdmin, needsHost);

        assertFalse(all.test(sender));
        assertTrue(all.message().isPresent());
        assertTrue(all.message().get().contains("familyfeud.admin"));
    }

    @Test
    void firstMessagePrefersFirstNonEmptyRequirementMessage() {
        Requirement host = new Requirement() {
            @Override
            public boolean test(CommandSender sender) {
                return false;
            }

            @Override
            public Optional<String> message() {
                return Optional.empty();
            }
        };
        Requirement admin = Requirements.permission("familyfeud.admin");

        Requirement any = Requirements.anyOf(host, admin);

        assertTrue(any.message().isPresent());
        assertTrue(any.message().get().contains("familyfeud.admin"));
    }
}
