# Design

## Context

Builds on `bootstrap-foundation` (`TechLeadAgent` interface in `core:ai`, `FirebaseAI` provided via Hilt with the Gemini Developer API backend, App Check installed) and `add-sprint-management` (`sprints` table v1, sprint details screen with empty state). Requirements: `specs/task-estimation/spec.md`, `specs/tech-lead-agent/spec.md`.

## Goals / Non-Goals

**Goals:**
- Features never touch the Firebase SDK; everything AI-specific sits in `core:ai`.
- The AI response shape is enforced by the model API (response schema), and checked again in the app.
- Draft and in-flight state behave correctly across rotation and process death.

**Non-Goals:**
- Evaluation history, comparing old and new results, multi-user voting, highlighting how far apart the user and AI votes are.
- Deleting a single task (only via sprint deletion).
- Remote Config for prompt/model; streaming responses.

## Decisions

### Domain model

```kotlin
// core:model
enum class Vote(val label: String) { ZERO("0"), ONE("1"), TWO("2"), THREE("3"), FIVE("5"),
    EIGHT("8"), THIRTEEN("13"), TWENTY("20"), UNKNOWN("?") }
val AI_VOTES = Vote.entries - Vote.UNKNOWN

data class Task(id, sprintId, title, description, userVote: Vote, aiVote: Vote,
                risks: List<String>, evaluatedAt: Instant)

data class TaskEvaluationRequest(description: String, languageTag: String)
data class TaskEvaluationResult(title: String, estimate: Vote, risks: List<String>)
```

A single `Vote` enum serves both sides so list items and comparisons are uniform; the "AI never says ?" rule is enforced by the schema enum and by validation that rejects `UNKNOWN`. The request type has no field for the user vote, so it cannot be sent by mistake.

### Persistence

```
sprints (v1)                     tasks (new in v2)
-----------                      -----------------
id  PK  <----------------------- sprintId  FK ON DELETE CASCADE, indexed
name                             id PK autoGenerate
createdAt                        title, description
                                 userVote  TEXT (enum name)
                                 aiVote    TEXT (enum name)
                                 risks     TEXT (JSON array, TypeConverter)
                                 evaluatedAt INTEGER (epoch millis)
```

- `@Database(version = 2, autoMigrations = [AutoMigration(1, 2)])` - adding a table is supported by auto-migration. Foreign keys are enforced by Room for the declared FK, giving the cascade required by the sprint delete requirement.
- Risks stored as a JSON string via kotlinx-serialization instead of a child table: they are always read and replaced together and never queried.
- `TaskDao`: observe by sprint ordered `evaluatedAt DESC, id DESC`; observe by id; insert; update.

### AI implementation (`core:ai`)

```kotlin
FirebaseAI (googleAI backend)
  .generativeModel(
     modelName = MODEL_NAME,             // single constant, e.g. a current Gemini Flash model
     systemInstruction = content { text(TECH_LEAD_INSTRUCTION) },
     generationConfig = generationConfig {
        responseMimeType = "application/json"
        responseSchema = Schema.obj(mapOf(
            "title"    to Schema.string(),
            "estimate" to Schema.enumeration(listOf("0","1","2","3","5","8","13","20")),
            "risks"    to Schema.array(Schema.string())))
     })
```

- System instruction: role only (senior Tech Lead, relative complexity in story points not time, concise title, concrete technical/scope risks, never ask questions). The output shape is the schema's job, not the prompt's.
- User content: the description plus "Respond in `<languageTag>`" (from `Locale.getDefault().toLanguageTag()`, read in `core:data` and passed in the request).
- `withTimeout(30.seconds)` around the call.
- Parse `response.text` with kotlinx-serialization, then validate: non-blank title, estimate maps to `AI_VOTES`, no blank risks (trimmed). Any violation is an `InvalidResponse`.
- Errors are mapped to `EvaluationError { Network, Timeout, InvalidResponse, Rejected, Unknown }` returned as a `Result`-like sealed type; Firebase exception types never leave `core:ai`. `Rejected` covers App Check / quota / safety blocks.
- Alternatives: JSON-by-prompt only (rejected - can return out-of-scale or malformed values, which is exactly what Story 3.4 wanted to avoid); function calling (rejected - more machinery for a single structured answer).

