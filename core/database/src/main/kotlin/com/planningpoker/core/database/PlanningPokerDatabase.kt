package com.planningpoker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.model.SprintEntity

@Database(
    entities = [SprintEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PlanningPokerDatabase : RoomDatabase() {
    abstract fun sprintDao(): SprintDao
}
