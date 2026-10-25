# Design

## Context

State after `add-settings-and-voting-scales`:

| Module | Plugin today | Blocks iOS |
|---|---|---|
| `core:model` | `planningpoker.jvm.library` | `java.time.Instant` in `Sprint`, `Task` |
| `core:database` | android library + hilt + room | `java.time` in entity mappers, `Context` in `DatabaseModule` |
| `core:data` | android library + hilt | `java.time.Clock` in both repositories and `DataModule`; DataStore created with `Context` |
| `core:ai` | android library + hilt | Firebase AI Android SDK in `AiModule` and error mapping; `android.util.Log` in `GeminiTechLeadAgent`; a Java test helper for Firebase exceptions |

DAOs already use only `suspend` functions and `Flow`, which Room KMP requires. Tests use JUnit 4, Turbine and coroutines-test, no mocking library. There is no CI. Versions: AGP 8.13, Kotlin 2.2.20, Room 2.8.1.

See proposal.md for motivation. No spec changes: Android behaviour must stay identical.

## Goals / Non-Goals

**Goals:**
- All business rules (scales, validation, prompt, parsing, repositories, preferences mapping) compile once in `commonMain` and are tested in `commonTest`.
- Android code paths, Hilt graph and database file stay the same: an existing install upgrades without data loss.
- `./gradlew :app:assembleDebug` keeps working on any OS; iOS tasks need macOS only.

**Non-Goals:**
- Compose Multiplatform, moving `feature:*`, `core:ui` or `core:designsystem`.
- A working AI call on iOS, App Attest, iOS feature screens.
- Replacing Hilt on Android.

## Decisions

### 1. Android KMP library plugin, two targets for iOS

```kotlin
// planningpoker.kmp.library
plugins: org.jetbrains.kotlin.multiplatform + com.android.kotlin.multiplatform.library
kotlin {
    androidLibrary { namespace (from module), compileSdk, minSdk; withHostTest {} }
    iosArm64(); iosSimulatorArm64()
    jvmToolchain(17)
}
```

- `com.android.kotlin.multiplatform.library` instead of `com.android.library` + `androidTarget()`: the old combination is on its way out in AGP, so adopting it later would mean migrating twice.
- It has a single variant. No `core:*` module has `debug`/`release` source sets; App Check variants stay in `:app`.
- Host and device tests are opt-in (`withHostTest {}`, `withDeviceTest {}`); `planningpoker.kmp.room` enables device tests for the migration test.
- `iosX64` is left out: x86_64 Apple targets are being deprecated by Kotlin and every target adds KSP and link time.
- The new plugin reuses SDK levels from a shared helper, not from `AndroidLibraryConventionPlugin`, which keeps serving `core:ui`, `core:designsystem` and features.

### 2. Room KMP

```kotlin
// planningpoker.kmp.room: androidx.room plugin + KSP
room { schemaDirectory("$projectDir/schemas") }
dependencies { kspAndroid, kspIosArm64, kspIosSimulatorArm64 -> room-compiler }

// commonMain
@Database(...) @ConstructedBy(PlanningPokerDatabaseConstructor::class)
abstract class PlanningPokerDatabase : RoomDatabase()
expect object PlanningPokerDatabaseConstructor : RoomDatabaseConstructor<PlanningPokerDatabase>

fun RoomDatabase.Builder<PlanningPokerDatabase>.configure() =
    setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO)

// androidMain: Room.databaseBuilder(context, name = context.getDatabasePath(DB_NAME).absolutePath)
// iosMain:     Room.databaseBuilder(name = "<NSDocumentDirectory>/$DB_NAME")
```

- Same database file name on Android, so existing installs keep their data; the migration test continues to run on the device against the exported schemas (now under the KMP source layout).
- `BundledSQLiteDriver` gives the same SQLite version on both platforms. *Alternative:* the platform driver on Android. Rejected: two SQLite versions to reason about for a small APK saving.
- Enum columns keep Room's built-in name conversion; verify that the KMP compiler generates it for `Vote` and `VotingSystem` on all targets, otherwise add an explicit converter that writes the same names.

### 3. Time: `kotlin.time`

- `Sprint.createdAt`, `Task.evaluatedAt`: `kotlin.time.Instant`. Entities keep epoch millis, so the schema does not change.
- Repositories take `kotlin.time.Clock`; Hilt provides `Clock.System`; tests use a small fixed fake.
- Android UI formats via `toJavaInstant()` and the existing `DateTimeFormatter`.
- `@OptIn(ExperimentalTime::class)` is applied through the convention plugin's compiler options while the API needs it, not per file.

