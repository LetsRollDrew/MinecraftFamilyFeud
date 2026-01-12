package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;

// walks a CommandSpecificationNode tree against Bukkit args
// only matches, enforces requirements, and returns the match outcome
// so callers can route to module handlers as needed

public final class SpecificationDispatcher {
    private final CommandSpecificationNode root;

    public SpecificationDispatcher(CommandSpecificationNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root");
        }
        this.root = root;
    }

    // returns result describing whether we matched a node, enforced requirements, and the remaining args for callers

    public DispatchResult dispatch(CommandSender sender, String label, String[] args) {
        List<String> tokens = args == null ? List.of() : List.of(args);

        WalkState state = new WalkState(sender, tokens);

        CommandSpecificationNode matched = walk(state, root, 0);
        if (state.requirementFailed) {
            return DispatchResult.handledRequirement();
        }

        if (matched == null) {
            return DispatchResult.noMatch();
        }

        List<String> remaining = new ArrayList<>();
        if (state.nextIndex < tokens.size()) {
            remaining.addAll(tokens.subList(state.nextIndex, tokens.size()));
        }

        return new DispatchResult(true, false, matched, remaining);
    }

    private CommandSpecificationNode walk(WalkState state, CommandSpecificationNode node, int index) {
        if (!requirementsPass(state.sender, node.requirements())) {
            state.requirementFailed = true;
            return null;
        }

        if (index >= state.tokens.size()) {
            return node.executes() ? node : null;
        }

        String token = state.tokens.get(index);
        for (CommandSpecificationNode child : node.children()) {
            switch (child.type()) {
                case LITERAL -> { // token "ui" in /feud ui strike
                    if (token.equalsIgnoreCase(child.name())) {
                        state.nextIndex = index + 1;

                        CommandSpecificationNode found = walk(state, child, index + 1);
                        if (found != null) {
                            return found;
                        }
                    }
                }
                case WORD -> { // token "red" in /feud team set red name ...
                    state.nextIndex = index + 1;

                    CommandSpecificationNode found = walk(state, child, index + 1);
                    if (found != null) {
                        return found;
                    }
                }
                case INT -> { // token "3" in /feud ui reveal 3
                    if (isInt(token)) {
                        state.nextIndex = index + 1;

                        CommandSpecificationNode found = walk(state, child, index + 1);
                        if (found != null) {
                            return found;
                        }
                    }
                }
                case GREEDY -> { // tokens "abc def" in /feud fastmoney answer abc def
                    state.nextIndex = state.tokens.size();

                    List<String> greedyTokens = greedyParts(joinFrom(state.tokens, index), child.greedyToken());
                    state.capturedGreedy = greedyTokens;

                    CommandSpecificationNode found = walk(state, child, state.tokens.size());
                    if (found != null) {
                        return found;
                    }
                }
                default -> {
                    // just ignore
                }
            }
        }
        return null;
    }

    private boolean requirementsPass(CommandSender sender, List<Requirement> requirements) {
        for (Requirement requirement : requirements) {
            if (!requirement.test(sender)) {
                if (sender != null) {
                    String msg = requirement.message().orElse("You cannot use this command.");
                    sender.sendMessage(msg);
                }
                return false;
            }
        }
        return true;
    }

    private static boolean isInt(String token) {
        try {
            Integer.parseInt(token);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static String joinFrom(List<String> tokens, int start) {
        if (start >= tokens.size()) {
            return "";
        }
        return String.join(" ", tokens.subList(start, tokens.size()));
    }

    private static List<String> greedyParts(String raw, GreedyToken policy) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        if (policy == GreedyToken.RAW_SINGLE_ARG) {
            return List.of(raw);
        }
        String[] parts = raw.split(" ");
        List<String> filtered = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty()) {
                filtered.add(part);
            }
        }
        return filtered;
    }

    private static final class WalkState {
        private final CommandSender sender;
        private final List<String> tokens;
        private int nextIndex;
        private boolean requirementFailed;

        @SuppressWarnings("unused")
        private List<String> capturedGreedy = List.of();

        private WalkState(CommandSender sender, List<String> tokens) {
            this.sender = sender;
            this.tokens = tokens;
        }
    }

    public static final class DispatchResult {
        private final boolean matched;
        private final boolean handledRequirement;
        private final CommandSpecificationNode node;
        private final List<String> remainingArgs;

        private DispatchResult(
                boolean matched,
                boolean handledRequirement,
                CommandSpecificationNode node,
                List<String> remainingArgs) {
            this.matched = matched;
            this.handledRequirement = handledRequirement;
            this.node = node;
            this.remainingArgs = remainingArgs == null ? List.of() : List.copyOf(remainingArgs);
        }

        public static DispatchResult handledRequirement() {
            return new DispatchResult(false, true, null, List.of());
        }

        public static DispatchResult noMatch() {
            return new DispatchResult(false, false, null, List.of());
        }

        public DispatchResult(boolean matched, boolean handledRequirement, CommandSpecificationNode node) {
            this(matched, handledRequirement, node, List.of());
        }

        public boolean matched() {
            return matched;
        }

        public boolean requirementHandled() {
            return handledRequirement;
        }

        public CommandSpecificationNode node() {
            return node;
        }

        public List<String> remainingArgs() {
            return remainingArgs;
        }
    }
}
