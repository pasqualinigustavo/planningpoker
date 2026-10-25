package com.planningpoker.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.planningpoker.core.model.Task
import com.planningpoker.core.model.Vote
import java.time.Instant

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = SprintEntity::class,
            parentColumns = ["id"],
            childColumns = ["sprint_id"],
            // Deleting a sprint deletes its tasks.
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sprint_id")],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sprint_id") val sprintId: Long,
    val title: String,
    val description: String,
    @ColumnInfo(name = "user_vote") val userVote: Vote,
    @ColumnInfo(name = "ai_vote") val aiVote: Vote,
    val risks: List<String>,
    /** Epoch milliseconds. */
    @ColumnInfo(name = "evaluated_at") val evaluatedAt: Long,
)

fun TaskEntity.asExternalModel() = Task(
    id = id,
    sprintId = sprintId,
    title = title,
    description = description,
    userVote = userVote,
    aiVote = aiVote,
    risks = risks,
    evaluatedAt = Instant.ofEpochMilli(evaluatedAt),
)
