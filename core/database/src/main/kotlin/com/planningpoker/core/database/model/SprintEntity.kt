package com.planningpoker.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.planningpoker.core.model.Sprint
import java.time.Instant

@Entity(
    tableName = "sprints",
    indices = [Index("created_at")],
)
data class SprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Epoch milliseconds. */
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

fun SprintEntity.asExternalModel() = Sprint(
    id = id,
    name = name,
    createdAt = Instant.ofEpochMilli(createdAt),
)
