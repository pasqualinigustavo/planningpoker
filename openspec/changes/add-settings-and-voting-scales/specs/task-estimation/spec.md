# Spec Delta

## MODIFIED Requirements

### Requirement: Vote scale
The system SHALL let the user select exactly one vote from the voting scale of the task's sprint: 0, 1, 2, 3, 5, 8, 13, 20 and "?" for Fibonacci, or XS, S, M, L, XL, XXL and "?" for T-shirt sizes. The same scale SHALL be offered for new tasks and for saved tasks, including when re-evaluating, regardless of the default voting scale in Settings. Selecting a different value SHALL replace the previous selection.

#### Scenario: Select a vote
- **WHEN** the user taps 5 and then 8 in a Fibonacci sprint
- **THEN** only 8 is selected

#### Scenario: Unknown vote
- **WHEN** the user taps "?"
- **THEN** "?" is selected as the user's vote

#### Scenario: T-shirt sprint
- **WHEN** the user opens the new-task screen in a sprint that uses T-shirt sizes
- **THEN** the cards XS, S, M, L, XL, XXL and "?" are offered and no numeric card is offered

#### Scenario: Settings changed after the sprint was created
- **WHEN** a sprint uses Fibonacci, the user changes the default voting scale in Settings to T-shirt sizes and then opens a saved task of that sprint
- **THEN** the Fibonacci cards are offered and the saved votes are shown as before
