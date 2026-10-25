package com.planningpoker.core.model

/** Outcome of asking the Tech Lead agent to evaluate a task. */
sealed interface TaskEvaluation {
    data class Success(val result: TaskEvaluationResult) : TaskEvaluation
    data class Failure(val error: EvaluationError) : TaskEvaluation
}

enum class EvaluationError {
    /** No connection or the request could not reach the service. */
    Network,

    /** No answer within the time limit. */
    Timeout,

    /** The answer was missing fields, off the scale or unreadable. */
    InvalidResponse,

    /** The service refused the request: App Check, quota or safety filters. */
    Rejected,

    Unknown,
}
