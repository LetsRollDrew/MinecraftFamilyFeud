package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class Requirements {
    private Requirements() {}

    public static Requirement permission(String permission) {
        Objects.requireNonNull(permission, "permission");

        Predicate<CommandSender> predicate = sender -> sender != null && sender.hasPermission(permission);
        Optional<String> message = Optional.of("You need permission: " + permission);

        return new BasicRequirement(predicate, message);
    }

    public static Requirement playerOnly() {
        Predicate<CommandSender> predicate = sender -> sender instanceof Player;
        Optional<String> message = Optional.of("Must be a player to use this command.");

        return new BasicRequirement(predicate, message);
    }

    // anyOf combinator, passes when a SINGLE requirement in the group passes
    public static Requirement anyOf(Requirement... requirements) {
        List<Requirement> requirementList = copyRequirements(requirements);

        Predicate<CommandSender> predicate = sender -> {
            for (Requirement requirement : requirementList) {
                if (requirement.test(sender)) {
                    return true;
                }
            }
            return false;
        };

        return new BasicRequirement(predicate, firstMessage(requirementList));
    }

    // allOf combinator, passing only when EVERY requirement in the group passes
    public static Requirement allOf(Requirement... requirements) {
        List<Requirement> requirementList = copyRequirements(requirements);

        Predicate<CommandSender> predicate = sender -> {
            for (Requirement requirement : requirementList) {
                if (!requirement.test(sender)) {
                    return false;
                }
            }
            return true;
        };

        return new BasicRequirement(predicate, firstMessage(requirementList));
    }

    private static List<Requirement> copyRequirements(Requirement... requirements) {
        Objects.requireNonNull(requirements, "requirements");
        List<Requirement> requirementList = new ArrayList<>();
        for (Requirement requirement : requirements) {
            requirementList.add(Objects.requireNonNull(requirement, "requirement"));
        }
        return requirementList;
    }

    private static Optional<String> firstMessage(List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            Optional<String> message = requirement.message();
            if (message.isPresent()) {
                return message;
            }
        }
        return Optional.empty();
    }

    private static final class BasicRequirement implements Requirement {
        private final Predicate<CommandSender> predicate;
        private final Optional<String> message;

        private BasicRequirement(Predicate<CommandSender> predicate, Optional<String> message) {
            this.predicate = Objects.requireNonNull(predicate, "predicate");
            this.message = Objects.requireNonNull(message, "message");
        }

        @Override
        public boolean test(CommandSender sender) {
            return predicate.test(sender);
        }

        @Override
        public Optional<String> message() {
            return message;
        }
    }
}
