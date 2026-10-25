package com.planningpoker.appcheck

import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

/**
 * Debug builds and emulators cannot pass Play Integrity. The debug provider logs a token
 * (tag `DebugAppCheckProvider`) that must be registered in the Firebase Console.
 * This source set is never compiled into release builds.
 */
internal object AppCheckInstaller {
    fun install() {
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
    }
}
