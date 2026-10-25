# Tasks

## 1. Build logic

- [ ] 1.1 Add the Kotlin Multiplatform plugin, `com.android.kotlin.multiplatform.library`, the `androidx.room` Gradle plugin, `androidx.sqlite:sqlite-bundled` and `androidx.datastore:datastore-preferences-core` to `gradle/libs.versions.toml`; verify the project syncs
- [ ] 1.2 Implement `planningpoker.kmp.library` (Android KMP library + `iosArm64` + `iosSimulatorArm64`, Java 17 toolchain, `kotlin.time` opt-in, host tests enabled) and `planningpoker.kmp.room` (Room plugin, schema directory, KSP per target, device tests enabled), sharing SDK levels with the Android conventions; verify `./gradlew -p build-logic build` and a full `./gradlew assembleDebug` pass

## 2. core:model

- [ ] 2.1 Move `core:model` to `planningpoker.kmp.library` with sources in `commonMain` / `commonTest` (tests on `kotlin.test`), switch `Sprint` and `Task` to `kotlin.time.Instant`, adapt mappers and UI formatting (`toJavaInstant()`) in dependants; verify `./gradlew :core:model:allTests` (or the host and iOS simulator test tasks) and `testDebugUnitTest verifyPaparazziDebug` pass

## 3. core:database

- [ ] 3.1 Move `core:database` to `planningpoker.kmp.room`: entities, converters, DAOs and `PlanningPokerDatabase` with `@ConstructedBy` in `commonMain`, builder with `BundledSQLiteDriver` and IO query context, database path builders in `androidMain` (same file name and location as today) and `iosMain` (`NSDocumentDirectory`), Hilt module in `androidMain` with `kspAndroid` Hilt compiler; verify enum columns still store names, `MigrationTest` (1 -> 2 -> 3) and DAO device tests pass, and `compileKotlinIosSimulatorArm64` succeeds
- [ ] 3.2 Install the new debug build over the previous one on a device with existing sprints and tasks; verify all data is still shown

## 4. core:ai

- [ ] 4.1 Move `core:ai` to `planningpoker.kmp.library`: agent, instruction, prompt, parser, `TransportFailure` and a public `TechLeadModel(scale, instruction, prompt)` in `commonMain`; inject an `EvaluationLogger` instead of `android.util.Log`; Firebase model, error mapping and Hilt module in `androidMain`; verify existing parser, prompt and agent tests run from `commonTest`, the Firebase error-mapping tests run as Android host tests, `isReturnDefaultValues` is removed, and a debug build evaluates a task on each scale

## 5. core:data

- [ ] 5.1 Move `core:data` to `planningpoker.kmp.library`: repositories and `UserPreferencesRepository` in `commonMain` on `kotlin.time.Clock` and `datastore-preferences-core`, DataStore creation in `androidMain` (same file as before) and `iosMain`, Hilt modules in `androidMain`; verify repository tests run from `commonTest` with a fixed fake clock, saved preferences survive the upgrade on a device, and `compileKotlinIosSimulatorArm64` succeeds

## 6. iOS framework and shell

- [ ] 6.1 Create the `:shared` module (iOS targets, `export` of `core:model` and `core:data`, static framework `Shared`) with `IosDependencyContainer(techLeadModel)` in `iosMain` exposing the sprint, task and preferences repositories; verify `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds and the generated header lists the container and repositories
- [ ] 6.2 Create `iosApp/` in Xcode (SwiftUI `iOSApp.swift`, `ContentView.swift`), add the Run Script phase calling `./gradlew :shared:embedAndSignAppleFrameworkForXcode`, pass a placeholder `TechLeadModel` that throws `TransportFailure(Unknown)`, and show the number of stored sprints from the container; verify the app builds and launches on an iOS simulator showing the placeholder screen
- [ ] 6.3 Document the iOS build (requirements, Gradle command, opening `iosApp`) and the macOS-only nature of iOS tasks in `README.md`; verify the documented commands run as written from a clean checkout

## 7. Integration check

- [ ] 7.1 From a clean checkout run `./gradlew :app:assembleDebug testDebugUnitTest verifyPaparazziDebug connectedDebugAndroidTest :shared:linkDebugFrameworkIosSimulatorArm64`, then open every Android screen and evaluate a task on each scale; verify everything passes and record the `Shared` framework size in the PR description
