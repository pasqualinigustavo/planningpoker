# Proposal

## Why

The repository has a partial skeleton that does not build: `settings.gradle.kts`, the module directories and the product spec disagree on module names, there are no module build scripts, the convention plugins referenced by the version catalog do not exist, and nothing is wired for Firebase. Every feature change (sprints, tasks, AI evaluation) depends on a working, agreed-upon foundation, so it must land first.

## What Changes

- Settle the module layout to `core:{model, database, data, designsystem, ui, ai}` and `feature:{sprints, tasks}` plus `:app`, aligning `settings.gradle.kts` and the filesystem.
- **BREAKING** (skeleton only): remove the empty `core/network` and `feature/poker` directories; `core:ai` replaces `core:network` and `feature:tasks` replaces `feature:poker`.
- Implement the convention plugins already declared in the version catalog (`planningpoker.android.application`, `.library`, `.compose`, `.hilt`, `.room`, `.feature`, `planningpoker.jvm.library`) and a build script for every module.
- Complete the Gradle wrapper so the project builds from a clean checkout.
- Configure Hilt (`@HiltAndroidApp` application, `@AndroidEntryPoint` activity), Compose Navigation with type-safe routes, Room in `core:database`, and Paparazzi for snapshot tests.
- Create the base Material 3 theme in `core:designsystem`.
- Integrate Firebase via the BoM and the Google Services plugin, using **Firebase AI Logic** (`firebase-ai`, Gemini Developer API backend) instead of the deprecated `firebase-vertexai`.
- Enforce **Firebase App Check** with Play Integrity, with the debug provider in debug builds only.
- Define the `TechLeadAgent` abstraction in `core:ai` and provide the Firebase AI entry point via Hilt; the concrete evaluation behavior is delivered by `add-task-estimation`.

## Capabilities

### New Capabilities

None. This change is build and wiring work with no user-observable behavior; it sets `skip_specs: true`. The AI evaluation contract is specified in `add-task-estimation` (`tech-lead-agent`).

### Modified Capabilities

None.

## Impact

- Build: `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml` (adds Firebase BoM, `firebase-ai`, App Check libraries, Google Services plugin), `build-logic/convention`, Gradle wrapper.
- Modules: every module gains a build script and minimal source; `core/network` and `feature/poker` are deleted.
- External: Firebase project `planningpokerai-57c5d` needs the Gemini Developer API enabled, App Check registered with Play Integrity (release SHA-256), and enforcement turned on for Firebase AI Logic; the Android API key should be restricted to the app's package and signing certificate.
- Repository: `app/google-services.json` is committed (no longer ignored); this is acceptable only together with App Check enforcement.
- Downstream: `add-sprint-management` and `add-task-estimation` build on this layout.
