package com.planningpoker.core.data.testdoubles

import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.model.SprintEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSprintDao : SprintDao {

    private val sprints = MutableStateFlow(emptyList<SprintEntity>())
    private var nextId = 1L

    val inserted: List<SprintEntity> get() = sprints.value

    override fun observeSprints(): Flow<List<SprintEntity>> =
        sprints.map { list -> list.sortedWith(compareByDescending<SprintEntity> { it.createdAt }.thenByDescending { it.id }) }

    override fun observeSprint(id: Long): Flow<SprintEntity?> =
        sprints.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun insert(sprint: SprintEntity): Long {
        val id = nextId++
        sprints.update { it + sprint.copy(id = id) }
        return id
    }

    override suspend fun delete(id: Long) {
        sprints.update { list -> list.filterNot { it.id == id } }
    }
}