### Data layer

`TaskRepository` in `core:data`:
- `observeTasks(sprintId)`, `observeTask(id)`
- `evaluateAndCreate(sprintId, description, userVote): EvaluationOutcome` - calls the agent, and only on success inserts with `evaluatedAt = clock.instant()`.
- `reevaluate(taskId, description, userVote): EvaluationOutcome` - calls the agent, and only on success updates all fields in one write.

### Screen and navigation (`feature:tasks`)

One screen for both modes: `@Serializable data class TaskRoute(val sprintId: Long, val taskId: Long? = null)`.

```
+--------------------------------+
| <-  New task / <title>         |
|================================| <- LinearProgressIndicator (only while evaluating)
| Description                    |
| [__________________________][mic]
|                                |
| Your vote                      |
| (0)(1)(2)(3)(5)(8)(13)(20)(?)  | <- LazyRow of selectable chips
|                                |
| -- Tech Lead (saved tasks) --- |
| AI vote: 8                     |
| Risks:                         |
|  * Biometric API fragmentation |
|  * ...                         |
|                                |
| [ Evaluate with Tech Lead ]    | <- "Re-evaluate" for saved tasks
+--------------------------------+
```

- After a successful first evaluation the ViewModel emits a navigation event to `TaskRoute(sprintId, newTaskId)` with `popUpTo` the new-task entry, so back goes to sprint details and the user sees both votes immediately.
- Sprint details (in `feature:sprints`) renders the task list from `observeTasks(sprintId)` and navigates to `TaskRoute`; to keep features independent, `:app` wires the navigation callbacks (`onTaskClick`, `onCreateTask`) between the two feature graphs.

### State and interruptions

```
             +----------- edit -----------+
             v                            |
 [Editing] --evaluate--> [Evaluating] --fail--> [Editing + Snackbar]
   (inputs enabled)      (progress bar,
                          inputs disabled) --ok--> [Saved result shown]
```

- `description` and `selectedVote` live in `SavedStateHandle` (survive rotation and process death). For saved tasks, they are initialized from the database once (only if not already present in the handle, so restored edits win).
- The evaluation runs in `viewModelScope`, which survives rotation; `isEvaluating` is in-memory only. After process death the request is gone, so the screen restores to `Editing` with the draft intact - acceptable, since nothing was saved and the user can tap evaluate again.
- Errors are delivered as one-shot UI events shown in a `SnackbarHost`.
- The evaluate button also ignores taps while `isEvaluating`, so no duplicate requests.

### Voice input

- `rememberLauncherForActivityResult(StartActivityForResult())` with `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`, `EXTRA_LANGUAGE_MODEL = FREE_FORM`, `EXTRA_LANGUAGE = device language tag`.
- Uses the system recognizer activity, so no `RECORD_AUDIO` permission is needed.
- Availability: `SpeechRecognizer.isRecognitionAvailable(context)`; requires a `<queries><intent><action android:name="android.speech.RecognitionService"/></intent></queries>` entry for Android 11+ package visibility. Mic button hidden when unavailable.
- Result text is appended to the description with a single space separator.

## Risks / Trade-offs

- [Gemini model names get retired] -> name is a single constant in `core:ai`; changing it is a one-line change.
- [Free tier rate limits / quota] -> surfaced as `Rejected`, shown as a failure Snackbar with retry.
- [Model ignores the language instruction] -> language tag is in the user content right next to the task; acceptable residual risk, not validated in-app.
- [User's description may contain sensitive data sent to Google] -> inherent to the product; nothing else (votes, sprint names) is sent.
- [Process death during evaluation drops the request] -> draft is restored; user retries. A completed-but-unseen response is lost, which costs one request.
- [Auto-migration 1 -> 2 correctness] -> migration test with `MigrationTestHelper` using the exported v1 schema.

## Migration Plan

Database v1 -> v2 via auto-migration (adds `tasks`). Existing sprints are untouched. Rollback before release is reverting the change; after release a downgrade is not supported.
