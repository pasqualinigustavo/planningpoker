package com.planningpoker.core.model

// Placeholder shape; `add-task-estimation` replaces `estimate` with the vote scale type.
data class TaskEvaluationResult(
    val title: String,
    val estimate: Int,
    val risks: List<String>,
)
