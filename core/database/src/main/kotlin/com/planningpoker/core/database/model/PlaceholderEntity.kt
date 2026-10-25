package com.planningpoker.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room requires at least one entity. Replaced by `SprintEntity` in `add-sprint-management`
 * before the first release, so schema v1 never ships with this table.
 */
@Entity(tableName = "placeholder")
internal data class PlaceholderEntity(
    @PrimaryKey val id: Long,
)
