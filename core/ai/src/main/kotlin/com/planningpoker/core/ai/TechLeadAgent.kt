package com.planningpoker.core.ai

import com.planningpoker.core.model.TaskEvaluationRequest
import com.planningpoker.core.model.TaskEvaluationResult

/**
 * Independent AI evaluation of a task, acting as a senior Tech Lead.
 * The only entry point to generative AI in the app; callers never touch the Firebase SDK.
 */
interface TechLeadAgent {
    suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluationResult
}
