package com.planningpoker.core.ai

import android.util.Log
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds

internal val EVALUATION_TIMEOUT = 30.seconds

private const val TAG = "TechLeadAgent"

internal class GeminiTechLeadAgent @Inject constructor(
    private val model: TechLeadModel,
) : TechLeadAgent {

    // Wraps the answer so a null text is not mistaken for a timeout.
    private class Answer(val text: String?)

    override suspend fun evaluate(request: TaskEvaluationRequest): TaskEvaluation {
        val answer = try {
            withTimeoutOrNull(EVALUATION_TIMEOUT) { Answer(model.generate(buildUserPrompt(request))) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val error = e.toEvaluationError()
            Log.w(TAG, "Tech Lead evaluation failed: $error", e)
            return TaskEvaluation.Failure(error)
        }
        return if (answer == null) {
            Log.w(TAG, "Tech Lead evaluation failed: ${EvaluationError.Timeout} after $EVALUATION_TIMEOUT")
            TaskEvaluation.Failure(EvaluationError.Timeout)
        } else {
            TechLeadResponseParser.parse(answer.text)
        }
    }
}
