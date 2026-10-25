package com.planningpoker.feature.sprints.testing

import com.planningpoker.core.data.repository.TaskRepository
import com.planningpoker.core.data.repository.TaskSaveOutcome
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Read side only: sprint details lists tasks but never evaluates them. */
class FakeTaskRepository(initial: List<Task> = emptyList()) : TaskRepository {

    val tasks = MutableStateFlow(initial)

    override fun observeTasks(sprintId: Long): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.sprintId == sprintId } }

    override fun observeTask(id: Long): Flow<Task?> = tasks.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun evaluateAndCreate(sprintId: Long, description: String, userVote: Vote): TaskSaveOutcome =
        error("Not used by sprint details")

    override suspend fun reevaluate(taskId: Long, description: String, userVote: Vote): TaskSaveOutcome =
        error("Not used by sprint details")
}
