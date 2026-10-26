package com.planningpoker.core.ai

import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationResult
import com.planningpoker.core.model.Vote
import org.junit.Assert.assertEquals
import org.junit.Test

class TechLeadResponseParserTest {

    private val invalid = TaskEvaluation.Failure(EvaluationError.InvalidResponse)

    private fun json(
        title: String = "\"Biometric login\"",
        estimate: String = "\"8\"",
        risks: String = "[\"Device fragmentation\", \"Fallback flow\"]",
    ) = """{"title": $title, "estimate": $estimate, "risks": $risks}"""

    @Test
    fun validResponse() {
        assertEquals(
            TaskEvaluation.Success(
                TaskEvaluationResult(
                    title = "Biometric login",
                    estimate = Vote.EIGHT,
                    risks = listOf("Device fragmentation", "Fallback flow"),
                ),
            ),
            TechLeadResponseParser.parse(json()),
        )
    }

    @Test
    fun everyAiCardIsAccepted() {
        listOf("0", "1", "2", "3", "5", "8", "13", "20").forEach { label ->
            val result = TechLeadResponseParser.parse(json(estimate = "\"$label\"")) as TaskEvaluation.Success
            assertEquals(label, result.result.estimate.label)
        }
    }

    @Test
    fun offScaleEstimatesAreRejected() {
        listOf("\"4\"", "\"21\"", "\"?\"", "\"\"", "\"five\"").forEach { estimate ->
            assertEquals(estimate, invalid, TechLeadResponseParser.parse(json(estimate = estimate)))
        }
    }

    @Test
    fun blankTitleIsRejected() {
        assertEquals(invalid, TechLeadResponseParser.parse(json(title = "\"   \"")))
    }

    @Test
    fun blankRiskIsRejected() {
        assertEquals(invalid, TechLeadResponseParser.parse(json(risks = "[\"Real risk\", \"  \"]")))
    }

    @Test
    fun emptyRiskListIsValid() {
        val result = TechLeadResponseParser.parse(json(risks = "[]")) as TaskEvaluation.Success
        assertEquals(emptyList<String>(), result.result.risks)
    }

    @Test
    fun valuesAreTrimmed() {
        val result = TechLeadResponseParser.parse(
            json(title = "\"  Login  \"", estimate = "\" 5 \"", risks = "[\"  Scope  \"]"),
        ) as TaskEvaluation.Success
        assertEquals(TaskEvaluationResult("Login", Vote.FIVE, listOf("Scope")), result.result)
    }

    @Test
    fun missingFieldIsRejected() {
        assertEquals(invalid, TechLeadResponseParser.parse("""{"title": "Login", "estimate": "5"}"""))
    }

    @Test
    fun unknownFieldsAreIgnored() {
        val withExtra = json().dropLast(1) + """, "confidence": "high"}"""
        assert(TechLeadResponseParser.parse(withExtra) is TaskEvaluation.Success)
    }

    @Test
    fun malformedOrMissingTextIsRejected() {
        listOf(null, "", "not json", "{", "[]", "```json\n{}\n```").forEach { text ->
            assertEquals(text.toString(), invalid, TechLeadResponseParser.parse(text))
        }
    }
}
