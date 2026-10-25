package com.planningpoker.core.model

/** A successful Tech Lead evaluation. [estimate] is always one of [AI_VOTES]. */
data class TaskEvaluationResult(
    val title: String,
    val estimate: Vote,
    val risks: List<String>,
)
