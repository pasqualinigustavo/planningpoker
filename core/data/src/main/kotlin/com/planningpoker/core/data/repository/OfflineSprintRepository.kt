package com.planningpoker.core.data.repository

import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.model.SprintEntity
import com.planningpoker.core.database.model.asExternalModel
import com.planningpoker.core.model.Sprint
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class OfflineSprintRepository @Inject constructor(
    private val sprintDao: SprintDao,
    private val clock: Clock,
) : SprintRepository {

    override fun observeSprints(): Flow<List<Sprint>> =
        sprintDao.observeSprints().map { sprints -> sprints.map(SprintEntity::asExternalModel) }

    override fun observeSprint(id: Long): Flow<Sprint?> = sprintDao.observeSprint(id).map { it?.asExternalModel() }

    override suspend fun create(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Sprint name must not be blank" }
        return sprintDao.insert(
            SprintEntity(name = trimmed, createdAt = clock.millis()),
        )
    }

    override suspend fun delete(id: Long) = sprintDao.delete(id)
}
