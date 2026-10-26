package com.planningpoker.core.ai

import com.google.firebase.ai.type.APINotConfiguredException
import com.google.firebase.ai.type.ContentBlockedException
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.InvalidLocationException
import com.google.firebase.ai.type.PermissionMissingException
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.SerializationException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.ServiceDisabledException
import com.google.firebase.ai.type.UnsupportedUserLocationException
import com.planningpoker.core.model.EvaluationError
import java.io.IOException

/** Maps SDK and transport failures so Firebase exception types never leave `core:ai`. */
internal fun Throwable.toEvaluationError(): EvaluationError = when {
    causes().any { it is IOException } -> EvaluationError.Network

    this is RequestTimeoutException -> EvaluationError.Timeout

    this is SerializationException -> EvaluationError.InvalidResponse

    this is QuotaExceededException ||
        this is PromptBlockedException ||
        this is ContentBlockedException ||
        this is ResponseStoppedException ||
        this is InvalidAPIKeyException ||
        this is ServiceDisabledException ||
        this is APINotConfiguredException ||
        this is PermissionMissingException ||
        this is UnsupportedUserLocationException ||
        this is InvalidLocationException ||
        // Includes App Check rejections, which the backend reports as server errors.
        this is ServerException -> EvaluationError.Rejected

    else -> EvaluationError.Unknown
}

private fun Throwable.causes(): Sequence<Throwable> = generateSequence(this) { it.cause }
