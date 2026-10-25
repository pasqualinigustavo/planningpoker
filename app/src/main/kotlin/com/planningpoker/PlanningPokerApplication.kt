package com.planningpoker

import android.app.Application
import com.planningpoker.appcheck.AppCheckInstaller
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PlanningPokerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Must run before any Firebase AI request so every call carries an App Check token.
        AppCheckInstaller.install()
    }
}
