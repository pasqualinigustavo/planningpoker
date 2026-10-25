package com.planningpoker.core.ai

import com.planningpoker.core.model.AI_VOTES
import com.planningpoker.core.model.TaskEvaluationRequest

/**
 * Role only. The response shape is enforced by the response schema, not by this text.
 */
internal val TECH_LEAD_INSTRUCTION = """
    You are a senior Tech Lead estimating work with your team in a planning poker session.
    For each task you receive, estimate its relative complexity, uncertainty and effort in story
    points, never in calendar time. Use only these cards: ${AI_VOTES.joinToString { it.label }}.
    Choose the largest card when the task is too big or too unclear and should be split.
    Give the task a short, specific title of at most eight words.
    List the concrete technical and scope risks that could make the estimate wrong, such as
    unknowns, dependencies, edge cases and integration points, each as one short sentence.
    Return an empty list only if there are genuinely no risks.
    Do not mention dates or deadlines. Do not ask questions: when information is missing,
    state your assumption as a risk.
    Write the title and the risks in the language requested with the task.
""".trimIndent()

/**
 * The per-task message. Contains only the description and the response language: never the
 * user's vote, sprint data or a previous evaluation.
 */
internal fun buildUserPrompt(request: TaskEvaluationRequest): String = """
    Respond in the language with BCP 47 tag "${request.languageTag}".

    Task description:
    ${request.description.trim()}
""".trimIndent()
