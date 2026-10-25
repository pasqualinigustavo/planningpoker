# Design

## Context

Current state this change builds on:

- `Vote` is a single enum (`ZERO`..`TWENTY`, `UNKNOWN`) and Room stores it by enum **name** in `tasks.user_vote` / `tasks.ai_vote`. `AI_VOTES = Vote.entries - UNKNOWN`.
- `AiModule` provides one `@Singleton` Gemini model whose `systemInstruction` and `responseSchema` are fixed to `AI_VOTES`. `TechLeadModel` takes only the prompt.
- `TaskRepository.evaluateAndCreate(sprintId, ...)` and `reevaluate(taskId, ...)` already start from the sprint or the task. `TaskRoute(sprintId, taskId?)` always carries the sprint id.
- `PlanningPokerDatabase` is at version 2 with `AutoMigration(1, 2)`; columns are snake_case; `MigrationTest` exists in `core/database/src/androidTest`.
- `MainActivity` calls `PlanningPokerTheme { }` with `darkTheme = isSystemInDarkTheme()`; there is no preferences storage and no splash screen setup.
- `prepare-kmp-ios-foundation` will move `core:*` to Kotlin Multiplatform next, so new code in `core:*` avoids Android APIs where it costs nothing.

Requirements: `specs/app-settings`, `specs/sprint-management`, `specs/task-estimation`, `specs/tech-lead-agent`.

## Goals / Non-Goals

**Goals:**
- One source of truth for a task's scale: its sprint. The scale shown in the UI and the scale sent to the AI cannot diverge.
- No data migration of existing votes; the database upgrade only adds a column.
- No wrong-theme frame at launch.

**Non-Goals:**
- Converting votes between scales, changing a sprint's scale, custom scales.
- Sprint totals or averages.
- Dynamic color settings (wallpaper colors stay on as today).

## Decisions

### 1. `Vote` stays one enum; each card knows its scales

```kotlin
// core:model
enum class VotingSystem { FIBONACCI, TSHIRT }

enum class Vote(val label: String, val systems: Set<VotingSystem>) {
    ZERO("0", FIB), ONE("1", FIB), ..., TWENTY("20", FIB),
    XS("XS", TSHIRT), S("S", TSHIRT), M("M", TSHIRT), L("L", TSHIRT), XL("XL", TSHIRT), XXL("XXL", TSHIRT),
    UNKNOWN("?", BOTH),
}

fun VotingSystem.votes(): List<Vote>     // ordered cards incl. UNKNOWN, for the UI
fun VotingSystem.aiVotes(): List<Vote>   // votes() - UNKNOWN, for schema/prompt/validation
```

- Existing rows keep working: `FIVE` is still `FIVE`. The upgrade is an `ADD COLUMN` only.
- `UNKNOWN` belongs to both scales, so "?" needs no special case.
- `AI_VOTES` is removed; callers use `aiVotes()` of the relevant scale.
- Enum **names** are persisted (Room columns, `SavedStateHandle`); they must never be renamed. A comment on the enum says so.

*Alternative:* one enum per scale (`FibonacciVote`, `TShirtVote`) behind a sealed type. Rejected: needs a new converter and rewriting stored values, and every UI/list path becomes generic for little gain. The compiler no longer prevents a T-shirt vote in a Fibonacci sprint; the repository enforces it (decision 3).

### 2. The scale lives on the sprint

```
sprints (v3)                         tasks (unchanged)
------------                         -----------------
id PK                                sprint_id FK -> sprints.id
name                                 user_vote  TEXT (enum name)
created_at                           ai_vote    TEXT (enum name)
voting_system TEXT NOT NULL          ...
    DEFAULT 'FIBONACCI'   <-- new
```

- `SprintEntity.votingSystem` with `@ColumnInfo(name = "voting_system", defaultValue = ...)`; `Sprint.votingSystem: VotingSystem`.
- `@Database(version = 3, autoMigrations = [AutoMigration(1, 2), AutoMigration(2, 3)])`. Adding a non-null column with a default is supported by auto-migration.
- The `defaultValue` literal must produce `DEFAULT 'FIBONACCI'` in the exported `3.json` `createSql`; check the generated schema rather than assuming Room's quoting.
- `Task` and `TaskEntity` do not get a scale: duplicating it would allow a task and its sprint to disagree.
- `SprintRepository.create(name, votingSystem)`. There is no update path for the scale.

### 3. The repository resolves the scale; the ViewModel only displays it

```
TaskViewModel --observeSprint(route.sprintId)--> chips = sprint.votingSystem.votes()

TaskRepository.evaluateAndCreate(sprintId, description, userVote)
TaskRepository.reevaluate(taskId, description, userVote)
    |
    +-- load sprint (directly, or via task.sprintId)
    +-- require(userVote in scale.votes())          // programming error otherwise
    +-- agent.evaluate(TaskEvaluationRequest(description, languageTag, scale))
    +-- persist
```

