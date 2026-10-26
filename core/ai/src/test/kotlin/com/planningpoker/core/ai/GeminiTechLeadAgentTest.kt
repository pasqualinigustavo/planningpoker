package com.planningpoker.core.ai

import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest
import com.planningpoker.core.model.Vote
import java.io.IOException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiTechLeadAgentTest {

    private val request = TaskEvaluationRequest(
        description = "  Add biometric login to the sign-in screen  ",
        languageTag = "pt-BR",
    )
    private val validAnswer = """{"title": "Login biométrico", "estimate": "8", "risks": ["Fragmentação"]}"""

    private val prompts = mutableListOf<String>()

    private fun agent(answer: suspend (String) -> String?) = GeminiTechLeadAgent { prompt ->
        prompts += prompt
        answer(prompt)
    }

    @Test
    fun successfulEvaluation() = runTest {
        val result = agent { validAnswer }.evaluate(request) as TaskEvaluation.Success

        assertEquals("Login biométrico", result.result.title)
        assertEquals(Vote.EIGHT, result.result.estimate)
    }

    @Test
    fun promptContainsOnlyDescriptionAndLanguage() = runTest {
        agent { validAnswer }.evaluate(request)

        assertEquals(
            """
            Respond in the language with BCP 47 tag "pt-BR".

            Task description:
            Add biometric login to the sign-in screen
            """.trimIndent(),
            prompts.single(),
        )
    }

    @Test
    fun promptCarriesNoVoteSprintOrPreviousEvaluation() = runTest {
        // The request type has no fields for these; the prompt must not invent them either.
        agent { validAnswer }.evaluate(request)

        val prompt = prompts.single().lowercase()
        listOf("vote", "sprint", "estimate", "previous", "title", "risk").forEach { word ->
            assertFalse("prompt mentions '$word'", word in prompt)
        }
    }

    @Test
    fun noAnswerWithin30SecondsIsTimeout() = runTest {
        val result = agent {
            delay(31.seconds)
            validAnswer
        }.evaluate(request)

        assertEquals(TaskEvaluation.Failure(EvaluationError.Timeout), result)
    }

    @Test
    fun answerJustInTimeIsAccepted() = runTest {
        val result = agent {
            delay(29.seconds)
            validAnswer
        }.evaluate(request)

        assertTrue(result is TaskEvaluation.Success)
    }

    @Test
    fun nullTextIsInvalidResponseNotTimeout() = runTest {
        assertEquals(
            TaskEvaluation.Failure(EvaluationError.InvalidResponse),
            agent { null }.evaluate(request),
        )
    }

    @Test
    fun exceptionsAreMapped() = runTest {
        assertEquals(
            TaskEvaluation.Failure(EvaluationError.Network),
            agent { throw IOException("offline") }.evaluate(request),
        )
        assertEquals(
            TaskEvaluation.Failure(EvaluationError.Rejected),
            agent { throw FirebaseAIExceptions.quotaExceeded("quota") }.evaluate(request),
        )
    }

    @Test
    fun callerCancellationIsNotSwallowed() = runTest {
        val evaluation = async { agent { awaitCancellation() }.evaluate(request) }
        delay(1.seconds)

        evaluation.cancel()

        assertTrue(runCatching { evaluation.await() }.exceptionOrNull() is CancellationException)
    }
}
