# Proposal

## Why

Sprints are the container for every estimated task: without a way to create, list, open and delete sprints, there is nowhere to run planning poker. This is the first user-facing capability and the home screen of the app.

## What Changes

- Sprints dashboard as the app's start screen: sprints listed newest first by creation date, an empty state when there are none, and an always-visible create action.
- Create-sprint dialog with a single **Name** field; save is only possible with a non-blank name; the creation time is recorded automatically. There is no deadline or date field.
- Sprint details screen reached by tapping a sprint, showing the sprint name and an empty state when it has no tasks (task listing arrives in `add-task-estimation`).
- Delete action on sprint details with a confirmation dialog that warns the sprint **and all its tasks** will be permanently deleted; confirming deletes and returns to the dashboard.
- Typed input (create dialog) survives screen rotation and the app being killed in the background.
- Sprints are stored locally on the device and persist across app restarts.

## Capabilities

### New Capabilities

- `sprint-management`: creating, listing, viewing and deleting sprints stored locally on the device.

### Modified Capabilities

None.

## Impact

- `core:model`: `Sprint` domain type.
- `core:database`: `SprintEntity`, `SprintDao`, database version 1 with the sprints table.
- `core:data`: `SprintRepository`.
- `feature:sprints`: dashboard, create dialog, sprint details, delete confirmation, navigation routes.
- `:app`: dashboard becomes the start destination.
- Depends on `bootstrap-foundation`. `add-task-estimation` extends sprint details with the task list and links tasks to sprints with cascade delete.
