# Spec Delta

## MODIFIED Requirements

### Requirement: Evaluation input
The agent SHALL receive only the task description, the device language and the voting scale of the task's sprint. It MUST NOT receive the user's vote, any other sprint data such as its name, other tasks or any previous evaluation.

#### Scenario: User vote withheld
- **WHEN** the user evaluates a task with vote 3
- **THEN** the request sent to the AI contains the description, language and scale and does not contain the value 3 as a vote or any other sprint data

#### Scenario: Re-evaluation is independent
- **WHEN** a saved task with AI vote 8 is re-evaluated
- **THEN** the request does not include the previous AI vote, title or risks

#### Scenario: Scale of the sprint
- **WHEN** a task in a T-shirt sizes sprint is evaluated or re-evaluated while the default voting scale in Settings is Fibonacci
- **THEN** the request carries the T-shirt sizes scale

### Requirement: Tech Lead role
The agent SHALL act as a senior Tech Lead estimating relative complexity, uncertainty and effort of the described work on the requested voting scale, as story points for Fibonacci or as relative sizes for T-shirt sizes, never as calendar time.

#### Scenario: Points not time
- **WHEN** a task is evaluated on the Fibonacci scale
- **THEN** the result expresses the estimate as a story point value and the risks describe technical or scope concerns, not dates

#### Scenario: Sizes not time
- **WHEN** a task is evaluated on the T-shirt sizes scale
- **THEN** the result expresses the estimate as a size and the risks describe technical or scope concerns, not dates

### Requirement: Allowed AI estimates
The agent's estimate SHALL be one of the cards of the requested scale other than "?": 0, 1, 2, 3, 5, 8, 13, 20 for Fibonacci, or XS, S, M, L, XL, XXL for T-shirt sizes. It MUST NOT be "?", a card of the other scale or any other value.

#### Scenario: Value on the scale
- **WHEN** an evaluation on the Fibonacci scale succeeds
- **THEN** the estimate is one of 0, 1, 2, 3, 5, 8, 13, 20

#### Scenario: Size on the scale
- **WHEN** an evaluation on the T-shirt sizes scale succeeds
- **THEN** the estimate is one of XS, S, M, L, XL, XXL

#### Scenario: Off-scale value rejected
- **WHEN** the AI response for a Fibonacci evaluation contains an estimate such as 4, 21, "M" or "?"
- **THEN** the evaluation is treated as failed and nothing is stored

#### Scenario: Off-scale size rejected
- **WHEN** the AI response for a T-shirt sizes evaluation contains an estimate such as "5", "XXXL" or "?"
- **THEN** the evaluation is treated as failed and nothing is stored
