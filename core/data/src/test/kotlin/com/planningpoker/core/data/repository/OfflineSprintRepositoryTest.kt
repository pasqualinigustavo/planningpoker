package com.planningpoker.core.data.repository

import com.planningpoker.core.data.testdoubles.FakeSprintDao
import com.planningpoker.core.model.Sprint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OfflineSprintRepositoryTest {

    private val now = Instant.parse("2026-10-25T10:15:30.123Z")
    private val dao = FakeSprintDao()
    private val repository = OfflineSprintRepository(dao, Clock.fixed(now, ZoneOffset.UTC))

    @Test
    fun createTrimsName() = runTest {
        repository.create("  Sprint 42 ")

        assertEquals("Sprint 42", dao.inserted.single().name)
    }

    @Test
    fun createRejectsBlankNames() = runTest {
        listOf("", "   ", "\t\n").forEach { blank ->
            val result = runCatching { repository.create(blank) }
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        }
        assertTrue(dao.inserted.isEmpty())
    }

    @Test
    fun createStampsCurrentTimeFromClock() = runTest {
        repository.create("Sprint 1")

        assertEquals(now.toEpochMilli(), dao.inserted.single().createdAt)
    }

    @Test
    fun entitiesAreMappedToModels() = runTest {
        val id = repository.create("Sprint 1")

        assertEquals(listOf(Sprint(id, "Sprint 1", now)), repository.observeSprints().first())
        assertEquals(Sprint(id, "Sprint 1", now), repository.observeSprint(id).first())
    }

    @Test
    fun deleteRemovesSprint() = runTest {
        val id = repository.create("Sprint 1")

        repository.delete(id)

        assertNull(repository.observeSprint(id).first())
    }
}