### 4. `core:ai`: shared logic, platform transport

```
commonMain                                   androidMain
  TechLeadAgent (interface)                    AiModule (Hilt): FirebaseAI, one model per scale
  GeminiTechLeadAgent -> renamed               FirebaseTechLeadModel : TechLeadModel
     DefaultTechLeadAgent(model, logger)       Firebase exception -> TransportFailure mapping
  techLeadInstruction(scale), buildUserPrompt
  TechLeadResponseParser                     iosMain
  public fun interface TechLeadModel {         (nothing: Swift implements TechLeadModel)
      suspend fun generate(scale, instruction, prompt): String?
  }
  TransportFailure(kind: EvaluationError) : Exception
```

- `TechLeadModel` becomes public so Swift can implement it. It receives the instruction text and the scale; the schema cards come from `scale.aiVotes()`, so an iOS implementation builds the same schema without duplicating the card list. This extends the `generate(votingSystem, prompt)` shape from `add-settings-and-voting-scales` so the instruction text is built once in `commonMain` instead of in each platform's model setup.
- Each platform maps its SDK errors to a common `TransportFailure(EvaluationError)` thrown from `generate`; the agent turns it into `TaskEvaluation.Failure`. Error categories stay defined once.
- `android.util.Log` is replaced by an injected `fun interface EvaluationLogger` (Android: `Log.w`; iOS: no-op until the iOS epic), removing `isReturnDefaultValues` from the tests.
- *Alternative:* a community Firebase KMP wrapper. Rejected: no Firebase AI coverage, and an extra dependency on the security-sensitive path (App Check).

### 5. Preferences and repositories in `commonMain`

- `UserPreferencesRepository` already depends only on `DataStore<Preferences>`; it moves to `commonMain` with `datastore-preferences-core`.
- DataStore creation: `androidMain` uses `context.filesDir`, `iosMain` uses `NSDocumentDirectory`, both via `PreferenceDataStoreFactory.createWithPath` with the same file name as before.
- Hilt `@Module`s move to `androidMain`. Library modules apply the Hilt compiler with `kspAndroid` only; the Hilt Gradle plugin stays on `:app`, which is the only place that needs its bytecode transform.

### 6. `:shared` umbrella and iOS container

```
:shared (KMP, iOS targets only)
  export(core:model), export(core:data)
  binaries.framework { baseName = "Shared"; isStatic = true }
  iosMain: class IosDependencyContainer(techLeadModel: TechLeadModel) {
      val sprintRepository; val taskRepository; val userPreferencesRepository
  }
```

- A separate umbrella keeps framework settings out of `core:data` and is the single place that decides the Swift API surface.
- `iosApp/` is created once in Xcode (SwiftUI app template) and committed; a Run Script phase calls `./gradlew :shared:embedAndSignAppleFrameworkForXcode`. The shell passes a placeholder `TechLeadModel` that throws `TransportFailure(Unknown)` and shows the sprint count read from the container.
- *Alternative:* XcodeGen. Rejected for now: another tool to install for a project that will be edited mostly in Xcode.

## Risks / Trade-offs

- [The Android KMP plugin's test support differs from `com.android.library` (device tests, Room `MigrationTestHelper`)] -> migrate `core:database` in its own task and keep the migration test passing before moving on; fall back to `androidTarget()` for that module only if blocked, recorded in this design.
- [Hilt without its Gradle plugin in library modules misses aggregation] -> `:app` keeps the plugin with aggregation enabled; the integration task runs the app and opens every screen.
- [`suspend` interfaces implemented in Swift are awkward (completion handlers)] -> accepted for this epic; the iOS UI epic can add SKIE or a callback adapter.
- [Building on Linux skips iOS targets with warnings] -> expected; iOS tasks are documented as macOS-only.
- [Database path or name drift loses user data on upgrade] -> keep the existing name and Android location; verified by installing the new build over the previous one.
- [Static framework size grows with Room and DataStore] -> accepted; measured once in the integration task.

## Migration Plan

- Migrate in dependency order: build logic -> `core:model` -> `core:database` -> `core:ai` -> `core:data` -> `:shared` -> `iosApp`. Each step leaves Android building and its tests green.
- Rollback: each module step is one commit and can be reverted on its own while the later ones are not yet merged.
