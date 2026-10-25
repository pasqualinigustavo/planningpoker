package com.planningpoker.feature.sprints.testing

import com.planningpoker.core.data.repository.SprintRepository
import com.planningpoker.core.model.Sprint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakeSprintRepository(initial: List<Sprint> = emptyList()) : SprintRepository {

    private val sprints = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1
    val deletedIds = mutableListOf<Long>()

    override fun observeSprints(): Flow<List<Sprint>> =
        sprints.map { list -> list.sortedByDescending { it.createdAt } }

    override fun observeSprint(id: Long): Flow<Sprint?> =
        sprints.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun create(name: String): Long {
        require(name.isNotBlank())
        val id = nextId++
        sprints.update { it + Sprint(id, name.trim(), Instant.ofEpochSecond(id)) }
        return id
    }

    override suspend fun delete(id: Long) {
        deletedIds += id
        sprints.update { list -> list.filterNot { it.id == id } }
    }
}
