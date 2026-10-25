package com.planningpoker.core.model

/** A planning poker card. [UNKNOWN] ("?") is a user-only card for "I don't know yet". */
enum class Vote(val label: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FIVE("5"),
    EIGHT("8"),
    THIRTEEN("13"),
    TWENTY("20"),
    UNKNOWN("?"),
    ;

    companion object {
        fun fromLabel(label: String): Vote? = entries.firstOrNull { it.label == label }
    }
}

/** The cards the Tech Lead agent may answer with: the scale without "?". */
val AI_VOTES: List<Vote> = Vote.entries - Vote.UNKNOWN
