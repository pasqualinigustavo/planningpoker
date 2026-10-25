# Proposal

## Why

An iOS version of the app is planned. The domain model, persistence, preferences and Tech Lead evaluation logic are platform-neutral in intent but tied to the JVM and Android today (`java.time`, Android-only Room setup, `android.util.Log`, Firebase AI Android SDK). Moving them to Kotlin Multiplatform now, while the codebase is small, lets the future iOS UI reuse them instead of re-implementing and re-testing the same rules in Swift.

## What Changes

- Gradle convention plugins for Kotlin Multiplatform libraries and Room KMP, targeting Android, `iosArm64` and `iosSimulatorArm64`, built on `com.android.kotlin.multiplatform.library`. No `iosX64` target.
- `core:model`, `core:database`, `core:data` and `core:ai` become Kotlin Multiplatform modules with their logic in `commonMain`.
- Time uses `kotlin.time.Instant` and `kotlin.time.Clock` instead of `java.time`; `kotlinx-datetime` is not added.
- Room uses its multiplatform setup with the bundled SQLite driver; only database file location and Hilt modules are platform-specific.
- `core:ai` keeps the agent, prompt, schema cards, timeout, parsing, validation and error contract in `commonMain`. The model transport is a public interface: Android implements it with Firebase AI; on iOS it will be implemented in Swift with the Firebase iOS SDK and injected.
- New `:shared` umbrella module exporting a static `Shared` framework for iOS with a dependency container for the repositories.
- New `iosApp/` SwiftUI shell that links the framework, initialises the container and database on the simulator and shows a placeholder screen.
- Android behaviour is unchanged.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. This is a build and architecture change; user-visible behaviour on Android does not change and the iOS shell exposes no product feature. `.openspec.yaml` sets `skip_specs: true`.

## Impact

- `build-logic/convention`: new `planningpoker.kmp.library` and `planningpoker.kmp.room` plugins; `planningpoker.jvm.library` loses its last user.
- `gradle/libs.versions.toml`: Kotlin Multiplatform plugin, Android KMP library plugin, `androidx.sqlite:sqlite-bundled`, `androidx.datastore:datastore-preferences-core`.
- `core:model`, `core:database`, `core:data`, `core:ai`: source sets move to `commonMain` / `androidMain` / `iosMain`; tests to `commonTest` where possible.
- Feature modules and `:app`: only adapt to `kotlin.time.Instant` (formatting via `toJavaInstant()`).
- New `:shared` module and `iosApp/` Xcode project; README documents the iOS build.
- Building iOS targets requires macOS with Xcode; Android-only builds keep working elsewhere.
- Depends on `add-settings-and-voting-scales` being applied first (DataStore, `VotingSystem`, scale-aware `TechLeadModel`).
- Out of scope: iOS feature screens, the Swift Firebase AI implementation and App Attest setup (future iOS UI epic), Compose Multiplatform.
