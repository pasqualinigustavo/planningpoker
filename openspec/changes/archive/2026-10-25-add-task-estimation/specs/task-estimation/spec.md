# Spec Delta

## Purpose

Lets the user run a solo planning poker round for each task in a sprint: describe the task by text or voice, commit to their own estimate, get an independent evaluation from the Tech Lead agent, and keep both side by side, re-evaluating when the scope changes.

## ADDED Requirements

### Requirement: Task list in sprint details
The system SHALL list the sprint's saved tasks on the sprint details screen, most recently saved first. Each item SHALL show the task title, the user's vote and the AI's vote.

#### Scenario: Tasks shown
- **WHEN** the user opens a sprint with saved tasks
- **THEN** each task is listed with its title, user vote and AI vote, the most recently saved first

#### Scenario: Only this sprint's tasks
- **WHEN** the user opens sprint "A"
- **THEN** tasks saved in other sprints are not listed

### Requirement: Create task action
The system SHALL provide a create-task action on the sprint details screen that opens a new-task screen for that sprint.

#### Scenario: Open new task
- **WHEN** the user taps the create-task action in sprint "A"
- **THEN** an empty new-task screen for sprint "A" opens

### Requirement: Vote scale
The system SHALL let the user select exactly one vote from the scale 0, 1, 2, 3, 5, 8, 13, 20 and "?". Selecting a different value SHALL replace the previous selection.

#### Scenario: Select a vote
- **WHEN** the user taps 5 and then 8
- **THEN** only 8 is selected

#### Scenario: Unknown vote
- **WHEN** the user taps "?"
- **THEN** "?" is selected as the user's vote

### Requirement: Voice input for description
The system SHALL let the user dictate the task description using speech recognition in the device language. Recognized text SHALL be added to the description; cancelling recognition SHALL leave the description unchanged. If speech recognition is unavailable on the device, the voice action SHALL not be offered.

#### Scenario: Dictation appended
- **WHEN** the description is "Login screen" and the user dictates "with biometric support"
- **THEN** the description becomes "Login screen with biometric support"

#### Scenario: Dictation cancelled
- **WHEN** the user starts dictation and cancels it
- **THEN** the description is unchanged

#### Scenario: No recognizer
- **WHEN** the device has no speech recognition service
- **THEN** the voice action is hidden

### Requirement: Evaluation preconditions
The system SHALL enable "Evaluate with Tech Lead" only when the description contains non-whitespace characters and the user has selected a vote.

#### Scenario: Missing vote
- **WHEN** the description is filled but no vote is selected
- **THEN** the evaluate action is disabled

#### Scenario: Missing description
- **WHEN** a vote is selected but the description is blank
- **THEN** the evaluate action is disabled

### Requirement: Draft kept in memory until evaluated
The system SHALL NOT store a new task until an evaluation succeeds. Leaving the new-task screen before that SHALL discard the draft.

#### Scenario: Leave without evaluating
- **WHEN** the user types a description, selects a vote and navigates back
- **THEN** no task is stored in the sprint

### Requirement: Draft survives interruptions
The system SHALL preserve the description and selected vote on the task screen across screen rotation and across the app being stopped by the system in the background and restored.

#### Scenario: Rotation
- **WHEN** the user has typed a description and selected 3, then rotates the device
- **THEN** the same description and vote 3 are shown

#### Scenario: Process restored
- **WHEN** the system kills the app in the background while a draft is filled, and the user returns
- **THEN** the same description and vote are shown

### Requirement: Evaluation progress
The system SHALL show an indeterminate progress bar while an evaluation is running, and SHALL disable the description, voice input, vote scale and evaluate action until it finishes. A running evaluation SHALL continue across screen rotation.

#### Scenario: Progress shown
- **WHEN** the user taps "Evaluate with Tech Lead"
- **THEN** a progress bar is shown and the inputs and evaluate action are disabled

#### Scenario: Rotation during evaluation
- **WHEN** the device is rotated while an evaluation is running
- **THEN** the progress bar is still shown and the result is applied when it arrives, without sending a second request

### Requirement: Save on successful evaluation
The system SHALL store the task when the evaluation succeeds, with the generated title, the description, the user's vote, the AI's vote, the risks and the time of saving, and SHALL then show the saved task.

#### Scenario: Successful evaluation
- **WHEN** the evaluation returns title "Biometric login", estimate 8 and two risks
- **THEN** a task is stored with those values, the user's vote and the current time, and the saved task is shown with both votes and the risks

### Requirement: Evaluation failure keeps the draft
The system SHALL show a Snackbar explaining that the evaluation failed when the evaluation fails or times out, store nothing, re-enable the inputs and keep the description and vote so the user can retry.

#### Scenario: No connection
- **WHEN** the user evaluates with no network connection
- **THEN** a Snackbar reports the failure, no task is stored, and the description and vote are unchanged and editable

#### Scenario: Retry
- **WHEN** the user taps evaluate again after a failure and the evaluation succeeds
- **THEN** the task is stored as in a first successful evaluation

### Requirement: View saved task
The system SHALL show a saved task when the user selects it in the sprint details list, with its title, description, user vote, AI vote, risks and time of last evaluation.

#### Scenario: Open task
- **WHEN** the user taps a task in the sprint details list
- **THEN** the task screen shows all of its saved fields

### Requirement: Edit and re-evaluate saved task
The system SHALL let the user edit a saved task's description and user vote and re-evaluate it. A successful re-evaluation SHALL overwrite the stored description, user vote, title, AI vote, risks and timestamp. Edits SHALL be stored only through a successful re-evaluation; previous results are not kept.

#### Scenario: Successful re-evaluation
- **WHEN** the user changes the description, selects 13 and taps "Re-evaluate" and the evaluation succeeds
- **THEN** the stored task holds the new description, vote 13, the new title, AI vote and risks, and the new timestamp

#### Scenario: Failed re-evaluation
- **WHEN** a re-evaluation fails
- **THEN** a Snackbar reports the failure, the stored task is unchanged, and the user's edits remain on screen for retry

#### Scenario: Leave without re-evaluating
- **WHEN** the user edits a saved task and navigates back without re-evaluating
- **THEN** the stored task is unchanged

#### Scenario: Re-evaluation shows progress
- **WHEN** a re-evaluation is running
- **THEN** the same progress and input locking as a first evaluation apply
