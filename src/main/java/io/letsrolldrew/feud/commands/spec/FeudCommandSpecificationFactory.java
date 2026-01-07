package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// builds the command specification tree for /feud

public final class FeudCommandSpecificationFactory {

    public CommandSpecificationNode buildBaseSpecification(
            SpecExecutor rootExecutor, SpecExecutor helpExecutor, SpecExecutor versionExecutor) {
        SpecExecutor safeRootExecutor = Objects.requireNonNull(rootExecutor, "rootExecutor");
        SpecExecutor safeHelpExecutor = Objects.requireNonNull(helpExecutor, "helpExecutor");
        SpecExecutor safeVersionExecutor = Objects.requireNonNull(versionExecutor, "versionExecutor");

        CommandSpecificationNode helpNode = CommandSpecificationNode.builder(ArgType.LITERAL, "help")
                .executor(safeHelpExecutor)
                .build();

        CommandSpecificationNode versionNode = CommandSpecificationNode.builder(ArgType.LITERAL, "version")
                .executor(safeVersionExecutor)
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .executor(safeRootExecutor)
                .child(helpNode)
                .child(versionNode)
                .build();
    }

    // builds the /feud ui ... commands

    public CommandSpecificationNode buildUiSpecification(
            String hostPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor uiExecutor,
            SpecExecutor revealExecutor,
            SpecExecutor strikeExecutor,
            SpecExecutor clearStrikesExecutor,
            SpecExecutor addExecutor,
            SpecExecutor controlExecutor,
            SpecExecutor awardExecutor,
            SpecExecutor resetExecutor,
            SpecExecutor clickExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode reveal = CommandSpecificationNode.builder(ArgType.LITERAL, "reveal")
                .child(CommandSpecificationNode.builder(ArgType.INT, "slot")
                        .executor(Objects.requireNonNull(revealExecutor, "revealExecutor"))
                        .build())
                .executor(Objects.requireNonNull(revealExecutor, "revealExecutor"))
                .build();

        CommandSpecificationNode strike = CommandSpecificationNode.builder(ArgType.LITERAL, "strike")
                .executor(Objects.requireNonNull(strikeExecutor, "strikeExecutor"))
                .build();

        CommandSpecificationNode clearStrikes = CommandSpecificationNode.builder(ArgType.LITERAL, "clearstrikes")
                .executor(Objects.requireNonNull(clearStrikesExecutor, "clearStrikesExecutor"))
                .build();

        CommandSpecificationNode add = CommandSpecificationNode.builder(ArgType.LITERAL, "add")
                .child(CommandSpecificationNode.builder(ArgType.INT, "points")
                        .executor(Objects.requireNonNull(addExecutor, "addExecutor"))
                        .build())
                .executor(Objects.requireNonNull(addExecutor, "addExecutor"))
                .build();

        CommandSpecificationNode control = CommandSpecificationNode.builder(ArgType.LITERAL, "control")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                        .executor(Objects.requireNonNull(controlExecutor, "controlExecutor"))
                        .build())
                .executor(Objects.requireNonNull(controlExecutor, "controlExecutor"))
                .build();

        CommandSpecificationNode award = CommandSpecificationNode.builder(ArgType.LITERAL, "award")
                .executor(Objects.requireNonNull(awardExecutor, "awardExecutor"))
                .build();

        CommandSpecificationNode reset = CommandSpecificationNode.builder(ArgType.LITERAL, "reset")
                .executor(Objects.requireNonNull(resetExecutor, "resetExecutor"))
                .build();

        CommandSpecificationNode click = CommandSpecificationNode.builder(ArgType.LITERAL, "click")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "page")
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "action")
                                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "actionId")
                                        .executor(Objects.requireNonNull(clickExecutor, "clickExecutor"))
                                        .build())
                                .build())
                        .build())
                .requirements(List.of(Requirements.playerOnly()))
                .executor(Objects.requireNonNull(clickExecutor, "clickExecutor"))
                .build();

        CommandSpecificationNode uiRoot = CommandSpecificationNode.builder(ArgType.LITERAL, "ui")
                .requirements(List.of(Requirements.permission(hostPermission)))
                .executor(Objects.requireNonNull(uiExecutor, "uiExecutor"))
                .children(List.of(reveal, strike, clearStrikes, add, control, award, reset, click))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithUi(base.children(), uiRoot))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithUi(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode uiRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(uiRoot);
        return children;
    }
}
