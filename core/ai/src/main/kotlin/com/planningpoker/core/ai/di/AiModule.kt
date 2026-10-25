package com.planningpoker.core.ai.di

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AiModule {

    @Provides
    @Singleton
    fun providesFirebaseAI(): FirebaseAI = Firebase.ai(
        app = FirebaseApp.getInstance(),
        // Gemini Developer API; switch to GenerativeBackend.vertexAI() to move to Vertex AI.
        backend = GenerativeBackend.googleAI(),
        // Single-use App Check tokens protect AI requests against replay.
        useLimitedUseAppCheckTokens = true,
    )
}
