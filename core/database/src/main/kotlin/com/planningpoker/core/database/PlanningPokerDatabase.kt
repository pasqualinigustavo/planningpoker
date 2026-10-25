package com.planningpoker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.planningpoker.core.database.model.PlaceholderEntity

@Database(
    entities = [PlaceholderEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PlanningPokerDatabase : RoomDatabase()
