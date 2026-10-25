package com.planningpoker.core.data.repository

import com.planningpoker.core.ai.TechLeadAgent
import com.planningpoker.core.database.dao.TaskDao
import com.planningpoker.core.database.model.TaskEntity
import com.planningpoker.core.database.model.asExternalModel
import com.planningpoker.core.model.EvaluationError
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.TaskEvaluation
import com.planningpoker.core.model.TaskEvaluationRequest
import com.planningpoker.core.model.TaskEvaluationResult
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class OfflineFirstTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val techLeadAgent: TechLeadAgent,
    private val deviceLanguage: DeviceLanguage,
    private val clock: Clock,
) : TaskRepository {

    override fun observeTasks(sprintId: Long): Flow<List<Task>> =
        taskDao.observeTasks(sprintId).map { tasks -> tasks.map(TaskEntity::asExternalModel) }

    override fun observeTask(id: Long): Flow<Task?> =
        taskDao.observeTask(id).map { it?.asExternalModel() }

    override suspend fun evaluateAndCreate(
        sprintId: Long,
        description: String,
        userVote: Vote,
    ): TaskSaveOutcome {
        val trimmed = description.trim()
        require(trimmed.isNotEmpty()) { "Task description must not be blank" }
        return evaluate(trimmed) { result ->
            taskDao.insert(entity(id = 0, sprintId, trimmed, userVote, result))
        }
    }

    override suspend fun reevaluate(taskId: Long, description: String, userVote: Vote): TaskSaveOutcome {
        val trimmed = description.trim()
        require(trimmed.isNotEmpty()) { "Task description must not be blank" }
        val existing = taskDao.getTask(taskId) ?: return TaskSaveOutcome.Failed(EvaluationError.Unknown)
        return evaluate(trimmed) { result ->
            taskDao.update(entity(id = taskId, existing.sprintId, trimmed, userVote, result))
            taskId
        }
    }

    private suspend fun evaluate(
        description: String,
        save: suspend (TaskEvaluationResult) -> Long,
    ): TaskSaveOutcome {
        val request = TaskEvaluationRequest(description = description, languageTag = deviceLanguage.tag())
        return when (val evaluation = techLeadAgent.evaluate(request)) {
            is TaskEvaluation.Success -> TaskSaveOutcome.Saved(save(evaluation.result))
            is TaskEvaluation.Failure -> TaskSaveOutcome.Failed(evaluation.error)
        }
    }

    private fun entity(
        id: Long,
        sprintId: Long,
        description: String,
        userVote: Vote,
        result: TaskEvaluationResult,
    ) = TaskEntity(
        id = id,
        sprintId = sprintId,
        title = result.title,
        description = description,
        userVote = userVote,
        aiVote = result.estimate,
        risks = result.risks,
        evaluatedAt = clock.millis(),
    )
}
