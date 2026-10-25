package com.planningpoker.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.dao.TaskDao
import com.planningpoker.core.database.model.SprintEntity
import com.planningpoker.core.database.model.TaskEntity
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: PlanningPokerDatabase
    private lateinit var sprintDao: SprintDao
    private lateinit var taskDao: TaskDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PlanningPokerDatabase::class.java,
        ).build()
        sprintDao = database.sprintDao()
        taskDao = database.taskDao()
    }

    @After
    fun tearDown() = database.close()

    private fun task(sprintId: Long, title: String, evaluatedAt: Long) = TaskEntity(
        sprintId = sprintId,
        title = title,
        description = "Description of $title",
        userVote = Vote.THREE,
        aiVote = Vote.EIGHT,
        risks = listOf("Risk A", "Risk \"B\""),
        evaluatedAt = evaluatedAt,
    )

    @Test
    fun tasksAreOrderedByEvaluationTimeNewestFirst() = runTest {
        val sprintId = sprintDao.insert(SprintEntity(name = "S", createdAt = 0))
        taskDao.insert(task(sprintId, "Old", 1_000))
        taskDao.insert(task(sprintId, "New", 3_000))
        taskDao.insert(task(sprintId, "Middle", 2_000))

        val titles = taskDao.observeTasks(sprintId).first().map { it.title }

        assertEquals(listOf("New", "Middle", "Old"), titles)
    }

    @Test
    fun onlyTheSprintsTasksAreReturned() = runTest {
        val sprintA = sprintDao.insert(SprintEntity(name = "A", createdAt = 0))
        val sprintB = sprintDao.insert(SprintEntity(name = "B", createdAt = 0))
        taskDao.insert(task(sprintA, "In A", 1_000))
        taskDao.insert(task(sprintB, "In B", 1_000))

        assertEquals(listOf("In A"), taskDao.observeTasks(sprintA).first().map { it.title })
    }

    @Test
    fun allFieldsRoundTrip() = runTest {
        val sprintId = sprintDao.insert(SprintEntity(name = "S", createdAt = 0))
        val id = taskDao.insert(task(sprintId, "Login", 1_234))

        assertEquals(task(sprintId, "Login", 1_234).copy(id = id), taskDao.observeTask(id).first())
    }

    @Test
    fun emptyRisksRoundTrip() = runTest {
        val sprintId = sprintDao.insert(SprintEntity(name = "S", createdAt = 0))
        val id = taskDao.insert(task(sprintId, "No risks", 1).copy(risks = emptyList()))

        assertEquals(emptyList<String>(), taskDao.getTask(id)!!.risks)
    }

    @Test
    fun updateReplacesAllFields() = runTest {
        val sprintId = sprintDao.insert(SprintEntity(name = "S", createdAt = 0))
        val id = taskDao.insert(task(sprintId, "Login", 1_000))
        val updated = TaskEntity(
            id = id,
            sprintId = sprintId,
            title = "Biometric login",
            description = "Login with fingerprint",
            userVote = Vote.UNKNOWN,
            aiVote = Vote.THIRTEEN,
            risks = listOf("Device support"),
            evaluatedAt = 5_000,
        )

        taskDao.update(updated)

        assertEquals(updated, taskDao.getTask(id))
    }

    @Test
    fun deletingSprintDeletesItsTasks() = runTest {
        val sprintId = sprintDao.insert(SprintEntity(name = "S", createdAt = 0))
        val otherSprintId = sprintDao.insert(SprintEntity(name = "Other", createdAt = 0))
        val taskId = taskDao.insert(task(sprintId, "Login", 1_000))
        val otherTaskId = taskDao.insert(task(otherSprintId, "Kept", 1_000))

        sprintDao.delete(sprintId)

        assertNull(taskDao.getTask(taskId))
        assertEquals(emptyList<TaskEntity>(), taskDao.observeTasks(sprintId).first())
        assertEquals("Kept", taskDao.getTask(otherTaskId)!!.title)
    }
}
