package com.planningpoker.core.ai;

import com.google.firebase.ai.type.QuotaExceededException;
import com.google.firebase.ai.type.RequestTimeoutException;
import com.google.firebase.ai.type.SerializationException;
import com.google.firebase.ai.type.ServerException;
import com.google.firebase.ai.type.UnknownException;
import java.util.Collections;

/**
 * The SDK's exception constructors are Kotlin-internal (public in bytecode), so Kotlin tests
 * cannot call them; Java can. Lets tests use the real exception types the SDK throws.
 */
final class FirebaseAIExceptions {
    private FirebaseAIExceptions() {}

    static UnknownException unknown(String message, Throwable cause) {
        return new UnknownException(message, cause);
    }

    static RequestTimeoutException requestTimeout(String message) {
        return new RequestTimeoutException(message, null, Collections.emptyList());
    }

    static SerializationException serialization(String message) {
        return new SerializationException(message, null);
    }

    static QuotaExceededException quotaExceeded(String message) {
        return new QuotaExceededException(message, null);
    }

    static ServerException server(String message) {
        return new ServerException(message, null);
    }
}
