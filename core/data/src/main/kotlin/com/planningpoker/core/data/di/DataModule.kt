package com.planningpoker.core.data.di

import com.planningpoker.core.data.repository.DeviceLanguage
import com.planningpoker.core.data.repository.OfflineFirstTaskRepository
import com.planningpoker.core.data.repository.OfflineSprintRepository
import com.planningpoker.core.data.repository.SprintRepository
import com.planningpoker.core.data.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.util.Locale

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindsSprintRepository(repository: OfflineSprintRepository): SprintRepository

    @Binds
    abstract fun bindsTaskRepository(repository: OfflineFirstTaskRepository): TaskRepository

    companion object {
        @Provides
        fun providesClock(): Clock = Clock.systemUTC()

        // Read on every call so a language change applies without restarting the app.
        @Provides
        fun providesDeviceLanguage(): DeviceLanguage = DeviceLanguage { Locale.getDefault().toLanguageTag() }
    }
}
