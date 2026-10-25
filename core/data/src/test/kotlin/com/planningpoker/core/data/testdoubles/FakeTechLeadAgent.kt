package com.planningpoker.core.data.testdoubles

import com.planningpoker.core.ai.TechLeadAgent
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest

class FakeTechLeadAgent(var nextEvaluation: TaskEvaluation) : TechLeadAgent {

    val requests = mutableListOf<TaskEvaluationRequest>()

    override suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluation {
        requests += request
        return nextEvaluation
    }
}
