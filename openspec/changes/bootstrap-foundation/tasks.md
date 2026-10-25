# Tasks

## 1. Module layout and Gradle

- [x] 1.1 Delete `core/network` and `feature/poker`; create `core/ai` and `feature/tasks` with `src/main/kotlin/com/planningpoker/...` packages; verify `settings.gradle.kts` includes exactly `:app`, `:core:{model,database,data,ai,designsystem,ui}`, `:feature:{sprints,tasks}` and each has a matching directory
- [x] 1.2 Complete the Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) and verify `./gradlew --version` runs from a clean checkout
- [x] 1.3 Add Firebase BoM, `firebase-ai`, `firebase-appcheck-playintegrity`, `firebase-appcheck-debug` and the Google Services plugin to `libs.versions.toml`; verify `./gradlew help` resolves the catalog

## 2. Convention plugins

- [x] 2.1 Create `build-logic/convention` build and settings scripts reading the root version catalog; verify `./gradlew -p build-logic build` succeeds
- [x] 2.2 Implement `planningpoker.android.application`, `.android.library` and `.jvm.library` (compileSdk/targetSdk 36, minSdk 26, Java 17); verify by applying them in the next group
- [x] 2.3 Implement `planningpoker.android.compose`, `.android.hilt` and `.android.room` (schema export to `core/database/schemas`); verify by applying them in the next group
- [x] 2.4 Implement `planningpoker.android.feature` wiring library + compose + hilt and the shared feature dependencies; verify by applying it to both feature modules

## 3. Module build scripts

- [x] 3.1 Add build scripts for `core:model` (jvm) and `core:designsystem`, `core:ui` (library + compose); verify `./gradlew :core:model:build :core:designsystem:assembleDebug :core:ui:assembleDebug`
- [x] 3.2 Add build scripts for `core:database` (library + hilt + room), `core:data` (library + hilt) and `core:ai` (library + hilt + firebase-ai); verify each assembles
- [x] 3.3 Add build scripts for `feature:sprints`, `feature:tasks` and `:app` (application + compose + hilt + google-services, depends on both features); verify `./gradlew assembleDebug` succeeds

## 4. App shell

- [x] 4.1 Create `PlanningPokerApplication` (`@HiltAndroidApp`) and `MainActivity` (`@AndroidEntryPoint`, edge-to-edge, `setContent`) and register them in the manifest; verify the debug app installs and launches without crashing
- [x] 4.2 Add the `NavHost` in `:app` with a placeholder `@Serializable` start route; verify the placeholder screen renders on launch
- [x] 4.3 Add an empty `PlanningPokerDatabase` (`@Database`, version 1, no entities yet or a placeholder entity as Room requires) and its Hilt module in `core:database`; verify `./gradlew :core:database:kspDebugKotlin` succeeds and a schema JSON is exported

## 5. Design system

- [x] 5.1 Create `PlanningPokerTheme` (Material 3, dynamic color on API 31+, static light/dark fallback) and apply it in `MainActivity`; verify visually in light and dark mode
- [x] 5.2 Configure Paparazzi in `core:designsystem` with a theme snapshot test (light + dark); verify `./gradlew :core:designsystem:recordPaparazziDebug` then `verifyPaparazziDebug` pass

## 6. Firebase, App Check and AI abstraction

- [x] 6.1 Install App Check in `Application.onCreate()`: Play Integrity provider in release, debug provider from the `debug` source set only; verify the release APK contains no `DebugAppCheckProviderFactory` class (`apkanalyzer dex packages`) and debug logs print the debug token
- [x] 6.2 Define `TechLeadAgent` and its request/result placeholder types (types in `core:model`, interface in `core:ai`), plus a Hilt module providing `FirebaseAI` with `GenerativeBackend.googleAI()`; verify `:core:ai` unit test injects a fake `TechLeadAgent` without Firebase
- [ ] 6.3 Firebase Console (manual): enable Gemini Developer API in Firebase AI Logic, register the app with App Check (Play Integrity, release SHA-256), add the debug token, turn on enforcement for Firebase AI Logic; verify by recording the console state in the PR description
- [ ] 6.4 Google Cloud Console (manual): restrict the Android API key to package `com.planningpoker` and the release/debug SHA-1; verify by recording the restriction in the PR description

## 7. Integration check

- [x] 7.1 From a clean clone run `./gradlew assembleDebug testDebugUnitTest verifyPaparazziDebug`; verify all succeed and the app launches to the themed placeholder
