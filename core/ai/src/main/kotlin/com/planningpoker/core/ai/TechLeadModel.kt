package com.planningpoker.core.ai

/** The generative model behind the agent: a prompt in, the raw text answer out. */
internal fun interface TechLeadModel {
    suspend fun generate(prompt: String): String?
}
