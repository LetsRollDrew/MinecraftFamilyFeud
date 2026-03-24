# State of the Repo

Generated on 2026-03-23 from code inspection, test inspection, git status, and local `./gradlew compileJava` / `./gradlew test` runs.

## Snapshot

- This is a Paper Minecraft plugin for a Family Feud style game.
- Stack: Java 21, Gradle, Paper API `1.21.8-R0.1-SNAPSHOT`, JUnit 5, MockBukkit.
- Repo size at a glance: 159 main Java files, 39 test Java files.
- Main plugin entry is `io.letsrolldrew.feud.FeudPlugin`.
- The repo already contains a lot more than a stub. This is a substantial plugin with real command wiring, UI, display spawning, team/buzzer/timer systems, survey loading, and a partially implemented Fast Money mode.

## What Looks Done

### Core plugin wiring

- Plugin bootstrap is in place and creates the main services, commands, listeners, and registries.
- `/feud` is registered as the root command and dispatches to submodules.
- Brigadier integration is present as an optional enhancement.
- Messaging has been centralized into a `Messages` / `Msg` system instead of scattered raw strings.

Relevant files:

- `src/main/java/io/letsrolldrew/feud/FeudPlugin.java`
- `src/main/java/io/letsrolldrew/feud/bootstrap/PluginBootstrap.java`
- `src/main/java/io/letsrolldrew/feud/commands/FeudRootCommand.java`
- `src/main/java/io/letsrolldrew/feud/messages/Messages.java`

### Standard Family Feud game loop

- Survey loading and validation are implemented.
- The base round controller supports:
  - loading an active survey
  - revealing answer slots
  - tracking strikes
  - tracking round points
  - awarding round points to red or blue
  - resetting round state
- Team state exists for red/blue names, scores, and buzzer bindings.

Relevant files:

- `src/main/java/io/letsrolldrew/feud/survey/SurveyRepository.java`
- `src/main/java/io/letsrolldrew/feud/game/SimpleGameController.java`
- `src/main/java/io/letsrolldrew/feud/team/TeamService.java`

### Visual board and display systems

- There is a nontrivial display layer for Family Feud boards.
- Dynamic board layouts and selection tooling exist.
- Score panels and timer panels can be spawned and rehydrated from stored config.
- Map wall rendering, text fitting, slot reveal visuals, and display registry/storage are all present.

Relevant files:

- `src/main/java/io/letsrolldrew/feud/board/display/DisplayBoardService.java`
- `src/main/java/io/letsrolldrew/feud/board/display/DynamicBoardLayoutBuilder.java`
- `src/main/java/io/letsrolldrew/feud/board/render/BoardRenderer.java`
- `src/main/java/io/letsrolldrew/feud/display/DisplayRegistry.java`
- `src/main/java/io/letsrolldrew/feud/board/display/panels/ScorePanelPresenter.java`
- `src/main/java/io/letsrolldrew/feud/board/display/panels/TimerPanelPresenter.java`

### Host controls and remote UI

- The host book UI is already a major feature, not a placeholder.
- There are multiple pages for:
  - control
  - survey loading
  - selector/setup
  - host config
  - Fast Money
- Click actions route back into commands and refresh the host book afterward.

Relevant files:

- `src/main/java/io/letsrolldrew/feud/ui/HostBookUiBuilder.java`
- `src/main/java/io/letsrolldrew/feud/commands/HostBookActionRouter.java`
- `src/main/java/io/letsrolldrew/feud/ui/pages/FastMoneyPageBuilder.java`

### Timer and buzzer systems

- Timer service is implemented and testable via injected scheduler/clock abstractions.
- Buzzer service exists with lockout and cooldown behavior.
- Team buzzer binding is implemented.

Relevant files:

- `src/main/java/io/letsrolldrew/feud/effects/timer/TimerService.java`
- `src/main/java/io/letsrolldrew/feud/effects/buzz/BuzzerService.java`
- `src/main/java/io/letsrolldrew/feud/effects/buzz/BuzzerListener.java`

### Fast Money foundation

- Fast Money is not just an idea. It already has:
  - round state model
  - question state model
  - survey-pack loading/validation from `fast-money.yml`
  - player bind flow
  - host commands for `set`, `start`, `stop`, `status`, `next`, `bind`, `answer`, `board`, and reveal-award actions
  - host book page for Fast Money actions
  - hover resolver that maps reveal buttons to actual survey answers/points
  - a dedicated Fast Money board backdrop presenter
  - a dedicated Fast Money board placement/presenter path
  - live board rendering for raw answers, awarded points, and per-player totals
  - chat capture for the active responder so answers are stored in order and the host decides whether the typed answer is "good enough" by awarding the matching slot
  - runtime use of pack timer metadata for player 1 / player 2 turns

