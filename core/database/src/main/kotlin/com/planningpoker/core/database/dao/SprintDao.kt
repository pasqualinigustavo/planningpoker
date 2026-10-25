package com.planningpoker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.planningpoker.core.database.model.SprintEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SprintDao {

    /** Newest first; id breaks ties between sprints created in the same millisecond. */
    @Query("SELECT * FROM sprints ORDER BY created_at DESC, id DESC")
    fun observeSprints(): Flow<List<SprintEntity>>

    @Query("SELECT * FROM sprints WHERE id = :id")
    fun observeSprint(id: Long): Flow<SprintEntity?>

    @Insert
    suspend fun insert(sprint: SprintEntity): Long

    @Query("DELETE FROM sprints WHERE id = :id")
    suspend fun delete(id: Long)
}
