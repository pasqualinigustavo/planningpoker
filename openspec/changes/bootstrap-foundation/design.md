# Design

## Context

See proposal.md - Why. Current state: version catalog already lists Compose BOM, Material 3, Navigation 2.9, Hilt, Room, Paparazzi, kotlinx-serialization and seven convention plugin ids, but `build-logic/convention` is empty and no module has a build script. `app/google-services.json` exists for package `com.planningpoker`.

## Goals / Non-Goals

**Goals:**
- A clean checkout builds (`./gradlew assembleDebug`) and runs a blank themed screen.
- Module boundaries follow Now in Android: features never depend on each other; data flows `database -> data -> feature`.
- AI access is behind an interface so features and tests never touch the Firebase SDK.
- AI requests are only accepted from genuine app instances.

**Non-Goals:**
- Any sprint or task behavior, Room entities, or the AI prompt/schema (later changes).
- A remote backend, user accounts, or sync. All data is local.
- Remote Config for the model name or prompt.

## Decisions

### Module graph

```
                       :app
        +---------------+----------------+
        v                                v
 :feature:sprints                 :feature:tasks
        |                                |
        +---------> :core:ui <-----------+
        |              |                 |
        |              v                 |
        |      :core:designsystem        |
        +----------> :core:data <--------+
                  +-----+-----+
                  v           v
          :core:database   :core:ai
                  |           |
                  +---> :core:model <---+ (everyone)
```

- `core:model`: pure Kotlin/JVM (`planningpoker.jvm.library`); domain types only, no Android.
- `core:ai` instead of `core:network`: the only remote call is Gemini; there is no REST API. The name states the purpose.
- `feature:tasks` instead of `feature:poker`: the product uses "Task" everywhere (no "Ticket").
- `core:ui` holds composables shared between features that depend on domain models; `core:designsystem` holds theme and model-agnostic components.

### Convention plugins

Implement the seven ids already in the catalog in `build-logic/convention`, reading versions from the root catalog (`versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }`).
- `android.library` / `android.application`: compileSdk 36, targetSdk 36, minSdk 26, Java 17 toolchain.
- `android.feature`: applies library + compose + hilt, and adds `core:ui`, `core:designsystem`, `core:data`, navigation, hilt-navigation-compose, lifecycle dependencies.
- `android.room`: KSP + Room with `exportSchema` into `core/database/schemas` (needed for auto-migrations in later changes).
- Alternative considered: plain per-module scripts. Rejected - nine modules would duplicate SDK/compose/hilt config, and the catalog already commits to convention plugins.

minSdk 26 is chosen because it covers nearly all active devices, includes `java.time` natively, and is supported by Firebase and App Check.

### Navigation

Navigation 2.9 type-safe routes: `@Serializable` route objects/classes, one `navigation/` package per feature exposing `NavGraphBuilder.xxxScreen()` and `NavController.navigateToXxx()`. `:app` owns the `NavHost`. This change adds only a placeholder start destination.

### Firebase AI Logic

- Dependencies via `firebase-bom`: `firebase-ai`, `firebase-appcheck-playintegrity`; `firebase-appcheck-debug` as `debugImplementation`.
- Backend: `GenerativeBackend.googleAI()` (Gemini Developer API, free tier, Spark plan). Switching to Vertex AI later is a one-line change confined to `core:ai`'s Hilt module.
- `firebase-vertexai` is not used: it is deprecated in favor of `firebase-ai`.
- `core:ai` exposes:
  ```kotlin
  interface TechLeadAgent {
      suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluationResult
  }
  ```
  Request/result types live in `core:model`. This change defines the interface, its types as minimal placeholders, and a Hilt module providing `FirebaseAI`; `add-task-estimation` supplies the implementation and final types.

### App Check

- Installed in `Application.onCreate()` before any Firebase call: `PlayIntegrityAppCheckProviderFactory` in release, `DebugAppCheckProviderFactory` in debug. Debug wiring lives in the `debug` source set so the debug provider can never ship in release.
- Prefer limited-use App Check tokens for AI requests if the `firebase-ai` version in the BoM supports them (replay protection).
- Enforcement is a Firebase Console step; documented in tasks as a manual checklist item.

### Theme and snapshots

- `PlanningPokerTheme` (Material 3): dynamic color on API 31+, a static light/dark scheme otherwise.
- Paparazzi applied in `core:designsystem` with one theme snapshot test proving the pipeline; features add their own snapshots later.

## Risks / Trade-offs

- [Committed `google-services.json` exposes the API key] -> App Check enforcement plus API key restriction (package + SHA-1) in Google Cloud Console; the key is not a secret by Firebase design.
- [Debug builds and emulators fail Play Integrity] -> debug provider in debug source set; each developer registers their debug token in the console.
- [Paparazzi 1.3.5 vs compileSdk 36 / AGP 8.13 compatibility] -> verify with the theme snapshot early; if it fails, move to the Paparazzi 2.x line before features write snapshots.
- [Gemini free tier rate limits] -> acceptable for a personal app; errors surface as evaluation failures (handled in `add-task-estimation`).
- [Deleting `core/network` / `feature/poker`] -> they only contain `.gitkeep`; no code is lost.

## Migration Plan

Nothing is released; no data migration. Rollback is reverting the change.
