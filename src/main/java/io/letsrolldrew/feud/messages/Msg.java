package io.letsrolldrew.feud.messages;

public enum Msg {
    // permissions / restrictions
    HOST_ONLY("You must be the host to do that"),
    ADMIN_ONLY("Admin only"),
    PLAYER_ONLY("Only players can use this command"),
    NEED_PERMISSION("You need permission: {permission}"),

    // validation / argument parsing
    TEAM_MUST_BE_RED_BLUE("Team must be red or blue"),
    SLOT_MUST_BE_1_8("Slot must be 1-8"),
    SECONDS_CANNOT_BE_NEGATIVE("Seconds can't be negative"),
    INVALID_ID_SIMPLE("Invalid id"),
    INVALID_ID_HOLO("Invalid id, use letters, numbers, _ or -"),

    // not ready / missing / not found
    NOT_READY("Not ready"),
    MISSING_BOARD_ID("Missing board id"),
    SURVEYS_NOT_LOADED("Surveys not loaded"),
    SURVEY_NOT_FOUND("Survey not found: {id}"),
    HOLOGRAM_NOT_FOUND("Hologram not found: {id}"),
    HOLOGRAM_ID_EXISTS("Hologram id already exists: {id}"),
    FAST_MONEY_SET_NOT_FOUND("Fast Money set not found: {setId}"),
    FAST_MONEY_BOARD_NO_LAYOUT("Fast Money board not spawned: no layout for {boardId}"),
    UNKNOWN_MATERIAL("Unknown material: {material}"),

    // generic fallbacks
    REQUIREMENT_DENIED("You cannot use this command"),

    // success confirmations templates
    HOLOGRAM_SPAWNED("Spawned hologram '{id}'"),
    HOLOGRAM_UPDATED("Updated hologram '{id}'"),
    HOLOGRAM_MOVED_TO_YOU("Moved hologram '{id}' to your location"),
    HOLOGRAM_REMOVED("Removed hologram '{id}'"),
    FAST_MONEY_SET_LOADED("Fast Money set loaded: {setId}"),
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

    // ui / round control (chat confirmations)
    UI_REVEALED_SLOT("Revealed slot {slot}"),
    UI_STRIKE_RECORDED("Strike recorded ({count}/{max})"),
    UI_STRIKES_CLEARED("Strikes cleared"),
    UI_POINTS_MUST_BE_POSITIVE("Points must be positive"),
    UI_ADDED_POINTS("Added {points} points, round total: {total}"),
    UI_CONTROL_SET("Control set to {team}"),
    UI_CONTROL_REQUIRED_TO_AWARD("Set a team in control before awarding points"),
    UI_AWARDED_POINTS("Awarded {points} points to {team}"),
    UI_ROUND_RESET("Round state reset"),

    // timer (chat confirmations / status)
    TIMER_STARTED("Timer started"),
    TIMER_STARTED_FOR("Timer started for {seconds}s"),
    TIMER_STOPPED("Timer stopped"),
    TIMER_RESET_DEFAULT("Timer reset to default"),
    TIMER_RESET_TO("Timer reset to {seconds}s"),
    TIMER_STATUS("Timer {state} ({seconds}s remaining)"),

    // team (chat confirmations / status)
    TEAM_INFO_HEADER("Teams"),
    TEAM_INFO_LINE("{team}: {name} | score={score} | buzzer={buzzer}"),
    TEAMS_RESET("Teams reset"),
    TEAM_NAME_MUST_BE_NON_BLANK("Name must be non-blank"),
    TEAM_NAME_UNCHANGED("Name unchanged"),
    TEAM_NAME_SET("Team {team} name set to '{name}'"),
    BUZZER_COMMANDS_NOT_AVAILABLE("Buzzer commands are not available"),

    // buzzer (chat confirmations / status)
    BUZZ_LOCK_RESET("Buzz lock reset"),
    BUZZER_BIND_PROMPT("Binding buzzer for {team}, right-click a block to bind"),
    BUZZER_CLEARED("Cleared buzzer for {team}"),
    BUZZER_NOT_BOUND("No buzzer bound for {team}"),
    BUZZER_LOCATION("Buzzer for {team} at {x},{y},{z}"),

