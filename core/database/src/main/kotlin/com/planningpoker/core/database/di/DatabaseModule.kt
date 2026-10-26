package com.planningpoker.core.database.di

import android.content.Context
import androidx.room.Room
import com.planningpoker.core.database.PlanningPokerDatabase
import com.planningpoker.core.database.dao.SprintDao
import com.planningpoker.core.database.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesPlanningPokerDatabase(@ApplicationContext context: Context): PlanningPokerDatabase =
        Room.databaseBuilder(
            context,
            PlanningPokerDatabase::class.java,
            "planning-poker-database",
        ).build()

    @Provides
    fun providesSprintDao(database: PlanningPokerDatabase): SprintDao = database.sprintDao()

    @Provides
    fun providesTaskDao(database: PlanningPokerDatabase): TaskDao = database.taskDao()
}
