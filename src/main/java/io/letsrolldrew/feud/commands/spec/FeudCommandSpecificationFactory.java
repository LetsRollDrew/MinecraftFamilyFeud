package io.letsrolldrew.feud.commands.spec;

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
}
