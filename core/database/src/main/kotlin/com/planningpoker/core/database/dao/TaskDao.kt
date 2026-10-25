package com.planningpoker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.planningpoker.core.database.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    /** Most recently evaluated first; id breaks ties. */
    @Query("SELECT * FROM tasks WHERE sprint_id = :sprintId ORDER BY evaluated_at DESC, id DESC")
    fun observeTasks(sprintId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeTask(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: Long): TaskEntity?

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)
}
