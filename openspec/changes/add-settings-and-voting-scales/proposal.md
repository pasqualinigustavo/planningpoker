# Proposal

## Why

The app always follows the system theme and only knows the Fibonacci scale, while many teams size work with T-shirt sizes and some users want a fixed light or dark appearance. A sprint is the natural unit for a scale: a team sizes one sprint with one scale, so the scale is chosen when the sprint is created and stays fixed, keeping every vote in that sprint comparable.

## What Changes

- New Settings screen, opened from a gear action in the Sprints top bar, with:
  - Theme: System default, Light or Dark, applied immediately and kept across restarts.
  - Default voting scale: Fibonacci (`0, 1, 2, 3, 5, 8, 13, 20, ?`) or T-shirt (`XS, S, M, L, XL, XXL, ?`), used only to pre-fill new sprints.
- The app keeps the launch splash on screen until the saved theme is known, so it never flashes the wrong theme.
- The create-sprint dialog asks for the voting scale, pre-filled with the default from Settings. The scale cannot be changed after the sprint is created.
- Sprint details shows the sprint's scale.
- Existing sprints are migrated to Fibonacci; their tasks and votes are untouched.
- The task screen offers the cards of the sprint's scale for new and saved tasks, including during re-evaluation.
- The Tech Lead agent receives the scale and must answer with a card of that scale (never `?`); answers outside it are failures. **BREAKING** for the agent contract: the request gains a scale, and the role is no longer described only in story points.

## Capabilities

### New Capabilities

- `app-settings`: user preferences kept on the device - theme selection and its application at launch, and the default voting scale for new sprints.

### Modified Capabilities

- `sprint-management`: creating a sprint now asks for a voting scale, the scale is fixed after creation, and sprint details shows it.
- `task-estimation`: the vote scale offered on the task screen is the sprint's scale instead of a fixed Fibonacci scale.
- `tech-lead-agent`: the request includes the scale, the role covers relative sizing in either scale, and allowed estimates depend on the scale.

## Impact

- `core:model`: `ThemePreference`, `VotingSystem`, `UserPreferences`; `Vote` gains T-shirt cards and per-scale lists; `Sprint` gains `votingSystem`; `TaskEvaluationRequest` gains `votingSystem`.
- `core:database`: `sprints.voting_system` column, version 2 -> 3 auto-migration, exported v3 schema, extended migration test. The `tasks` table is unchanged.
- `core:data`: new DataStore-backed `UserPreferencesRepository`; `SprintRepository.create` takes a scale; `TaskRepository` resolves the scale from the sprint and rejects votes outside it.
- `core:ai`: one Gemini model per scale (schema enum and instruction), prompt and parser parameterised by scale.
- New `feature:settings` module; `feature:sprints` (gear action, scale in the create dialog and details); `feature:tasks` (cards from the sprint's scale).
- `:app`: navigation to Settings, theme observed at the root, AndroidX SplashScreen.
- New dependencies: `androidx.datastore:datastore-preferences`, `androidx.core:core-splashscreen`.
- Out of scope: changing a sprint's scale, custom scales, per-task scales, totals per sprint, the Kotlin Multiplatform migration (`prepare-kmp-ios-foundation`).
