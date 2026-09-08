package dev.toothlonely.notesapp.core.domain.repository

import dev.toothlonely.notesapp.core.domain.model.GigaChatBalance

interface GigaChatRepository {
    suspend fun getBalance(): GigaChatBalance

    suspend fun formulateTask(recognizedText: String): String
}

class GigaChatException(
    val failure: GigaChatFailure,
    cause: Throwable? = null,
) : Exception(failure.name, cause)

enum class GigaChatFailure {
    NotConfigured,
    Authentication,
    AccessDenied,
    RateLimited,
    Network,
    InvalidResponse,
    Service,
}
