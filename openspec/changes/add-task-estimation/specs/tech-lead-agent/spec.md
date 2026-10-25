# Spec Delta

## Purpose

Defines the contract of the AI Tech Lead that independently evaluates a task: what it receives, what it must return, the values it may use, the language it answers in, how failures are reported and who may call it.

## ADDED Requirements

### Requirement: Evaluation input
The agent SHALL receive only the task description and the device language. It MUST NOT receive the user's vote, the sprint, other tasks or any previous evaluation.

#### Scenario: User vote withheld
- **WHEN** the user evaluates a task with vote 3
- **THEN** the request sent to the AI contains the description and language and does not contain the value 3 as a vote or any sprint data

#### Scenario: Re-evaluation is independent
- **WHEN** a saved task with AI vote 8 is re-evaluated
- **THEN** the request does not include the previous AI vote, title or risks

### Requirement: Tech Lead role
The agent SHALL act as a senior Tech Lead estimating relative complexity, uncertainty and effort of the described work in story points, not calendar time.

#### Scenario: Points not time
- **WHEN** a task is evaluated
- **THEN** the result expresses the estimate as a story point value and the risks describe technical or scope concerns, not dates

### Requirement: Structured result
The agent SHALL return a result with a non-blank title summarizing the task, exactly one estimate, and a list of risks where each risk is a non-blank text. The list MAY be empty.

#### Scenario: Complete result
- **WHEN** the evaluation succeeds
- **THEN** the result has a title, one estimate and a list of zero or more risks

### Requirement: Allowed AI estimates
The agent's estimate SHALL be one of 0, 1, 2, 3, 5, 8, 13, 20. It MUST NOT be "?" or any other value.

#### Scenario: Value on the scale
- **WHEN** the evaluation succeeds
- **THEN** the estimate is one of 0, 1, 2, 3, 5, 8, 13, 20

#### Scenario: Off-scale value rejected
- **WHEN** the AI response contains an estimate such as 4, 21 or "?"
- **THEN** the evaluation is treated as failed and nothing is stored

### Requirement: Response language
The agent SHALL write the title and risks in the device language.

#### Scenario: Portuguese device
- **WHEN** the device language is Portuguese (Brazil) and a task is evaluated
- **THEN** the title and risks are in Portuguese

### Requirement: Invalid responses are failures
The system SHALL treat a response that is missing fields, has a blank title or blank risk, or cannot be read as a failed evaluation, and MUST NOT store any part of it.

#### Scenario: Malformed response
- **WHEN** the AI returns a response that cannot be read as the expected structure
- **THEN** the evaluation fails and the task is neither created nor changed

### Requirement: Bounded evaluation time
The system SHALL fail an evaluation that has not completed within 30 seconds.

#### Scenario: Timeout
- **WHEN** the AI does not respond within 30 seconds
- **THEN** the evaluation fails as a timeout and the user can retry

### Requirement: Only genuine app instances
The AI service SHALL accept evaluation requests only from verified, unmodified installs of the app. Requests without valid app attestation MUST be rejected.

#### Scenario: Unattested client
- **WHEN** a request is made with the app's API key but without a valid app attestation token
- **THEN** the AI service rejects it

#### Scenario: Genuine release build
- **WHEN** the release app from a verified install evaluates a task
- **THEN** the request is accepted
