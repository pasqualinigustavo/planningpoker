package com.planningpoker.core.data.testdoubles

import com.planningpoker.core.database.dao.TaskDao
import com.planningpoker.core.database.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskDao : TaskDao {

    private val tasks = MutableStateFlow(emptyList<TaskEntity>())
    private var nextId = 1L

    val stored: List<TaskEntity> get() = tasks.value

    override fun observeTasks(sprintId: Long): Flow<List<TaskEntity>> = tasks.map { list ->
        list.filter { it.sprintId == sprintId }
            .sortedWith(compareByDescending<TaskEntity> { it.evaluatedAt }.thenByDescending { it.id })
    }

    override fun observeTask(id: Long): Flow<TaskEntity?> = tasks.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun getTask(id: Long): TaskEntity? = tasks.value.firstOrNull { it.id == id }

    override suspend fun insert(task: TaskEntity): Long {
        val id = nextId++
        tasks.update { it + task.copy(id = id) }
        return id
    }

    override suspend fun update(task: TaskEntity) {
        tasks.update { list -> list.map { if (it.id == task.id) task else it } }
    }
}
