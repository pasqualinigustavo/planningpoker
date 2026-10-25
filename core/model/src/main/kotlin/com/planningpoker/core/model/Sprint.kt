package com.planningpoker.core.model

import java.time.Instant

data class Sprint(
    val id: Long,
    val name: String,
    val createdAt: Instant,
)
