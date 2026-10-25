package com.planningpoker.core.ai

import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest

/**
 * Independent AI evaluation of a task, acting as a senior Tech Lead.
 * The only entry point to generative AI in the app; callers never touch the Firebase SDK.
 * Never throws for evaluation problems: they are reported as [TaskEvaluation.Failure].
 */
interface TechLeadAgent {
    suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluation
}
