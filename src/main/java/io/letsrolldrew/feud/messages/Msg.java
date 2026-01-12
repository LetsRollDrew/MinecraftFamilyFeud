package io.letsrolldrew.feud.messages;

public enum Msg {
    HOST_ONLY("You must be the host to do that"),
    ADMIN_ONLY("Admin only"),
    PLAYER_ONLY("Only players can use this command"),
    NEED_PERMISSION("You need permission: {permission}"),

    TEAM_MUST_BE_RED_BLUE("Team must be red or blue"),
    SLOT_MUST_BE_1_8("Slot must be 1-8"),
    SECONDS_CANNOT_BE_NEGATIVE("Seconds can't be negative"),
    INVALID_ID_SIMPLE("Invalid id"),
    INVALID_ID_HOLO("Invalid id, use letters, numbers, _ or -"),

    NOT_READY("Not ready"),
    MISSING_BOARD_ID("Missing board id"),
    SURVEYS_NOT_LOADED("Surveys not loaded"),
    SURVEY_NOT_FOUND("Survey not found: {id}"),
    HOLOGRAM_NOT_FOUND("Hologram not found: {id}"),
    HOLOGRAM_ID_EXISTS("Hologram id already exists: {id}"),
    HOLOGRAM_SPAWNED("Spawned hologram '{id}'"),
    HOLOGRAM_UPDATED("Updated hologram '{id}'"),
    HOLOGRAM_MOVED_TO_YOU("Moved hologram '{id}' to your location"),
    HOLOGRAM_REMOVED("Removed hologram '{id}'"),
    FAST_MONEY_SET_NOT_FOUND("Fast Money set not found: {setId}"),
    FAST_MONEY_SET_LOADED("Fast Money set loaded: {setId}"),
    FAST_MONEY_BOARD_NO_LAYOUT("Fast Money board not spawned: no layout for {boardId}"),
    FAST_MONEY_BOARD_SHOWN("Fast Money board shown on {boardId}"),
    FAST_MONEY_BOARD_CLEARED("Fast Money board cleared on {boardId}"),
    BOARD_CREATED_AT_LOCATION("Board '{boardId}' created at your location"),
    BOARD_REMOVED("Board '{boardId}' removed"),
    DYNAMIC_BOARD_CREATED("Dynamic board '{boardId}' created"),
    DYNAMIC_BOARD_CREATED_FROM_SELECTION("Dynamic board '{boardId}' created from selection"),
    ITEM_HOLOGRAM_SPAWNED_WITH_CMD("Spawned item hologram '{id}' with CMD {cmd}"),
    ITEM_HOLOGRAM_SPAWNED_WITH_MATERIAL_CMD("Spawned item hologram '{id}' ({material}, CMD {cmd})"),
    ITEM_HOLOGRAM_MOVED_TO_YOU("Moved item hologram '{id}' to your location"),
    ITEM_HOLOGRAM_REMOVED("Removed item hologram '{id}'"),
    UNKNOWN_MATERIAL("Unknown material: {material}"),

    USAGE_CLEAR_ALL("Usage: /feud clear all"),
    USAGE_HOST_BOOK("Usage: /feud host book [map|display|cleanup]"),
    USAGE_UI_ROOT("Usage: /feud ui <reveal, strike, clearstrikes, add, control, award, reset>"),
    USAGE_UI_REVEAL("Usage: /feud ui reveal <1-8>"),
    USAGE_UI_ADD("Usage: /feud ui add <points>"),
    USAGE_UI_CONTROL("Usage: /feud ui control <red|blue>"),
    USAGE_UI_CLICK("Usage: /feud ui click <page> action <actionId>"),
    USAGE_SURVEY_LOAD("Usage: /feud survey load <id>"),
    USAGE_TEAM_SET_NAME("Usage: /feud team set <red|blue> name <new-name>"),
    USAGE_TEAM_BUZZER("Usage: /feud team buzzer <bind|clear|test> <red|blue>"),

    TIMER_HELP("Timer commands:\n"
            + "/feud timer start [seconds]\n"
            + "/feud timer stop\n"
            + "/feud timer reset [seconds]\n"
            + "/feud timer status");

    private final String template;

    Msg(String template) {
        this.template = template;
    }

    public String template() {
        return template;
    }
}
