package com.planningpoker.core.data.di

import com.planningpoker.core.data.repository.OfflineSprintRepository
import com.planningpoker.core.data.repository.SprintRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindsSprintRepository(repository: OfflineSprintRepository): SprintRepository

    companion object {
        @Provides
        fun providesClock(): Clock = Clock.systemUTC()
    }
}
