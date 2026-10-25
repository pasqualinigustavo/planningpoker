package com.planningpoker.core.data.repository

import com.planningpoker.core.model.Sprint
import kotlinx.coroutines.flow.Flow

interface SprintRepository {

    /** All sprints, newest first. */
    fun observeSprints(): Flow<List<Sprint>>

    /** Emits `null` once the sprint no longer exists. */
    fun observeSprint(id: Long): Flow<Sprint?>

    /**
     * Stores a sprint named [name] with surrounding whitespace removed, stamped with the
     * current time, and returns its id.
     *
     * @throws IllegalArgumentException if [name] is blank.
     */
    suspend fun create(name: String): Long

    /** Deletes the sprint and everything that belongs to it. */
    suspend fun delete(id: Long)
}
