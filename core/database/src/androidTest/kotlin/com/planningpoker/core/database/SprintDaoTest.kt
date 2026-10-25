package com.planningpoker.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.model.SprintEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SprintDaoTest {

    private lateinit var database: PlanningPokerDatabase
    private lateinit var sprintDao: SprintDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlanningPokerDatabase::class.java,
        ).build()
        sprintDao = database.sprintDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun sprintsAreOrderedNewestFirst() = runTest {
        sprintDao.insert(SprintEntity(name = "Old", createdAt = 1_000))
        sprintDao.insert(SprintEntity(name = "New", createdAt = 3_000))
        sprintDao.insert(SprintEntity(name = "Middle", createdAt = 2_000))

        val names = sprintDao.observeSprints().first().map { it.name }

        assertEquals(listOf("New", "Middle", "Old"), names)
    }

    @Test
    fun sameCreationTimeIsOrderedByIdDescending() = runTest {
        val firstId = sprintDao.insert(SprintEntity(name = "First", createdAt = 1_000))
        val secondId = sprintDao.insert(SprintEntity(name = "Second", createdAt = 1_000))

        val ids = sprintDao.observeSprints().first().map { it.id }

        assertEquals(listOf(secondId, firstId), ids)
    }

    @Test
    fun deleteRemovesOnlyThatSprint() = runTest {
        val keepId = sprintDao.insert(SprintEntity(name = "Keep", createdAt = 1_000))
        val deleteId = sprintDao.insert(SprintEntity(name = "Delete", createdAt = 2_000))

        sprintDao.delete(deleteId)

        assertEquals(listOf(keepId), sprintDao.observeSprints().first().map { it.id })
        assertNull(sprintDao.observeSprint(deleteId).first())
    }

    @Test
    fun observeSprintReturnsStoredValues() = runTest {
        val id = sprintDao.insert(SprintEntity(name = "Sprint 42", createdAt = 1_234))

        assertEquals(
            SprintEntity(id = id, name = "Sprint 42", createdAt = 1_234),
            sprintDao.observeSprint(id).first(),
        )
    }
}