- Public signatures of `TaskRepository` do not change; it gains a suspend lookup of the sprint (`SprintDao` one-shot query by id).
- The ViewModel never passes a scale to the repository, so the request cannot carry a scale other than the sprint's.
- `TaskViewModel` observes the sprint for the chip list; the draft vote in `SavedStateHandle` stays an enum name and is valid because the sprint's scale cannot change.

### 4. One Gemini model per scale

```kotlin
// core:ai
internal fun interface TechLeadModel {
    suspend fun generate(votingSystem: VotingSystem, prompt: String): String?
}

// AiModule: lazily builds and caches one GenerativeModel per VotingSystem
systemInstruction = techLeadInstruction(votingSystem)            // cards + unit wording
responseSchema   = ... "estimate" to Schema.enumeration(votingSystem.aiVotes().map { it.label })

// GeminiTechLeadAgent
model.generate(request.votingSystem, buildUserPrompt(request))
TechLeadResponseParser.parse(text, request.votingSystem)          // rejects cards outside aiVotes()
```

- The instruction keeps one template; per scale it changes the card list, the unit ("story points" vs "relative T-shirt sizes") and the "too big or unclear" card (`20` vs `XXL`).
- The user prompt stays description + language only; the scale reaches the model through the instruction and the schema, and is checked again by the parser.
- *Alternative:* build a model per request. Rejected: repeats configuration on every call for no benefit with two fixed scales.
- This interface (prompt + scale in, text out) is also the boundary `prepare-kmp-ios-foundation` keeps for an iOS implementation.

### 5. Preferences in DataStore, repository free of Android types

```kotlin
// core:model
enum class ThemePreference { SYSTEM, LIGHT, DARK }
data class UserPreferences(val theme: ThemePreference, val defaultVotingSystem: VotingSystem)

// core:data
interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setTheme(theme: ThemePreference)
    suspend fun setDefaultVotingSystem(system: VotingSystem)
}
```

- Implementation takes a `DataStore<Preferences>` in its constructor and stores enum names as strings; missing or unknown values map to `SYSTEM` / `FIBONACCI`.
- The `DataStore` instance (file `user_preferences.preferences_pb`, `ReplaceFileCorruptionHandler` returning empty preferences) is created in the Hilt module, the only place that needs `Context`.
- Tests use a real `PreferenceDataStoreFactory` on a temporary file.
- *Alternative:* Proto DataStore. Rejected: two enums do not justify a schema and the protobuf plugin.

### 6. Theme at the root, splash until it is known

```
MainActivity
  installSplashScreen().setKeepOnScreenCondition { uiState.value is Loading }
  MainActivityViewModel: preferences.map { Success(it.theme) }.stateIn(Loading)
  setContent {
      darkTheme = when (theme) { SYSTEM -> isSystemInDarkTheme(); LIGHT -> false; DARK -> true }
      enableEdgeToEdge(...) with system bar style following darkTheme
      PlanningPokerTheme(darkTheme) { PlanningPokerApp() }
  }
```

- `androidx.core:core-splashscreen`; app theme gains a `Theme.PlanningPoker.Starting` parent with `postSplashScreenTheme`.
- Status and navigation bar icon contrast follows the resolved `darkTheme`, not only the system setting.
- `PlanningPokerTheme` itself does not change.

### 7. Settings feature and sprint creation

- New `feature:settings` (convention `planningpoker.android.feature`): `@Serializable object SettingsRoute`, `SettingsViewModel` exposing `Loading` / `Success(UserPreferences)`, `SettingsScreen` with two radio groups. Each scale option shows its cards.
- `SprintsScreen` top bar gets a gear action; `:app` wires `navigateToSettings`.
- `CreateSprintDialog` gains a scale selector. `SprintsViewModel` keeps the selected scale in `SavedStateHandle` next to the name. When the dialog opens it reads the current default once (`preferences.first()`); later changes in the dialog are not written back to Settings.
- `SprintDetailsScreen` shows the scale as a label under the name.

## Risks / Trade-offs

- [A vote from another scale reaches the repository through a bug] -> `require` in the repository fails fast; unit test for both create and re-evaluate.
- [Renaming a `Vote` or `VotingSystem` constant silently breaks stored data] -> comment on both enums; `MigrationTest` and DAO tests read stored names.
- [Room's `defaultValue` quoting differs from expectation and the migration writes a wrong default] -> task checks `3.json` `createSql` and the migration test asserts the migrated value is `FIBONACCI`.
- [DataStore read is slow on first launch and the splash lingers] -> a single small file; acceptable. If it ever matters, fall back to the system theme after a short timeout.
- [Gemini returns lower-case sizes ("xl")] -> the schema enum constrains the value; the parser stays strict and treats it as invalid, as with numbers today.
- [Paparazzi baselines change for the sprint dialog and details] -> regenerate and review those snapshots in the same task.

## Migration Plan

- Database: auto-migration 2 -> 3 on first launch after update; nothing else to migrate. Downgrading the app is not supported (as before).
- Preferences: absent on first launch, so defaults apply (System theme, Fibonacci).
- Rollback: revert the change before release; once a v3 database exists on a device, an older build cannot open it.