Relevant files:

- `src/main/java/io/letsrolldrew/feud/fastmoney/FastMoneyService.java`
- `src/main/java/io/letsrolldrew/feud/fastmoney/FastMoneyRoundState.java`
- `src/main/java/io/letsrolldrew/feud/fastmoney/FastMoneyQuestionState.java`
- `src/main/java/io/letsrolldrew/feud/fastmoney/FastMoneySurveySetStore.java`
- `src/main/java/io/letsrolldrew/feud/fastmoney/FastMoneyCommands.java`
- `src/main/java/io/letsrolldrew/feud/effects/fastmoney/FastMoneyPlayerBindService.java`
- `src/main/java/io/letsrolldrew/feud/board/display/fastmoney/FastMoneyBoardPresenter.java`
- `src/main/java/io/letsrolldrew/feud/board/display/fastmoney/FastMoneyBackdropPresenter.java`

## What Is Actively In Progress Right Now

There are uncommitted changes in these files:

- `src/main/java/io/letsrolldrew/feud/board/display/fastmoney/FastMoneyBoardPlacement.java`
- `src/main/java/io/letsrolldrew/feud/board/display/fastmoney/FastMoneyBoardPresenter.java`
- `src/test/java/io/letsrolldrew/feud/board/display/fastmoney/FastMoneyBoardPlacementTest.java`

This looks like active work on the Fast Money board layout/presenter layer:

- moving from a simpler row layout to explicit dual-column anchors
- splitting question rows from total-row anchors
- reshaping the presenter to spawn both player columns on rows 1-5
- adjusting total-row placement logic

## What Is Still Broken Or Incomplete

### 1. Plugin shutdown cleanup is basically empty

`PluginBootstrap.disable()` is empty.

That may be acceptable for now if everything is stateless or already persisted elsewhere, but as the plugin grows it is a clear unfinished area. There is no explicit shutdown cleanup or restore flow here.

### 2. Some config intent still exists without a finished policy

The repo now does capture Fast Money answers from player chat, but `allow-chat-guesses` in `PluginConfig` still is not the switch controlling that behavior.

So the feature exists, but the config toggle / policy around enabling or disabling it is still unfinished.

### 3. Fast Money test coverage is much better, but still light at the display-integration layer

What is covered:

- Fast Money service/state/store logic
- Fast Money player binding service
- Fast Money command progression and chat-answer capture
- Fast Money board placement math
- Fast Money backdrop placement math

What I did not find:

- a `FastMoneyBoardPresenterTest`
- integration coverage for full Fast Money round flow
- tests that assert the board actually reflects Fast Money state

## What You Have Clearly Already Accomplished

If I reduce the repo to the most important accomplishments, you have already built:

1. A real command-driven Family Feud plugin skeleton on Paper.
2. Survey data loading and validation.
3. A working standard Family Feud round controller.
4. A host remote/book UI system with action routing.
5. Display-board infrastructure, including dynamic layouts and panel presenters.
6. Team, buzzer, and timer subsystems.
7. A substantial Fast Money foundation, including state, config packs, bind flow, commands, and board layout work.

This is already past the "prototype idea" phase. The repo is now in the "finish policy details and polish lifecycle/integration edges" phase.

## Recommended Next Order Of Work

### Remaining polish

1. Decide whether `allow-chat-guesses` should gate Fast Money chat capture, be renamed, or be removed.
2. Decide what shutdown cleanup should happen in `PluginBootstrap.disable()`.

### Confidence and coverage

3. Add a `FastMoneyBoardPresenterTest` that asserts answer/points/total cell updates.
4. Add at least one integration test around:
   - show Fast Money board
   - hide Fast Money board
   - restore standard board
5. Add an integration test that runs a full host-controlled Fast Money round end to end.

## Bottom Line

The repo already has a strong base and a lot of real implementation. The main unfinished area is not "starting Fast Money from scratch." The main unfinished area is finishing the runtime wiring so Fast Money behaves like a complete minigame instead of a partially connected state machine plus board shell.

If you want the shortest honest summary:

- Standard Family Feud systems are largely built.
- Fast Money foundations are built.
- Fast Money full gameplay flow is now wired for chat capture, host awarding, question progression, player 2 transition, totals, timers, and board updates.
- The repo compiles and tests pass locally.
