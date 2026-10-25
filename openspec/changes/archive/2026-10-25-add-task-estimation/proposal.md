# Proposal

## Why

This is the core of the product: a solo planning poker round where the user commits to their own estimate first and then gets an independent estimate, a short title and a list of risks from an AI "Tech Lead". Comparing the two numbers is what makes the user rethink scope, and recording the user's vote before seeing the AI's avoids anchoring.

## What Changes

- Sprint details lists the sprint's tasks and offers a create-task action.
- New-task screen: description field, voice input in the device language, and a vote scale `0, 1, 2, 3, 5, 8, 13, 20, ?`.
- "Evaluate with Tech Lead" requires a description and a selected vote; the user's vote is never sent to the AI.
- While the AI is evaluating, an indeterminate progress bar is shown and inputs are locked.
- Nothing is saved until evaluation succeeds; then the task is stored with generated title, description, user vote, AI vote, risks and timestamp.
- On failure or timeout, a Snackbar is shown and the draft is kept for retry.
- The draft (description and vote) survives rotation and the app being killed in the background; an in-flight evaluation survives rotation.
- Saved tasks can be opened, their description and user vote edited, and re-evaluated; the new result overwrites the old one and updates the timestamp.
- The Tech Lead agent receives only the task description and the device language, returns a structured result whose estimate is restricted to `0-20` on the scale (never `?`), and answers in the device language.
- AI requests are accepted only from genuine app instances (App Check enforcement, set up in `bootstrap-foundation`).
- Sprints and tasks are linked so deleting a sprint deletes its tasks.

## Capabilities

### New Capabilities

- `task-estimation`: creating, voting on, evaluating, viewing, editing and re-evaluating tasks inside a sprint.
- `tech-lead-agent`: the contract for the AI evaluation - inputs, structured output, allowed values, language, failure handling and access control.

### Modified Capabilities

None. (`sprint-management` is introduced by `add-sprint-management`, which is not archived yet; its existing requirements already cover deleting a sprint together with its tasks and the no-tasks empty state.)

## Impact

- `core:model`: `Task`, `Vote` / scale types, final `TaskEvaluationRequest` / `TaskEvaluationResult`.
- `core:database`: `TaskEntity` with foreign key to sprints (`ON DELETE CASCADE`), `TaskDao`, auto-migration v1 -> v2.
- `core:ai`: Gemini-backed `TechLeadAgent` implementation with system instruction and response schema.
- `core:data`: `TaskRepository` combining persistence and evaluation.
- `feature:tasks`: task editor screen (new/edit), voice input, vote scale, progress and error states.
- `feature:sprints`: sprint details renders the task list and opens the task editor.
- `:app`: manifest `<queries>` entry for speech recognition.
- External: Gemini Developer API quota (free tier).
- Depends on `bootstrap-foundation` and `add-sprint-management`.