    // survey (chat confirmations / status)
    SURVEY_LIST_EMPTY("No surveys loaded"),
    SURVEY_LIST_HEADER("Loaded surveys"),
    SURVEY_LIST_ENTRY("- {id}: {question}"),
    SURVEY_LOADED("Loaded survey: {id}"),

    // fast money (chat confirmations / status)
    FAST_MONEY_QUESTION_AND_SLOT_MUST_BE_POSITIVE("Question and slot must be positive"),
    FAST_MONEY_AWARDED_P1("Awarded Player 1, question {question}, slot {slot}"),
    FAST_MONEY_AWARDED_P2("Awarded Player 2, question {question}, slot {slot}"),
    FAST_MONEY_ROUND_NOT_ACTIVE("Fast Money round is not active"),
    FAST_MONEY_P1_TURN_STARTED("Fast Money: Player 1 turn started"),
    FAST_MONEY_P2_TURN_STARTED("Fast Money: Player 2 turn started"),
    FAST_MONEY_ADVANCED_TO_QUESTION("Fast Money: advanced to question {question}"),
    FAST_MONEY_COMPLETE("Fast Money complete: total={total} target={target}"),
    FAST_MONEY_STOPPED("Fast Money stopped"),
    FAST_MONEY_STATUS("Fast Money status: phase={phase} set={set} q={question} p1={p1} p2={p2} total={total} target={target}"),
    FAST_MONEY_BINDINGS_CLEARED("Fast Money bindings cleared"),
    FAST_MONEY_BIND_P1_ARMED("Fast Money: bind P1 armed, right-click a player"),
    FAST_MONEY_BIND_P2_ARMED("Fast Money: bind P2 armed, right-click a player"),
    FAST_MONEY_ANSWER_RECORDED("Answer recorded for question {question}"),
    FAST_MONEY_SLOT_NOT_DEFINED("Fast Money question {question} has no survey answer in slot {slot}"),

    // board display (remote + selection spawn) confirmations / status
    BOARD_LIST_EMPTY("No boards active"),
    BOARD_LIST_LINE("Boards: {boards}"),
    DISPLAY_REMOTE_REFRESHED("Remote refreshed"),
    DISPLAY_REMOTE_HELP("Remote: reveal/strike/clearstrikes/control/award/reset"),
    DISPLAY_REMOTE_STRIKE("Strike {count}/{max}"),
    DISPLAY_REMOTE_REVEALED("Revealed {slot}"),
    DISPLAY_REMOTE_CONTROL("Control {team}"),
    DISPLAY_REMOTE_AWARDED("Awarded {points}"),
    DISPLAY_REMOTE_RESET("Reset"),
    SELECTION_INVALID_REASON("Selection invalid: {reason}"),
    BOARD_CREATE_FAILED_OR_EXISTS("Board id already exists or creation failed"),
    SCORE_PANELS_NOT_AVAILABLE("Score panels are not available"),
    TIMER_PANEL_NOT_AVAILABLE("Timer panel is not available"),
    PANELS_REMOVED_FOR_BOARD("Removed {target} for '{boardId}'"),
    PANELS_SPAWNED_FOR_BOARD("Spawned {target} for '{boardId}' using selection"),
    TIMER_PANEL_REMOVED_FOR_BOARD("Removed timer panel for '{boardId}'"),
    TIMER_PANEL_SPAWNED_FOR_BOARD("Timer panel spawned for '{boardId}' using selection"),
    UNKNOWN_SELECTION_TARGET("Unknown selection target, use board/panels/timer"),
    SELECTOR_WAND_NOT_AVAILABLE("Selector wand is not available"),
    DISPLAY_SELECTOR_GIVEN("Display selector given"),

