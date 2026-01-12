package io.letsrolldrew.feud.commands.spec;

import java.util.Optional;
import org.bukkit.command.CommandSender;

// consists of a predicate that must pass for a command to be executed

public interface Requirement {
    boolean test(CommandSender sender);

    Optional<String> message();
}
