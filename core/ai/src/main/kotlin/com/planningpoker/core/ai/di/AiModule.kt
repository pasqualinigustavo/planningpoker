package com.planningpoker.core.ai.di

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.planningpoker.core.ai.GeminiTechLeadAgent
import com.planningpoker.core.ai.TECH_LEAD_INSTRUCTION
import com.planningpoker.core.ai.TechLeadAgent
import com.planningpoker.core.ai.TechLeadModel
import com.planningpoker.core.model.AI_VOTES
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Stable Gemini 3.x Flash: available on the Gemini Developer API free tier, guaranteed until
 * at least 2027-05-19. Changing models is a change to this constant only.
 */
private const val MODEL_NAME = "gemini-3.5-flash"

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AiModule {

    @Binds
    abstract fun bindsTechLeadAgent(agent: GeminiTechLeadAgent): TechLeadAgent

    companion object {

        @Provides
        @Singleton
        fun providesFirebaseAI(): FirebaseAI = Firebase.ai(
            app = FirebaseApp.getInstance(),
            // Gemini Developer API; switch to GenerativeBackend.vertexAI() to move to Vertex AI.
            backend = GenerativeBackend.googleAI(),
            // Single-use App Check tokens protect AI requests against replay.
            useLimitedUseAppCheckTokens = true,
        )

        @Provides
        @Singleton
        fun providesTechLeadModel(firebaseAI: FirebaseAI): TechLeadModel {
            val model = firebaseAI.generativeModel(
                modelName = MODEL_NAME,
                systemInstruction = content { text(TECH_LEAD_INSTRUCTION) },
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    // The model cannot answer outside this shape or the card scale.
                    responseSchema = Schema.obj(
                        mapOf(
                            "title" to Schema.string(),
                            "estimate" to Schema.enumeration(AI_VOTES.map { it.label }),
                            "risks" to Schema.array(Schema.string()),
                        ),
                    )
                },
            )
            return TechLeadModel { prompt -> model.generateContent(prompt).text }
        }
    }
}
