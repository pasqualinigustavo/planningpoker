package com.planningpoker.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.dao.TaskDao
import com.planningpoker.core.database.model.SprintEntity
import com.planningpoker.core.database.model.TaskEntity
import com.planningpoker.core.database.util.RisksConverter

@Database(
    entities = [SprintEntity::class, TaskEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    exportSchema = true,
)
@TypeConverters(RisksConverter::class)
abstract class PlanningPokerDatabase : RoomDatabase() {
    abstract fun sprintDao(): SprintDao
    abstract fun taskDao(): TaskDao
}