    // host book / selector misc
    UNKNOWN_UI_ACTION("Unknown UI action: {actionId}"),
    SELECTION_SPAWN_NOT_WIRED("Selection spawn actions are not yet wired to commands"),
    INVALID_FAST_MONEY_REVEAL_ACTION("Invalid Fast Money reveal action: {actionId}"),
    INVALID_FAST_MONEY_INDICES_ACTION("Invalid Fast Money indices in action: {actionId}"),
    DISPLAY_SELECTION_NONE("No active display selection"),
    DISPLAY_SELECTION_INFO_UNAVAILABLE("No selection information available"),
    DISPLAY_SELECTION_STATUS("Selection: {cornerA} to {cornerB} facing {facing}"),
    HOST_REMOTE_SELECTOR_GIVEN("Host remote selector given"),
    DISPLAY_REMOTE_GIVEN_EMPTY("Display remote (no boards yet)"),
    DISPLAY_REMOTE_GIVEN("Display remote: {boardId}"),
    CLEANUP_BOOK_GIVEN("Cleanup book given"),
    MAP_BOARD_REMOTE_GIVEN("Map board remote given"),

    // holograms (list output / validation)
    CUSTOM_MODEL_DATA_MUST_BE_NUMBER("CustomModelData must be a number"),
    HOLOGRAM_LIST_EMPTY("No holograms are active"),
    HOLOGRAM_LIST_HEADER("Holograms"),
    HOLOGRAM_LIST_ENTRY("- {id} ({type})"),
    HOLOGRAM_LIST_TOTALS("Totals: text={text} item={item}"),

    // clear all
    CLEARED_DISPLAYS("Cleared {count} displays"),

    // version
    VERSION_OUTPUT("FamilyFeud v{version} - game state: not started\nUse /feud help for commands"),

    // help blocks
    ROOT_HELP("FamilyFeud commands:\n"
            + "/feud - show version\n"
            + "/feud help - this help\n"
            + "/feud version - show version\n"
            + "/feud survey ...\n"
            + "/feud team info - show teams\n"
            + "/feud team reset - reset teams\n"
            + "/feud team set <red|blue> name <new-name...>\n"
            + "/feud team buzzer bind|clear|test <red|blue>\n"
            + "/feud buzz reset\n"
            + "/feud host book - give host remote\n"
            + "/feud ui reveal <1-8> - reveal slot\n"
            + "/feud ui strike - add a strike\n"
            + "/feud ui clearstrikes - clear strikes\n"
            + "/feud ui add <points> - add points to round\n"
            + "/feud board wand - get Display Selector (admin)\n"
            + "/feud board initmaps - assign maps to board frames (admin)\n"
            + "/feud holo text spawn|set|move|remove ...\n"
            + "/feud holo item spawn|move|remove ...\n"
            + "/feud holo list\n"
            + "/feud clear all - remove all display entities\n"
            + "/feud host book cleanup - cleanup remote\n"
            + "/feud timer start|stop|reset|status"),

    BOARD_ENTRY_HELP("Board commands: map | display ..."),
    BOARD_MAP_HELP("Board map commands: /feud board map wand | /feud board map initmaps"),
    BOARD_DISPLAY_HELP("Board commands: create/dynamic/list/remove/wand (selector)"),

    TEAM_HELP("Team commands:\n"
            + "/feud team info\n"
            + "/feud team reset\n"
            + "/feud team set <red|blue> name <new-name>\n"
            + "/feud team buzzer bind|clear|test <red|blue>"),

    SURVEY_HELP("Survey commands:\n" + "/feud survey list\n" + "/feud survey load <id>"),

    HOLO_HELP("Usage: /feud holo text <spawn|set|move|remove> ...\n"
            + "       /feud holo item <spawn|move|remove> ...\n"
            + "       /feud holo list"),
    HOLO_TEXT_USAGE("Usage: /feud holo text spawn <id> <text> | set <id> <text> | move <id> | remove <id>"),
    HOLO_TEXT_SPAWN_USAGE("Usage: /feud holo text spawn <id> <text>"),
    HOLO_TEXT_SET_USAGE("Usage: /feud holo text set <id> <text>"),
    HOLO_TEXT_MOVE_USAGE("Usage: /feud holo text move <id>"),
    HOLO_TEXT_REMOVE_USAGE("Usage: /feud holo text remove <id>"),
    HOLO_ITEM_USAGE("Usage: /feud holo item spawn <id> [material] <customModelData> | move <id> | remove <id>"),

