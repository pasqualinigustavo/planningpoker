# sprint-management Specification

## Purpose

Lets the user organize estimation work into sprints: create them by name, browse them newest first, open one to see its tasks, and delete one together with everything it contains. All data stays on the device.

## Requirements

### Requirement: Sprint list ordering
The system SHALL show all saved sprints on the start screen, ordered by creation time with the most recently created sprint first.

#### Scenario: Newest sprint first
- **WHEN** the user has created sprint "A" and later sprint "B"
- **THEN** the list shows "B" above "A"

#### Scenario: List reflects a new sprint immediately
- **WHEN** the user saves a new sprint
- **THEN** it appears at the top of the list without leaving or refreshing the screen

### Requirement: Empty sprint list
The system SHALL show an empty state that tells the user there are no sprints and invites them to create one when no sprints exist.

#### Scenario: First launch
- **WHEN** the user opens the app and no sprints are saved
- **THEN** the empty state "No sprints" is shown instead of a list

#### Scenario: Last sprint deleted
- **WHEN** the user deletes the only remaining sprint
- **THEN** the start screen shows the empty state

### Requirement: Create action always available
The system SHALL keep a create-sprint action visible on the start screen at all times, both in the empty state and while scrolling a list.

#### Scenario: Create from empty state
- **WHEN** no sprints exist
- **THEN** the create action is visible and opens the create-sprint dialog

#### Scenario: Create while scrolled
- **WHEN** the user has scrolled a long sprint list
- **THEN** the create action remains visible

### Requirement: Create sprint by name
The system SHALL let the user create a sprint by entering a name in a dialog. Saving SHALL be possible only when the name contains non-whitespace characters, and the stored name SHALL have leading and trailing whitespace removed. No other field is requested.

#### Scenario: Valid name
- **WHEN** the user enters "  Sprint 42 " and saves
- **THEN** a sprint named "Sprint 42" is stored and the dialog closes

#### Scenario: Blank name
- **WHEN** the name field is empty or contains only spaces
- **THEN** the save action is disabled and nothing is stored

#### Scenario: Cancel
- **WHEN** the user dismisses or cancels the dialog
- **THEN** no sprint is stored

### Requirement: Creation time recorded automatically
The system SHALL record the moment a sprint is created without asking the user for it.

#### Scenario: Timestamp on save
- **WHEN** the user saves a new sprint
- **THEN** the stored sprint has a creation time equal to the time of saving

### Requirement: Create input survives interruptions
The system SHALL preserve an open create-sprint dialog and the name typed into it across screen rotation and across the app being stopped by the system in the background and restored.

#### Scenario: Rotation
- **WHEN** the user has typed "Sprint 4" in the open dialog and rotates the device
- **THEN** the dialog is still open with "Sprint 4" in the name field

#### Scenario: Process restored
- **WHEN** the system kills the app in the background while the dialog is open with text, and the user returns to it
- **THEN** the dialog is open with the same text

### Requirement: Open sprint details
The system SHALL open a details screen for a sprint when the user selects it in the list, showing the sprint's name.

#### Scenario: Select sprint
- **WHEN** the user taps sprint "Sprint 42" in the list
- **THEN** the sprint details screen for "Sprint 42" is shown

#### Scenario: Back to list
- **WHEN** the user navigates back from sprint details
- **THEN** the sprint list is shown

### Requirement: Empty sprint details
The system SHALL show an empty state on the sprint details screen when the sprint has no tasks.

#### Scenario: New sprint
- **WHEN** the user opens a sprint that has no tasks
- **THEN** an empty state indicating there are no tasks is shown

### Requirement: Delete sprint with confirmation
The system SHALL offer a delete action on the sprint details screen. Before deleting, it SHALL show a confirmation dialog warning that the sprint and all of its tasks will be permanently deleted. Deletion SHALL happen only when the user confirms.

#### Scenario: Confirm deletion
- **WHEN** the user taps delete and confirms in the dialog
- **THEN** the sprint and all of its tasks are removed from the device and the sprint list is shown without that sprint

#### Scenario: Cancel deletion
- **WHEN** the user taps delete and then cancels or dismisses the dialog
- **THEN** nothing is deleted and the sprint details screen remains

#### Scenario: Warning content
- **WHEN** the confirmation dialog is shown
- **THEN** it states that the sprint and all of its tasks will be deleted and that this cannot be undone

### Requirement: Local persistence
The system SHALL store sprints only on the device and keep them across app restarts.

#### Scenario: Restart
- **WHEN** the user creates a sprint, closes the app completely and reopens it
- **THEN** the sprint is still listed
