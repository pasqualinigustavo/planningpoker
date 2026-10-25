# Tasks

## 1. Domain model

- [ ] 1.1 Add `Vote` (with `AI_VOTES` excluding `UNKNOWN`), `Task`, and the final `TaskEvaluationRequest` (description, languageTag only) / `TaskEvaluationResult` to `core:model`, replacing the bootstrap placeholders; verify unit tests cover vote labels, `AI_VOTES` contents and label-to-vote parsing

## 2. Persistence

- [ ] 2.1 Add `TaskEntity` (FK to sprints `ON DELETE CASCADE`, index on `sprintId`), risks `TypeConverter`, and `TaskDao` (observe by sprint ordered `evaluatedAt DESC, id DESC`, observe by id, insert, update); verify instrumented DAO tests cover ordering, sprint filtering, update and cascade delete when the sprint is deleted
- [ ] 2.2 Bump `PlanningPokerDatabase` to version 2 with `AutoMigration(1, 2)`; verify a `MigrationTestHelper` test migrates a v1 database with sprints to v2 keeping all sprints

## 3. Tech Lead agent

- [ ] 3.1 Implement the Gemini-backed `TechLeadAgent` in `core:ai` (model constant, role-only system instruction, response schema with estimate enum `0..20` without `?`, language instruction in the user content, 30 s timeout) and bind it in Hilt; verify the app compiles and a debug build evaluates a sample task end to end
- [ ] 3.2 Implement response parsing and validation plus error mapping (`Network`, `Timeout`, `InvalidResponse`, `Rejected`, `Unknown`) behind a parser that is testable without Firebase; verify unit tests cover valid JSON, off-scale estimates (4, 21, "?"), blank title, blank risk, empty risk list (valid), malformed JSON and timeout mapping
- [ ] 3.3 Verify by unit test that the built prompt contains the description and language tag and contains no user vote, sprint data or previous evaluation

## 4. Data layer

- [ ] 4.1 Implement `TaskRepository` (`observeTasks`, `observeTask`, `evaluateAndCreate`, `reevaluate`) with injected `Clock` and device language provider; verify unit tests with fake agent and DAO cover: success inserts all fields, failure stores nothing, re-evaluation success overwrites all fields and timestamp, re-evaluation failure leaves the task unchanged

## 5. Task screen

- [ ] 5.1 Add `TaskRoute(sprintId, taskId?)` and navigation extensions in `feature:tasks`, and wire them in `:app`; verify navigation compiles and the route opens an empty screen
- [ ] 5.2 Implement `TaskViewModel` (draft in `SavedStateHandle`, one-time init from DB for saved tasks, evaluate enabled only with non-blank description and selected vote, `isEvaluating` lock and duplicate-tap guard, success navigation event, failure Snackbar event); verify Turbine tests cover preconditions, success -> navigation event, failure -> event + draft kept, restored handle wins over DB values, and no second agent call while evaluating
- [ ] 5.3 Implement `TaskScreen` (description field, vote `LazyRow`, indeterminate progress bar, disabled inputs while evaluating, AI vote and risks section for saved tasks, "Evaluate with Tech Lead" / "Re-evaluate" button, `SnackbarHost`); verify Paparazzi snapshots for new empty, filled, evaluating, and saved-task states
- [ ] 5.4 Add voice input (`RecognizerIntent` launcher with device language, `<queries>` entry for `RecognitionService`, mic hidden when unavailable, result appended with a space); verify a unit test of the append logic and manually dictate on a device, including cancel

## 6. Sprint details integration

- [ ] 6.1 Render the sprint's tasks in sprint details (title, user vote, AI vote; empty state when none) and add the create-task FAB, navigating to `TaskRoute`; verify Paparazzi snapshots with tasks and updated ViewModel tests for the task list

## 7. Integration check

- [ ] 7.1 Run `./gradlew testDebugUnitTest verifyPaparazziDebug connectedDebugAndroidTest`, then manually walk the spec scenarios on a device: create and evaluate, airplane-mode failure and retry, rotation while typing and while evaluating, "Don't keep activities" restore, re-evaluate success and failure, Portuguese device language, delete a sprint with tasks; verify all pass
- [ ] 7.2 Verify App Check enforcement: a debug build without a registered debug token gets its evaluation rejected (failure Snackbar), and with the token registered it succeeds
