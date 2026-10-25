package com.planningpoker.core.ai

import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest
import com.planningpoker.core.model.TaskEvaluationResult
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TechLeadAgentTest {

    private class FakeTechLeadAgent(private val result: TaskEvaluation) : TechLeadAgent {
        val requests = mutableListOf<TaskEvaluationRequest>()

        override suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluation {
            requests += request
            return result
        }
    }

    /** Stand-in for a consumer that receives the agent through its constructor, as Hilt would. */
    private class Consumer(private val agent: TechLeadAgent) {
        suspend fun evaluate(description: String) =
            agent.evaluate(TaskEvaluationRequest(description = description, languageTag = "pt-BR"))
    }

    @Test
    fun consumerWorksWithFakeAgentWithoutFirebase() = runTest {
        val expected = TaskEvaluation.Success(
            TaskEvaluationResult(title = "Login", estimate = Vote.FIVE, risks = listOf("Scope")),
        )
        val agent = FakeTechLeadAgent(expected)

        val result = Consumer(agent).evaluate("Build the login screen")

        assertEquals(expected, result)
        assertEquals(
            listOf(TaskEvaluationRequest("Build the login screen", "pt-BR")),
            agent.requests,
        )
    }
}
