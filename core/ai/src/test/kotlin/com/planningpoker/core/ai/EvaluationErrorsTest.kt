package com.planningpoker.core.ai

import com.planningpoker.core.model.EvaluationError
import java.io.IOException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

class EvaluationErrorsTest {

    @Test
    fun transportFailuresAreNetwork() {
        assertEquals(EvaluationError.Network, IOException("offline").toEvaluationError())
        assertEquals(
            EvaluationError.Network,
            FirebaseAIExceptions.unknown("wrapped", UnknownHostException("no dns")).toEvaluationError(),
        )
    }

    @Test
    fun sdkTimeoutIsTimeout() {
        assertEquals(EvaluationError.Timeout, FirebaseAIExceptions.requestTimeout("slow").toEvaluationError())
    }

    @Test
    fun unreadableResponseIsInvalidResponse() {
        assertEquals(EvaluationError.InvalidResponse, FirebaseAIExceptions.serialization("bad").toEvaluationError())
    }

    @Test
    fun refusalsAreRejected() {
        assertEquals(EvaluationError.Rejected, FirebaseAIExceptions.quotaExceeded("quota").toEvaluationError())
        assertEquals(
            EvaluationError.Rejected,
            FirebaseAIExceptions.server("App Check token invalid").toEvaluationError(),
        )
    }

    @Test
    fun anythingElseIsUnknown() {
        assertEquals(EvaluationError.Unknown, IllegalStateException("boom").toEvaluationError())
        assertEquals(EvaluationError.Unknown, FirebaseAIExceptions.unknown("??", null).toEvaluationError())
    }
}
