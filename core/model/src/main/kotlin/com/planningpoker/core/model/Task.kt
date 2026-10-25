package com.planningpoker.core.model

import java.time.Instant

data class Task(
    val id: Long,
    val sprintId: Long,
    val title: String,
    val description: String,
    val userVote: Vote,
    val aiVote: Vote,
    val risks: List<String>,
    /** When the stored evaluation was produced; updated on every re-evaluation. */
    val evaluatedAt: Instant,
)
