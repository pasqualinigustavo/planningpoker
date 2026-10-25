# Spec Delta

## MODIFIED Requirements

### Requirement: Create sprint by name
The system SHALL let the user create a sprint by entering a name and choosing a voting scale in a dialog. The voting scale SHALL be exactly one of Fibonacci or T-shirt sizes and SHALL be pre-selected with the default voting scale from Settings. Saving SHALL be possible only when the name contains non-whitespace characters, and the stored name SHALL have leading and trailing whitespace removed. No other field is requested.

#### Scenario: Valid name
- **WHEN** the user enters "  Sprint 42 " and saves
- **THEN** a sprint named "Sprint 42" is stored and the dialog closes

#### Scenario: Blank name
- **WHEN** the name field is empty or contains only spaces
- **THEN** the save action is disabled and nothing is stored

#### Scenario: Cancel
- **WHEN** the user dismisses or cancels the dialog
- **THEN** no sprint is stored

#### Scenario: Scale pre-filled from Settings
- **WHEN** the default voting scale in Settings is T-shirt sizes and the user opens the create-sprint dialog
- **THEN** T-shirt sizes is selected in the dialog

#### Scenario: Scale chosen in the dialog
- **WHEN** the default voting scale is Fibonacci, the user selects T-shirt sizes in the dialog, enters "Sprint 7" and saves
- **THEN** sprint "Sprint 7" is stored with the T-shirt sizes scale and the default voting scale in Settings is still Fibonacci

### Requirement: Create input survives interruptions
The system SHALL preserve an open create-sprint dialog, the name typed into it and the voting scale selected in it across screen rotation and across the app being stopped by the system in the background and restored.

#### Scenario: Rotation
- **WHEN** the user has typed "Sprint 4" and selected T-shirt sizes in the open dialog and rotates the device
- **THEN** the dialog is still open with "Sprint 4" in the name field and T-shirt sizes selected

#### Scenario: Process restored
- **WHEN** the system kills the app in the background while the dialog is open with text and a selected scale, and the user returns to it
- **THEN** the dialog is open with the same text and the same scale selected

### Requirement: Open sprint details
The system SHALL open a details screen for a sprint when the user selects it in the list, showing the sprint's name and voting scale.

#### Scenario: Select sprint
- **WHEN** the user taps sprint "Sprint 42" in the list
- **THEN** the sprint details screen for "Sprint 42" is shown

#### Scenario: Scale shown
- **WHEN** the user opens a sprint created with T-shirt sizes
- **THEN** the sprint details screen indicates that the sprint uses T-shirt sizes

#### Scenario: Back to list
- **WHEN** the user navigates back from sprint details
- **THEN** the sprint list is shown

## ADDED Requirements

### Requirement: Voting scale fixed after creation
The system SHALL keep a sprint's voting scale as chosen at creation. It MUST NOT offer a way to change the scale of an existing sprint.

#### Scenario: No scale editing
- **WHEN** the user opens the details of an existing sprint
- **THEN** the voting scale is shown but no action to change it is offered

### Requirement: Sprints from before voting scales use Fibonacci
Sprints stored before voting scales were introduced SHALL use the Fibonacci scale after the app is updated, and their tasks SHALL keep their titles, descriptions, votes, risks and evaluation times.

#### Scenario: Upgrade with existing data
- **WHEN** the app is updated on a device with sprint "Old" holding a task with user vote 5 and AI vote 8
- **THEN** sprint "Old" uses Fibonacci and the task still shows user vote 5 and AI vote 8
