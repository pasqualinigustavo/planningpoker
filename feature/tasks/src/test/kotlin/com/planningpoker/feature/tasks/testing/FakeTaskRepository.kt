package com.planningpoker.feature.tasks.testing

import com.planningpoker.core.data.repository.TaskRepository
import com.planningpoker.core.data.repository.TaskSaveOutcome
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Evaluations stay pending until the test completes [pendingOutcome]. */
class FakeTaskRepository(initial: List<Task> = emptyList()) : TaskRepository {

    data class Call(val kind: String, val id: Long, val description: String, val userVote: Vote)

    private val tasks = MutableStateFlow(initial)
    val calls = mutableListOf<Call>()
    var pendingOutcome = CompletableDeferred<TaskSaveOutcome>()

    override fun observeTasks(sprintId: Long): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.sprintId == sprintId } }

    override fun observeTask(id: Long): Flow<Task?> = tasks.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun evaluateAndCreate(sprintId: Long, description: String, userVote: Vote): TaskSaveOutcome {
        calls += Call("create", sprintId, description, userVote)
        return pendingOutcome.await()
    }

    override suspend fun reevaluate(taskId: Long, description: String, userVote: Vote): TaskSaveOutcome {
        calls += Call("reevaluate", taskId, description, userVote)
        return pendingOutcome.await()
    }
}
