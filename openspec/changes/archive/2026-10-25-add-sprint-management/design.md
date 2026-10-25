# Design

## Context

Builds on `bootstrap-foundation` (module layout, Hilt, Room with schema export, type-safe navigation, theme, Paparazzi). Requirements are in `specs/sprint-management/spec.md`. The task table does not exist yet; it is added by `add-task-estimation`.

## Goals / Non-Goals

**Goals:**
- Reactive list: the UI observes the database, so inserts/deletes show up without manual refresh.
- Testable without a device for everything except DAO queries.

**Non-Goals:**
- Editing/renaming a sprint, undo after delete, search, sorting options.
- Any deadline or date field (explicitly removed from the product).
- Rendering tasks in sprint details (comes with `add-task-estimation`).

## Decisions

### Data model

```
core:model            core:database                     core:data
----------            -------------                     ---------
data class Sprint(    @Entity("sprints",                interface SprintRepository {
  id: Long,             indices=[createdAt])              fun observeSprints(): Flow<List<Sprint>>
  name: String,       SprintEntity(                       fun observeSprint(id): Flow<Sprint?>
  createdAt: Instant    id: Long  (PK, autoGenerate)      suspend fun create(name: String): Long
)                       name: String                      suspend fun delete(id: Long)
                        createdAt: Long (epoch millis)  }
                      )
```

- DAO list query: `ORDER BY createdAt DESC, id DESC` (id breaks ties if two sprints share a millisecond).
- `createdAt` comes from an injected `java.time.Clock` in the repository, so tests are deterministic.
- Name trimming and the non-blank rule live in the repository (`create` rejects blank) and the ViewModel (button enablement), so the rule holds even if UI changes.
- Database version 1 contains only `sprints`. `add-task-estimation` adds `tasks` with a foreign key `ON DELETE CASCADE` via a Room auto-migration 1 -> 2, which is what fulfils "delete all its tasks". Alternative considered: deleting tasks manually in a transaction - rejected, the FK cascade is atomic and cannot be forgotten.

### Screens and navigation

```
SprintsRoute (start)                    SprintDetailsRoute(sprintId: Long)
+-----------------------------+         +-----------------------------+
| Sprints                     |  tap    | <-  Sprint 42          [trash]|
| +-------------------------+ | ------> |                             |
| | Sprint 42   25 Oct 2026 | |         |    (empty state: no tasks)  |
| +-------------------------+ |  back   |                             |
| | Sprint 41   11 Oct 2026 | | <------ |                             |
| +-------------------------+ |         +-----------------------------+
|                       [ + ] |                 | trash
+-----------------------------+                 v
        | + (FAB)                      +---------------------------+
        v                              | Delete sprint?            |
+---------------------------+          | "Sprint 42" and all its   |
| New sprint                |          | tasks will be permanently |
| Name [________________]   |          | deleted. This can't be    |
|        [Cancel] [Save]    |          | undone.                   |
+---------------------------+          |      [Cancel] [Delete]    |
                                       +---------------------------+
```

- The list items show the name and the creation date (formatted in the device locale), giving the user a visible cue for the ordering.
- `Scaffold` + `FloatingActionButton` keeps the create action anchored while the `LazyColumn` scrolls.
- `SprintDetailsRoute` is `@Serializable data class SprintDetailsRoute(val sprintId: Long)`; the ViewModel reads it via `savedStateHandle.toRoute<>()`.
- The delete button uses the error color and the dialog's confirm button is labeled "Delete" (not "OK").

### State and interruptions

- `SprintsViewModel`: `uiState: StateFlow<SprintsUiState>` (`Loading | Empty | Sprints(list)`) from `observeSprints()` via `stateIn(WhileSubscribed(5s))`.
- Dialog visibility and typed name are stored in `SavedStateHandle` (`getStateFlow`), so they survive rotation and process death. Alternative considered: `rememberSaveable` in the composable - works too, but keeping it in the ViewModel makes it unit-testable and keeps validation in one place.
- `SprintDetailsViewModel`: delete dialog visibility in `SavedStateHandle`; after a confirmed delete it emits a one-shot "navigate back" event. If `observeSprint(id)` emits `null` (sprint gone), the screen also navigates back, which also covers a stale back-stack entry after process death.

## Risks / Trade-offs

- [Two sprints created in the same millisecond] -> secondary order by id.
- [Delete is irreversible] -> explicit warning dialog naming the sprint; no undo in this scope.
- [Schema changes before release] -> schemas are exported from v1, so the 1 -> 2 auto-migration in the next change is checked at compile time.

## Migration Plan

First release of the sprints table; no migration. Rollback is reverting the change.
