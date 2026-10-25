# Tasks

## 1. Domain model

- [ ] 1.1 Add `VotingSystem`, `ThemePreference` and `UserPreferences` to `core:model`; extend `Vote` with `XS`..`XXL` and the `systems` each card belongs to (`UNKNOWN` in both), add `VotingSystem.votes()` / `aiVotes()`, remove `AI_VOTES`, and add a "names are persisted, never rename" comment on both enums; verify unit tests cover the ordered cards of each scale, `?` in both, `aiVotes()` without `?`, and label parsing of T-shirt cards
- [ ] 1.2 Add `votingSystem` to `Sprint` and to `TaskEvaluationRequest` (still no user vote, sprint name or previous evaluation); verify `core:model` and its dependants compile

## 2. Persistence

- [ ] 2.1 Add `voting_system` (`NOT NULL`, default `FIBONACCI`) to `SprintEntity` with mapping to `Sprint`, add a one-shot `SprintDao` query by id, bump `PlanningPokerDatabase` to version 3 with `AutoMigration(2, 3)` and commit the exported `3.json`; verify `3.json` `createSql` contains `DEFAULT 'FIBONACCI'` and the instrumented DAO tests store and read both scales
- [ ] 2.2 Extend `MigrationTest` with 2 -> 3: a v2 database with sprints and tasks (user vote 5, AI vote 8, risks) migrates with every sprint on `FIBONACCI` and every task field unchanged; verify `./gradlew :core:database:connectedDebugAndroidTest` passes

## 3. Preferences

- [ ] 3.1 Add `androidx.datastore:datastore-preferences` and `androidx.core:core-splashscreen` to `libs.versions.toml`; verify the project syncs and builds
- [ ] 3.2 Implement `UserPreferencesRepository` in `core:data` over an injected `DataStore<Preferences>` (enum names as strings, unknown or missing values -> `SYSTEM` / `FIBONACCI`) and provide the `DataStore` in Hilt with a corruption handler; verify unit tests on a temporary-file DataStore cover defaults, each setter, persistence across a new repository instance and an unknown stored value

## 4. Tech Lead agent

- [ ] 4.1 Make the system instruction per scale (cards, story points vs relative sizes, `20` vs `XXL` as the "too big" card) and the parser scale-aware (`parse(text, votingSystem)` accepts only `aiVotes()` of that scale); verify unit tests cover TSHIRT valid ("XS", "XXL"), TSHIRT invalid ("5", "XXXL", "?"), FIBONACCI invalid ("M", "4", "?"), and that the user prompt still contains no vote or sprint data
- [ ] 4.2 Change `TechLeadModel` to `generate(votingSystem, prompt)`, build and cache one Gemini model per scale in `AiModule` (schema enum from `aiVotes()`), and pass `request.votingSystem` from `GeminiTechLeadAgent`; verify agent unit tests with a fake model receive the request's scale, and a debug build evaluates a sample task on each scale end to end

## 5. Data layer

- [ ] 5.1 Add `votingSystem` to `SprintRepository.create`; verify repository unit tests store the given scale
- [ ] 5.2 Make `TaskRepository` resolve the scale from the sprint (via `task.sprintId` on re-evaluation), reject a user vote outside it, and send it in `TaskEvaluationRequest`; verify unit tests cover create and re-evaluate on a T-shirt sprint, re-evaluation using the sprint's scale while the default preference differs, and a mismatched vote failing without calling the agent

## 6. App theme and splash

- [ ] 6.1 Add `MainActivityViewModel` (`Loading` / `Success(theme)`), install the splash screen with `setKeepOnScreenCondition` while loading, resolve `darkTheme` from the preference, pass it to `PlanningPokerTheme` and match system bar styles; add the `Starting` theme with `postSplashScreenTheme`; verify Turbine tests for the ViewModel states, and on a device: Light with system dark mode shows no dark frame on cold start, and switching in Settings recolours the app without restart

## 7. Settings screen

- [ ] 7.1 Create `feature:settings` (`SettingsRoute`, navigation extension, `SettingsViewModel`, `SettingsScreen` with theme and default-scale radio groups showing each scale's cards, strings in English and Portuguese) and wire the gear action in `SprintsScreen` and the route in `:app`; verify Turbine tests for loading, selection and persistence calls, Paparazzi snapshots in light and dark, and the back navigation to the sprint list

## 8. Sprints

- [ ] 8.1 Add the scale selector to `CreateSprintDialog`, pre-filled once from the default preference when the dialog opens and kept with the name in `SavedStateHandle`; verify `SprintsViewModel` tests cover pre-fill from each default, a choice that does not change Settings, and restoring name and scale from a saved handle, plus updated Paparazzi snapshots of the dialog
- [ ] 8.2 Show the sprint's scale on `SprintDetailsScreen` with no edit action; verify updated `SprintDetailsViewModel` tests and Paparazzi snapshots for a Fibonacci and a T-shirt sprint

## 9. Task screen

- [ ] 9.1 Make `TaskViewModel` observe the route's sprint and expose its `votes()`; render the chips in `TaskScreen` from that list for new and saved tasks; verify Turbine tests for a T-shirt sprint (new task and saved task after the default preference changed) and Paparazzi snapshots for a new T-shirt task and a saved T-shirt task with AI vote and risks

## 10. Integration check

- [ ] 10.1 Run `./gradlew testDebugUnitTest verifyPaparazziDebug connectedDebugAndroidTest`, then on a device: install the current release over a build with existing sprints and confirm they show Fibonacci with intact votes; create a T-shirt sprint, evaluate and re-evaluate a task after switching the default to Fibonacci; rotate and restore ("Don't keep activities") with the create-sprint dialog open; verify all pass
