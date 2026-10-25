# Tasks

## 1. Data layer

- [ ] 1.1 Add `Sprint` to `core:model`; verify `./gradlew :core:model:build`
- [ ] 1.2 Add `SprintEntity` (index on `createdAt`) and `SprintDao` (observe all ordered `createdAt DESC, id DESC`, observe by id, insert, delete by id) and register them in `PlanningPokerDatabase` v1; verify an instrumented DAO test in `core/database/src/androidTest` covers ordering, tie-break and delete, and the v1 schema JSON is exported
- [ ] 1.3 Add `SprintRepository` + Hilt binding in `core:data` with injected `Clock`, trimming and blank-name rejection; verify unit tests with a fake DAO cover trimming, blank rejection, timestamp from the clock and entity-to-model mapping

## 2. Sprints dashboard

- [ ] 2.1 Add `SprintsRoute` and navigation extensions in `feature:sprints` and make it the `NavHost` start destination in `:app`; verify the app opens on the dashboard
- [ ] 2.2 Implement `SprintsViewModel` (`Loading | Empty | Sprints` state, dialog visibility and name in `SavedStateHandle`, save enabled only for non-blank names); verify Turbine tests cover empty -> list after create, blank-name disabled save, and dialog state restored from a pre-filled `SavedStateHandle`
- [ ] 2.3 Implement `SprintsScreen` (`Scaffold`, anchored FAB, `LazyColumn` with name + locale-formatted creation date, "No sprints" empty state); verify Paparazzi snapshots for empty and populated states
- [ ] 2.4 Implement the create-sprint dialog (Name field, Cancel/Save, Save disabled when blank); verify Paparazzi snapshots for empty and filled dialog and manually that rotation keeps typed text

## 3. Sprint details and deletion

- [ ] 3.1 Add `SprintDetailsRoute(sprintId)` and navigation from list item tap; verify tapping a sprint opens its details and back returns to the list
- [ ] 3.2 Implement `SprintDetailsViewModel` (observe sprint, delete dialog state in `SavedStateHandle`, delete then one-shot navigate-back event, navigate back when sprint is gone); verify unit tests cover confirm -> deleted + event, cancel -> nothing deleted, and missing sprint -> navigate back
- [ ] 3.3 Implement `SprintDetailsScreen` (top bar with name and trash action, no-tasks empty state) and the delete confirmation dialog warning that the sprint and all its tasks will be permanently deleted; verify Paparazzi snapshots for the screen and the dialog

## 4. Integration check

- [ ] 4.1 Run `./gradlew testDebugUnitTest verifyPaparazziDebug connectedDebugAndroidTest` and manually walk the spec scenarios (create, blank name, ordering, rotation with open dialog, "Don't keep activities" restore, delete confirm/cancel, restart persistence); verify all pass
