package io.letsrolldrew.feud.commands.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FeudCommandSpecificationFactory {

    /*********************************************************
     * Builds the minimal root with help and version literals
     *********************************************************/
    public CommandSpecificationNode buildBaseSpecification() {
        CommandSpecificationNode helpNode =
                CommandSpecificationNode.builder(ArgType.LITERAL, "help").build();
        CommandSpecificationNode versionNode =
                CommandSpecificationNode.builder(ArgType.LITERAL, "version").build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "feud")
                .child(helpNode)
                .child(versionNode)
                .build();
    }

    /******************************************************************************************************
     * builds the entire /feud command specification, merging all subcommands below
     * (ui, holo, board, survey, team, timer, fastmoney, host book, clear, buzz) into a single tree
     ******************************************************************************************************/
    public CommandSpecificationNode buildFullSpecification(String hostPermission, String adminPermission) {
        Objects.requireNonNull(hostPermission, "hostPermission");
        Objects.requireNonNull(adminPermission, "adminPermission");

        CommandSpecificationNode base = buildBaseSpecification();
        List<CommandSpecificationNode> children = new ArrayList<>(base.children());

        children.add(buildUi(hostPermission));
        children.add(buildHolo(adminPermission));
        children.add(buildBoard(hostPermission, adminPermission));
        children.add(buildSurvey(hostPermission));
        children.add(buildTeam(hostPermission, adminPermission));
        children.add(buildTimer(hostPermission, adminPermission));
        children.add(buildFastMoney(hostPermission, adminPermission));
        children.add(buildHostBook(hostPermission));
        children.add(buildClear(adminPermission));
        children.add(buildBuzz(hostPermission, adminPermission));

        return CommandSpecificationNode.builder(base.type(), base.name())
                .children(children)
                .build();
    }

    /****************************************************************
     * /feud ui ... (host-only)
     ****************************************************************/
    private CommandSpecificationNode buildUi(String hostPermission) {
        CommandSpecificationNode reveal = CommandSpecificationNode.builder(ArgType.LITERAL, "reveal")
                .child(CommandSpecificationNode.builder(ArgType.INT, "slot").build())
                .build();

        CommandSpecificationNode strike =
                CommandSpecificationNode.builder(ArgType.LITERAL, "strike").build();
        CommandSpecificationNode clearStrikes = CommandSpecificationNode.builder(ArgType.LITERAL, "clearstrikes")
                .build();
        CommandSpecificationNode add = CommandSpecificationNode.builder(ArgType.LITERAL, "add")
                .child(CommandSpecificationNode.builder(ArgType.INT, "points").build())
                .build();
        CommandSpecificationNode control = CommandSpecificationNode.builder(ArgType.LITERAL, "control")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "team").build())
                .build();
        CommandSpecificationNode award =
                CommandSpecificationNode.builder(ArgType.LITERAL, "award").build();
        CommandSpecificationNode reset =
                CommandSpecificationNode.builder(ArgType.LITERAL, "reset").build();

        CommandSpecificationNode click = CommandSpecificationNode.builder(ArgType.LITERAL, "click")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "page")
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "action")
                                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "actionId")
                                        .greedyRawSingleArg()
                                        .build())
                                .build())
                        .build())
                .requirements(List.of(Requirements.permission(hostPermission), Requirements.playerOnly()))
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "ui")
                .requirements(List.of(Requirements.permission(hostPermission)))
                .children(List.of(reveal, strike, clearStrikes, add, control, award, reset, click))
                .build();
    }

    /*******************************************************************
     * /feud holo ... (admin-only)
     *******************************************************************/
    private CommandSpecificationNode buildHolo(String adminPermission) {
        CommandSpecificationNode text = CommandSpecificationNode.builder(ArgType.LITERAL, "text")
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "spawn")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .child(CommandSpecificationNode.builder(ArgType.GREEDY, "text")
                                                .build())
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "set")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .child(CommandSpecificationNode.builder(ArgType.GREEDY, "text")
                                                .build())
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "move")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "remove")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .build())
                                .build()))
                .build();
        CommandSpecificationNode item = CommandSpecificationNode.builder(ArgType.LITERAL, "item")
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "spawn")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .child(CommandSpecificationNode.builder(ArgType.WORD, "material")
                                                .child(CommandSpecificationNode.builder(ArgType.INT, "cmd")
                                                        .build())
                                                .build())
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "move")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "remove")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "id")
                                        .build())
                                .build()))
                .build();
        CommandSpecificationNode list =
                CommandSpecificationNode.builder(ArgType.LITERAL, "list").build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "holo")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .children(List.of(text, item, list))
                .build();
    }

    /*******************************************************************************************
     * /feud board ... including map (admin) and display branches
     *******************************************************************************************/
    private CommandSpecificationNode buildBoard(String hostPermission, String adminPermission) {
        CommandSpecificationNode map = CommandSpecificationNode.builder(ArgType.LITERAL, "map")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "wand")
                                .requirements(List.of(Requirements.playerOnly()))
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "initmaps")
                                .build()))
                .build();

        CommandSpecificationNode remote = CommandSpecificationNode.builder(ArgType.LITERAL, "remote")
                .requirements(List.of(Requirements.permission(hostPermission), Requirements.playerOnly()))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                        .children(List.of(
                                CommandSpecificationNode.builder(ArgType.LITERAL, "reveal")
                                        .child(CommandSpecificationNode.builder(ArgType.INT, "slot")
                                                .build())
                                        .build(),
                                CommandSpecificationNode.builder(ArgType.LITERAL, "strike")
                                        .build(),
                                CommandSpecificationNode.builder(ArgType.LITERAL, "clearstrikes")
                                        .build(),
                                CommandSpecificationNode.builder(ArgType.LITERAL, "control")
                                        .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                                                .build())
                                        .build(),
                                CommandSpecificationNode.builder(ArgType.LITERAL, "award")
                                        .build(),
                                CommandSpecificationNode.builder(ArgType.LITERAL, "reset")
                                        .build()))
                        .build())
                .build();

        CommandSpecificationNode create = CommandSpecificationNode.builder(ArgType.LITERAL, "create")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();

        CommandSpecificationNode dynamic = CommandSpecificationNode.builder(ArgType.LITERAL, "dynamic")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();

        CommandSpecificationNode selection = CommandSpecificationNode.builder(ArgType.LITERAL, "selection")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "rest").build())
                .build();

        CommandSpecificationNode list = CommandSpecificationNode.builder(ArgType.LITERAL, "list")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .build();

        CommandSpecificationNode remove = CommandSpecificationNode.builder(ArgType.LITERAL, "remove")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();

        CommandSpecificationNode deleteAlias = CommandSpecificationNode.builder(ArgType.LITERAL, "delete")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();

        CommandSpecificationNode wand = CommandSpecificationNode.builder(ArgType.LITERAL, "wand")
                .requirements(List.of(Requirements.permission(adminPermission), Requirements.playerOnly()))
                .build();

        CommandSpecificationNode selectorAlias = CommandSpecificationNode.builder(ArgType.LITERAL, "selector")
                .requirements(List.of(Requirements.permission(adminPermission), Requirements.playerOnly()))
                .build();

        CommandSpecificationNode display = CommandSpecificationNode.builder(ArgType.LITERAL, "display")
                .children(List.of(remote, create, dynamic, selection, list, remove, deleteAlias, wand, selectorAlias))
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .children(List.of(map, display))
                .build();
    }

    /*******************************************************************
     * /feud survey ... (list open, load host)
     *******************************************************************/
    private CommandSpecificationNode buildSurvey(String hostPermission) {
        CommandSpecificationNode list =
                CommandSpecificationNode.builder(ArgType.LITERAL, "list").build();
        CommandSpecificationNode load = CommandSpecificationNode.builder(ArgType.LITERAL, "load")
                .requirements(List.of(Requirements.permission(hostPermission)))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "id").build())
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "survey")
                .children(List.of(list, load))
                .build();
    }

    /******************************************************************************************
     * /feud team ... (host/admin)
     ******************************************************************************************/
    private CommandSpecificationNode buildTeam(String hostPermission, String adminPermission) {
        CommandSpecificationNode info =
                CommandSpecificationNode.builder(ArgType.LITERAL, "info").build();
        CommandSpecificationNode reset =
                CommandSpecificationNode.builder(ArgType.LITERAL, "reset").build();
        CommandSpecificationNode set = CommandSpecificationNode.builder(ArgType.LITERAL, "set")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                        .child(CommandSpecificationNode.builder(ArgType.LITERAL, "name")
                                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "newName")
                                        .build())
                                .build())
                        .build())
                .build();
        CommandSpecificationNode buzzer = CommandSpecificationNode.builder(ArgType.LITERAL, "buzzer")
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "bind")
                                .requirements(List.of(Requirements.playerOnly()))
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "clear")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "test")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "team")
                                        .build())
                                .build()))
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "team")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .children(List.of(info, reset, set, buzzer))
                .build();
    }

    /*******************************************************************************************
     * /feud timer ... (host/admin)
     *******************************************************************************************/
    private CommandSpecificationNode buildTimer(String hostPermission, String adminPermission) {
        CommandSpecificationNode startSeconds =
                CommandSpecificationNode.builder(ArgType.INT, "seconds").build();
        CommandSpecificationNode start = CommandSpecificationNode.builder(ArgType.LITERAL, "start")
                .child(startSeconds)
                .build();
        CommandSpecificationNode stop =
                CommandSpecificationNode.builder(ArgType.LITERAL, "stop").build();
        CommandSpecificationNode resetSeconds =
                CommandSpecificationNode.builder(ArgType.INT, "seconds").build();
        CommandSpecificationNode reset = CommandSpecificationNode.builder(ArgType.LITERAL, "reset")
                .child(resetSeconds)
                .build();
        CommandSpecificationNode status =
                CommandSpecificationNode.builder(ArgType.LITERAL, "status").build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "timer")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .children(List.of(start, stop, reset, status))
                .build();
    }

    /***********************************************************************************************
     * /feud fastmoney ... (host/admin)
     ***********************************************************************************************/
    private CommandSpecificationNode buildFastMoney(String hostPermission, String adminPermission) {
        CommandSpecificationNode set = CommandSpecificationNode.builder(ArgType.LITERAL, "set")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "setId").build())
                .build();
        CommandSpecificationNode start = CommandSpecificationNode.builder(ArgType.LITERAL, "start")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();
        CommandSpecificationNode stop = CommandSpecificationNode.builder(ArgType.LITERAL, "stop")
                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId").build())
                .build();
        CommandSpecificationNode status =
                CommandSpecificationNode.builder(ArgType.LITERAL, "status").build();
        CommandSpecificationNode bind = CommandSpecificationNode.builder(ArgType.LITERAL, "bind")
                .requirements(List.of(Requirements.playerOnly()))
                .child(CommandSpecificationNode.builder(ArgType.WORD, "target").build())
                .build();
        CommandSpecificationNode answer = CommandSpecificationNode.builder(ArgType.LITERAL, "answer")
                .requirements(List.of(Requirements.playerOnly()))
                .child(CommandSpecificationNode.builder(ArgType.GREEDY, "text").build())
                .build();

        CommandSpecificationNode board = CommandSpecificationNode.builder(ArgType.LITERAL, "board")
                .children(List.of(
                        CommandSpecificationNode.builder(ArgType.LITERAL, "show")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                                        .noExec()
                                        .build())
                                .build(),
                        CommandSpecificationNode.builder(ArgType.LITERAL, "hide")
                                .child(CommandSpecificationNode.builder(ArgType.WORD, "boardId")
                                        .noExec()
                                        .build())
                                .build()))
                .build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "fastmoney")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .children(List.of(set, start, stop, status, bind, answer, board))
                .build();
    }

    /**********************************************************************
     * /feud host book ... (hosts)
     **********************************************************************/
    private CommandSpecificationNode buildHostBook(String hostPermission) {
        CommandSpecificationNode map =
                CommandSpecificationNode.builder(ArgType.LITERAL, "map").build();
        CommandSpecificationNode display =
                CommandSpecificationNode.builder(ArgType.LITERAL, "display").build();
        CommandSpecificationNode cleanup =
                CommandSpecificationNode.builder(ArgType.LITERAL, "cleanup").build();

        return CommandSpecificationNode.builder(ArgType.LITERAL, "host")
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "book")
                        .requirements(List.of(Requirements.permission(hostPermission), Requirements.playerOnly()))
                        .children(List.of(map, display, cleanup))
                        .build())
                .build();
    }

    /*******************************************************************
     * /feud clear ... (admin)
     *******************************************************************/
    private CommandSpecificationNode buildClear(String adminPermission) {
        return CommandSpecificationNode.builder(ArgType.LITERAL, "clear")
                .requirements(List.of(Requirements.permission(adminPermission)))
                .child(CommandSpecificationNode.builder(ArgType.LITERAL, "all").build())
                .build();
    }

    /******************************************************************************************
     * /feud buzz ... (host/admin)
     ******************************************************************************************/
    private CommandSpecificationNode buildBuzz(String hostPermission, String adminPermission) {
        return CommandSpecificationNode.builder(ArgType.LITERAL, "buzz")
                .requirements(List.of(Requirements.anyOf(
                        Requirements.permission(hostPermission), Requirements.permission(adminPermission))))
                .build();
    }
}
