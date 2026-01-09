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

    // builds the /feud holo ... commands

    public CommandSpecificationNode buildHoloSpecification(
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor holoExecutor,
            SpecExecutor textExecutor,
            SpecExecutor itemExecutor,
            SpecExecutor listExecutor) {
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode textNode = CommandSpecificationNode.builder(ArgType.LITERAL, "text")
                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "rest")
                        .executor(Objects.requireNonNull(textExecutor, "textExecutor"))
                        .build())
                .executor(Objects.requireNonNull(textExecutor, "textExecutor"))
                .build();

        CommandSpecificationNode itemNode = CommandSpecificationNode.builder(ArgType.LITERAL, "item")
                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "rest")
                        .executor(Objects.requireNonNull(itemExecutor, "itemExecutor"))
                        .build())
                .executor(Objects.requireNonNull(itemExecutor, "itemExecutor"))
                .build();

        CommandSpecificationNode listNode = CommandSpecificationNode.builder(ArgType.LITERAL, "list")
                .executor(Objects.requireNonNull(listExecutor, "listExecutor"))
                .build();

        CommandSpecificationNode holoRoot = CommandSpecificationNode.builder(ArgType.LITERAL, "holo")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .executor(Objects.requireNonNull(holoExecutor, "holoExecutor"))
                .children(List.of(textNode, itemNode, listNode))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithHolo(base.children(), holoRoot))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithHolo(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode holoRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(holoRoot);
        return children;
    }

    // builds the /feud board map ... commands

    public CommandSpecificationNode buildBoardMapSpecification(
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor mapExecutor,
            SpecExecutor wandExecutor,
            SpecExecutor initMapsExecutor) {
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode wandNode = CommandSpecificationNode.builder(ArgType.LITERAL, "wand")
                .requirements(List.of(Requirements.playerOnly()))
                .executor(Objects.requireNonNull(wandExecutor, "wandExecutor"))
                .build();

        CommandSpecificationNode initMapsNode = CommandSpecificationNode.builder(ArgType.LITERAL, "initmaps")
                .executor(Objects.requireNonNull(initMapsExecutor, "initMapsExecutor"))
                .build();

        CommandSpecificationNode mapRoot = CommandSpecificationNode.builder(ArgType.LITERAL, "map")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .executor(Objects.requireNonNull(mapExecutor, "mapExecutor"))
                .children(List.of(wandNode, initMapsNode))
                .build();

        CommandSpecificationNode boardRoot = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .executor(mapExecutor)
                .child(mapRoot)
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithBoard(base.children(), boardRoot))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithBoard(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode boardRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(boardRoot);
        return children;
    }

    // builds the /feud board display remote ... commands

    public CommandSpecificationNode buildBoardDisplayRemoteSpecification(
            String hostPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor boardExecutor,
            SpecExecutor displayExecutor,
            SpecExecutor remoteExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode remoteGreedy = CommandSpecificationNode.builder(ArgType.GREEDY, "rest")
                .executor(Objects.requireNonNull(remoteExecutor, "remoteExecutor"))
                .build();

        CommandSpecificationNode remote = CommandSpecificationNode.builder(ArgType.LITERAL, "remote")
                .requirements(List.of(Requirements.permission(hostPermission), Requirements.playerOnly()))
                .executor(Objects.requireNonNull(remoteExecutor, "remoteExecutor"))
                .child(remoteGreedy)
                .build();

        CommandSpecificationNode display = CommandSpecificationNode.builder(ArgType.LITERAL, "display")
                .executor(Objects.requireNonNull(displayExecutor, "displayExecutor"))
                .child(remote)
                .build();

        CommandSpecificationNode board = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .child(display)
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithBoard(base.children(), board))
                .build();
    }

    // builds the /feud board display ... admin commands

    public CommandSpecificationNode buildBoardDisplayAdminSpecification(
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor boardExecutor,
            SpecExecutor displayExecutor) {
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode adminGreedy = CommandSpecificationNode.builder(ArgType.GREEDY, "rest")
                .executor(Objects.requireNonNull(displayExecutor, "displayExecutor"))
                .build();

        CommandSpecificationNode display = CommandSpecificationNode.builder(ArgType.LITERAL, "display")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .executor(Objects.requireNonNull(displayExecutor, "displayExecutor"))
                .child(adminGreedy)
                .build();

        CommandSpecificationNode board = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .child(display)
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithBoard(base.children(), board))
                .build();
    }

    // builds the /feud team ... commands

    public CommandSpecificationNode buildTeamSpecification(
            String hostPermission,
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor teamExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode teamGreedy = CommandSpecificationNode.builder(ArgType.GREEDY, "rest")
                .executor(Objects.requireNonNull(teamExecutor, "teamExecutor"))
                .build();

        CommandSpecificationNode team = CommandSpecificationNode.builder(ArgType.LITERAL, "team")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .executor(Objects.requireNonNull(teamExecutor, "teamExecutor"))
                .child(teamGreedy)
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithTeam(base.children(), team))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithTeam(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode teamRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(teamRoot);
        return children;
    }

    // builds the /feud timer ... commands

    public CommandSpecificationNode buildTimerSpecification(
            String hostPermission,
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor timerExecutor,
            SpecExecutor startExecutor,
            SpecExecutor stopExecutor,
            SpecExecutor resetExecutor,
            SpecExecutor statusExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode startSeconds = CommandSpecificationNode.builder(ArgType.INT, "seconds")
                .executor(Objects.requireNonNull(startExecutor, "startExecutor"))
                .build();

        CommandSpecificationNode start = CommandSpecificationNode.builder(ArgType.LITERAL, "start")
                .child(startSeconds)
                .executor(Objects.requireNonNull(startExecutor, "startExecutor"))
                .build();

        CommandSpecificationNode stop = CommandSpecificationNode.builder(ArgType.LITERAL, "stop")
                .executor(Objects.requireNonNull(stopExecutor, "stopExecutor"))
                .build();

        CommandSpecificationNode resetSeconds = CommandSpecificationNode.builder(ArgType.INT, "seconds")
                .executor(Objects.requireNonNull(resetExecutor, "resetExecutor"))
                .build();

        CommandSpecificationNode reset = CommandSpecificationNode.builder(ArgType.LITERAL, "reset")
                .child(resetSeconds)
                .executor(Objects.requireNonNull(resetExecutor, "resetExecutor"))
                .build();

        CommandSpecificationNode status = CommandSpecificationNode.builder(ArgType.LITERAL, "status")
                .executor(Objects.requireNonNull(statusExecutor, "statusExecutor"))
                .build();

        CommandSpecificationNode timer = CommandSpecificationNode.builder(ArgType.LITERAL, "timer")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .executor(Objects.requireNonNull(timerExecutor, "timerExecutor"))
                .children(List.of(start, stop, reset, status))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithTimer(base.children(), timer))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithTimer(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode timerRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(timerRoot);
        return children;
    }

    // builds the /feud fastmoney ... commands

    public CommandSpecificationNode buildFastMoneySpecification(
            String hostPermission,
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor fastMoneyExecutor,
            SpecExecutor setExecutor,
            SpecExecutor startExecutor,
            SpecExecutor stopExecutor,
            SpecExecutor statusExecutor,
            SpecExecutor bindExecutor,
            SpecExecutor answerExecutor,
            SpecExecutor boardExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode set = CommandSpecificationNode.builder(ArgType.LITERAL, "set")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "setId")
                        .executor(Objects.requireNonNull(setExecutor, "setExecutor"))
                        .build())
                .executor(Objects.requireNonNull(setExecutor, "setExecutor"))
                .build();

        CommandSpecificationNode startBoard = CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                .executor(Objects.requireNonNull(startExecutor, "startExecutor"))
                .build();
        CommandSpecificationNode start = CommandSpecificationNode.builder(ArgType.LITERAL, "start")
                .child(startBoard)
                .executor(Objects.requireNonNull(startExecutor, "startExecutor"))
                .build();

        CommandSpecificationNode stopBoard = CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                .executor(Objects.requireNonNull(stopExecutor, "stopExecutor"))
                .build();
        CommandSpecificationNode stop = CommandSpecificationNode.builder(ArgType.LITERAL, "stop")
                .child(stopBoard)
                .executor(Objects.requireNonNull(stopExecutor, "stopExecutor"))
                .build();

        CommandSpecificationNode status = CommandSpecificationNode.builder(ArgType.LITERAL, "status")
                .executor(Objects.requireNonNull(statusExecutor, "statusExecutor"))
                .build();

        CommandSpecificationNode bindTarget = CommandSpecificationNode.builder(ArgType.WORD, "target")
                .executor(Objects.requireNonNull(bindExecutor, "bindExecutor"))
                .build();
        CommandSpecificationNode bind = CommandSpecificationNode.builder(ArgType.LITERAL, "bind")
                .child(bindTarget)
                .executor(Objects.requireNonNull(bindExecutor, "bindExecutor"))
                .build();

        CommandSpecificationNode answerText = CommandSpecificationNode.builder(ArgType.GREEDY, "text")
                .executor(Objects.requireNonNull(answerExecutor, "answerExecutor"))
                .build();
        CommandSpecificationNode answer = CommandSpecificationNode.builder(ArgType.LITERAL, "answer")
                .requirements(List.of(Requirements.playerOnly()))
                .child(answerText)
                .executor(Objects.requireNonNull(answerExecutor, "answerExecutor"))
                .build();

        CommandSpecificationNode boardId = CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .build();
        CommandSpecificationNode boardShow = CommandSpecificationNode.builder(ArgType.LITERAL, "show")
                .child(boardId)
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .build();
        CommandSpecificationNode boardHide = CommandSpecificationNode.builder(ArgType.LITERAL, "hide")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                        .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                        .build())
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .build();
        CommandSpecificationNode board = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .executor(Objects.requireNonNull(boardExecutor, "boardExecutor"))
                .children(List.of(boardShow, boardHide))
                .build();

        CommandSpecificationNode fastmoney = CommandSpecificationNode.builder(ArgType.LITERAL, "fastmoney")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .executor(Objects.requireNonNull(fastMoneyExecutor, "fastMoneyExecutor"))
                .children(List.of(set, start, stop, status, bind, answer, board))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithFastMoney(base.children(), fastmoney))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithFastMoney(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode fastmoneyRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(fastmoneyRoot);
        return children;
    }

    // builds the /feud survey ... commands

    public CommandSpecificationNode buildSurveySpecification(
            String hostPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor surveyExecutor,
            SpecExecutor listExecutor,
            SpecExecutor loadExecutor) {
        Objects.requireNonNull(hostPermission, "hostPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode list = CommandSpecificationNode.builder(ArgType.LITERAL, "list")
                .executor(Objects.requireNonNull(listExecutor, "listExecutor"))
                .build();

        CommandSpecificationNode loadId = CommandSpecificationNode.builder(ArgType.WORD, "id")
                .executor(Objects.requireNonNull(loadExecutor, "loadExecutor"))
                .build();

        CommandSpecificationNode load = CommandSpecificationNode.builder(ArgType.LITERAL, "load")
                .requirements(List.of(Requirements.permission(hostPermission)))
                .child(loadId)
                .executor(Objects.requireNonNull(loadExecutor, "loadExecutor"))
                .build();

        CommandSpecificationNode survey = CommandSpecificationNode.builder(ArgType.LITERAL, "survey")
                .executor(Objects.requireNonNull(surveyExecutor, "surveyExecutor"))
                .children(List.of(list, load))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithSurvey(base.children(), survey))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithSurvey(
            List<CommandSpecificationNode> baseChildren, CommandSpecificationNode surveyRoot) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(surveyRoot);
        return children;
    }

    // builds the /feud clear & /feud buzz commands

    public CommandSpecificationNode buildClearAndBuzzSpecification(
            String adminPermission,
            SpecExecutor rootExecutor,
            SpecExecutor helpExecutor,
            SpecExecutor versionExecutor,
            SpecExecutor clearExecutor,
            SpecExecutor buzzExecutor) {
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification(rootExecutor, helpExecutor, versionExecutor);

        CommandSpecificationNode clearAll = CommandSpecificationNode.builder(ArgType.LITERAL, "all")
                .executor(Objects.requireNonNull(clearExecutor, "clearExecutor"))
                .build();

        CommandSpecificationNode clear = CommandSpecificationNode.builder(ArgType.LITERAL, "clear")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .executor(Objects.requireNonNull(clearExecutor, "clearExecutor"))
                .child(clearAll)
                .build();

        CommandSpecificationNode buzz = CommandSpecificationNode.builder(ArgType.LITERAL, "buzz")
                .executor(Objects.requireNonNull(buzzExecutor, "buzzExecutor"))
                .build();

        return CommandSpecificationNode.builder(base.type(), base.name())
                .executor(base.executor().orElse(null))
                .children(mergeWithClearAndBuzz(base.children(), clear, buzz))
                .build();
    }

    private List<CommandSpecificationNode> mergeWithClearAndBuzz(
            List<CommandSpecificationNode> baseChildren,
            CommandSpecificationNode clear,
            CommandSpecificationNode buzz) {
        List<CommandSpecificationNode> children = new ArrayList<>(baseChildren);
        children.add(clear);
        children.add(buzz);
        return children;
    }
}