    FAST_MONEY_HELP("Fast Money: set|start|stop|status|next|bind|answer|board"),

    // listener / gameplay feedback
    BOARD_WAND_TOP_LEFT_SET("Top-left set, now click the bottom-right frame"),
    BOARD_WAND_SELECTION_INVALID("Selection invalid, ensure a 10x6 rectangle on the same wall (consistent facing)"),
    BOARD_WAND_BINDING_SAVED("Board binding saved ({width}x{height})"),
    BOARD_WAND_SAVE_ERROR("Error saving board binding, see console"),

    DISPLAY_SELECTOR_FACE_PROMPT("Select a vertical face (north/east/south/west)"),
    DISPLAY_SELECTION_CORNER_1_SET("Corner 1 set, click opposite corner on the same face"),
    DISPLAY_SELECTION_FAILED_SAME_WORLD("Selection failed: corners must be in the same world"),
    DISPLAY_SELECTION_FAILED_SAME_FACE("Selection failed: corners must be on the same face"),
    DISPLAY_SELECTION_FAILED_Z_MISMATCH("Selection failed: not on the same wall (z mismatch)"),
    DISPLAY_SELECTION_FAILED_X_MISMATCH("Selection failed: not on the same wall (x mismatch)"),
    DISPLAY_SELECTION_FAILED_REASON("Selection failed: {reason}"),
    DISPLAY_SELECTION_SAVED("Display selection saved: width={width} height={height} facing={facing}"),

    BUZZER_BOUND("Buzzer bound"),
    BUZZ_ACCEPTED("Buzz accepted: {team}"),

    FAST_MONEY_BOUND("Fast Money: bound {player}"),

    // usage guides
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
    USAGE_FAST_MONEY_SET("Usage: /feud fastmoney set <setId>"),
    USAGE_FAST_MONEY_BIND("Usage: /feud fastmoney bind <p1|p2|clear>"),
    USAGE_FAST_MONEY_ANSWER("Usage: /feud fastmoney answer <text...>"),
    USAGE_FAST_MONEY_NEXT("Usage: /feud fastmoney next"),
    USAGE_FAST_MONEY_BOARD("Usage: /feud fastmoney board <show|hide> [boardId]"),
    USAGE_BOARD_CREATE("Usage: /feud board create <boardId>"),
    USAGE_BOARD_DISPLAY_DYNAMIC("Usage: /feud board display dynamic <boardId>"),
    USAGE_BOARD_DISPLAY_REMOVE("Usage: /feud board display remove <boardId>"),
    USAGE_BOARD_DISPLAY_REMOTE("Usage: /feud board display remote <id> ..."),
    USAGE_BOARD_DISPLAY_REMOTE_REVEAL("Usage: /feud board display remote {boardId} reveal <1-8>"),
    USAGE_BOARD_DISPLAY_REMOTE_CONTROL("Usage: /feud board display remote {boardId} control <red|blue>"),
    USAGE_BOARD_DISPLAY_SELECTION("Usage: /feud board display selection <board|panels|timer> <boardId> [team]"),
    USAGE_BOARD_DISPLAY_SELECTION_PANELS(
            "Usage: /feud board display selection panels <boardId> <red|blue|both> [remove]\n"
                    + "       /feud board display selection panels <boardId> remove <red|blue|both>"),
    BOARD_WAND_GIVEN("Board wand given, right-click the top-left frame, then right-click the bottom-right frame"),
    BOARD_BINDING_MISSING("No board binding found, use /feud board map wand first"),
    BOARD_MAPS_INITIALIZED("Board maps initialized"),
    BOARD_BASE_PAINTED("Board base painted"),
    BOARD_MAP_INIT_FAILED("Board map init failed (binding missing or world unloaded)"),

    // Help (timer)
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
