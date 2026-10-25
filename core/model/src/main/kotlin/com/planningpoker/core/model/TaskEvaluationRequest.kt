package com.planningpoker.core.model

/**
 * What the Tech Lead agent receives. Deliberately has no field for the user's vote or any
 * sprint data, so neither can be sent to the AI by mistake.
 */
data class TaskEvaluationRequest(
    val description: String,
    /** BCP 47 tag of the device language the AI must answer in, e.g. `pt-BR`. */
    val languageTag: String,
)
