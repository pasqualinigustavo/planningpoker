package com.planningpoker.core.ai

import com.planningpoker.core.model.AI_VOTES
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationResult
import com.planningpoker.core.model.Vote
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Reads the model's JSON answer. Anything off-contract is a failure, never a partial result. */
internal object TechLeadResponseParser {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class RawEvaluation(val title: String, val estimate: String, val risks: List<String>)

    fun parse(text: String?): TaskEvaluation {
        val raw = text?.let { runCatching { json.decodeFromString<RawEvaluation>(it) }.getOrNull() }
            ?: return invalid()

        val title = raw.title.trim()
        val estimate = Vote.fromLabel(raw.estimate.trim())?.takeIf { it in AI_VOTES }
        val risks = raw.risks.map(String::trim)

        if (title.isEmpty() || estimate == null || risks.any(String::isEmpty)) return invalid()

        return TaskEvaluation.Success(TaskEvaluationResult(title, estimate, risks))
    }

    private fun invalid() = TaskEvaluation.Failure(EvaluationError.InvalidResponse)
}
