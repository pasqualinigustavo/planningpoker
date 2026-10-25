# Spec Delta

## Purpose

Lets the user adjust how the app looks and how new sprints start: the theme the whole app uses and the voting scale suggested when creating a sprint, kept on the device across restarts.

## ADDED Requirements

### Requirement: Open settings
The system SHALL offer a settings action in the top bar of the sprint list that opens the Settings screen. Navigating back from Settings SHALL return to the sprint list.

#### Scenario: Open from sprint list
- **WHEN** the user taps the settings action on the sprint list
- **THEN** the Settings screen is shown

#### Scenario: Back to sprint list
- **WHEN** the user navigates back from Settings
- **THEN** the sprint list is shown

### Requirement: Theme choice
The system SHALL let the user choose the theme as exactly one of System default, Light or Dark. System default SHALL be selected until the user chooses otherwise.

#### Scenario: First launch
- **WHEN** the user opens Settings without having changed the theme before
- **THEN** System default is selected

#### Scenario: Choose dark
- **WHEN** the user selects Dark
- **THEN** Dark is the only selected theme option

### Requirement: Theme applied immediately
The system SHALL apply a newly chosen theme to the whole app as soon as it is selected, without restarting the app or leaving the screen. System default SHALL follow the device's dark mode setting, including when it changes while the app is open.

#### Scenario: Switch to light
- **WHEN** the device is in dark mode and the user selects Light
- **THEN** the Settings screen and every other screen use the light appearance right away

#### Scenario: Follow the system
- **WHEN** System default is selected and the user turns the device's dark mode on
- **THEN** the app switches to the dark appearance

### Requirement: Theme kept across restarts
The system SHALL keep the chosen theme across app restarts and SHALL show the app in that theme from its first frame after launch, never briefly in another theme.

#### Scenario: Restart with light theme
- **WHEN** the user selected Light with the device in dark mode, closes the app and opens it again
- **THEN** the first screen shown is in the light appearance, with no dark frame before it

### Requirement: Default voting scale
The system SHALL let the user choose the default voting scale as exactly one of Fibonacci (0, 1, 2, 3, 5, 8, 13, 20, ?) or T-shirt sizes (XS, S, M, L, XL, XXL, ?), showing the cards of each option. Fibonacci SHALL be selected until the user chooses otherwise. The choice SHALL be kept across app restarts.

#### Scenario: First launch
- **WHEN** the user opens Settings without having changed the voting scale before
- **THEN** Fibonacci is selected

#### Scenario: Choice kept
- **WHEN** the user selects T-shirt sizes, closes the app and opens Settings again
- **THEN** T-shirt sizes is selected

### Requirement: Default scale only affects new sprints
The default voting scale SHALL only be used to pre-fill the scale when creating a sprint. Changing it MUST NOT change the scale, tasks or votes of any existing sprint.

#### Scenario: Existing sprint unchanged
- **WHEN** sprint "A" uses Fibonacci and the user changes the default voting scale to T-shirt sizes
- **THEN** sprint "A" still uses Fibonacci and its tasks keep their votes

### Requirement: Settings stored on the device
The system SHALL store the settings only on the device.

#### Scenario: No account needed
- **WHEN** the user changes a setting without network connection
- **THEN** the change is applied and kept
