package com.planningpoker.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VoteTest {

    @Test
    fun scaleLabelsInOrder() {
        assertEquals(
            listOf("0", "1", "2", "3", "5", "8", "13", "20", "?"),
            Vote.entries.map { it.label },
        )
    }

    @Test
    fun aiVotesExcludeUnknown() {
        assertEquals(
            listOf("0", "1", "2", "3", "5", "8", "13", "20"),
            AI_VOTES.map { it.label },
        )
    }

    @Test
    fun fromLabelParsesEveryCard() {
        Vote.entries.forEach { vote -> assertEquals(vote, Vote.fromLabel(vote.label)) }
    }

    @Test
    fun fromLabelRejectsOffScaleValues() {
        listOf("4", "21", "", "1/2", "coffee").forEach { assertNull(Vote.fromLabel(it)) }
    }
}
