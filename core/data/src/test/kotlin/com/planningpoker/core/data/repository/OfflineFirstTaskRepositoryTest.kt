package com.planningpoker.core.data.repository

import com.planningpoker.core.data.testdoubles.FakeTaskDao
import com.planningpoker.core.data.testdoubles.FakeTechLeadAgent
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest
import com.planningpoker.core.model.TaskEvaluationResult
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class OfflineFirstTaskRepositoryTest {

    private val firstEvaluation = TaskEvaluationResult("Biometric login", Vote.EIGHT, listOf("Device support"))
    private val secondEvaluation = TaskEvaluationResult("Fingerprint login", Vote.THIRTEEN, listOf("Fallback", "Security review"))

    private var now = Instant.parse("2026-10-25T10:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = now
    }

    private val dao = FakeTaskDao()
    private val agent = FakeTechLeadAgent(TaskEvaluation.Success(firstEvaluation))
    private val repository = OfflineFirstTaskRepository(dao, agent, { "pt-BR" }, clock)

    @Test
    fun successfulEvaluationStoresAllFields() = runTest {
        val outcome = repository.evaluateAndCreate(sprintId = 7, description = "  Add biometric login ", userVote = Vote.FIVE)

        val id = (outcome as TaskSaveOutcome.Saved).taskId
        assertEquals(
            Task(
                id = id,
                sprintId = 7,
                title = "Biometric login",
                description = "Add biometric login",
                userVote = Vote.FIVE,
                aiVote = Vote.EIGHT,
                risks = listOf("Device support"),
                evaluatedAt = now,
            ),
            repository.observeTask(id).first(),
        )
    }

    @Test
    fun agentReceivesDescriptionAndDeviceLanguageOnly() = runTest {
        repository.evaluateAndCreate(sprintId = 7, description = "Add biometric login", userVote = Vote.FIVE)

        assertEquals(listOf(TaskEvaluationRequest("Add biometric login", "pt-BR")), agent.requests)
    }

    @Test
    fun failedEvaluationStoresNothing() = runTest {
        agent.nextEvaluation = TaskEvaluation.Failure(EvaluationError.Network)

        val outcome = repository.evaluateAndCreate(sprintId = 7, description = "Login", userVote = Vote.FIVE)

        assertEquals(TaskSaveOutcome.Failed(EvaluationError.Network), outcome)
        assertTrue(dao.stored.isEmpty())
    }

    @Test
    fun successfulReevaluationOverwritesAllFieldsAndTimestamp() = runTest {
        val id = (repository.evaluateAndCreate(7, "Login", Vote.FIVE) as TaskSaveOutcome.Saved).taskId
        agent.nextEvaluation = TaskEvaluation.Success(secondEvaluation)
        now = now.plusSeconds(3_600)

        val outcome = repository.reevaluate(id, "Login with fingerprint and fallback", Vote.UNKNOWN)

        assertEquals(TaskSaveOutcome.Saved(id), outcome)
        assertEquals(
            Task(
                id = id,
                sprintId = 7,
                title = "Fingerprint login",
                description = "Login with fingerprint and fallback",
                userVote = Vote.UNKNOWN,
                aiVote = Vote.THIRTEEN,
                risks = listOf("Fallback", "Security review"),
                evaluatedAt = now,
            ),
            repository.observeTask(id).first(),
        )
        assertEquals(1, dao.stored.size)
    }

    @Test
    fun failedReevaluationLeavesTaskUnchanged() = runTest {
        val id = (repository.evaluateAndCreate(7, "Login", Vote.FIVE) as TaskSaveOutcome.Saved).taskId
        val before = repository.observeTask(id).first()
        agent.nextEvaluation = TaskEvaluation.Failure(EvaluationError.Timeout)
        now = now.plusSeconds(3_600)

        val outcome = repository.reevaluate(id, "Completely different", Vote.TWENTY)

        assertEquals(TaskSaveOutcome.Failed(EvaluationError.Timeout), outcome)
        assertEquals(before, repository.observeTask(id).first())
    }

    @Test
    fun blankDescriptionIsRejectedWithoutCallingTheAgent() = runTest {
        val result = runCatching { repository.evaluateAndCreate(7, "   ", Vote.FIVE) }

        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertTrue(agent.requests.isEmpty())
    }
}
