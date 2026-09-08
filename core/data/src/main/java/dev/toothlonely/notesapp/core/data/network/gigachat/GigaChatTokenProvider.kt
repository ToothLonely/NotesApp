package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal interface GigaChatTokenProvider {
    suspend fun getAccessToken(forceRefresh: Boolean = false): String
}

internal class ConfigurableGigaChatTokenProvider(
    credentials: GigaChatCredentials,
    private val authDataSource: GigaChatAuthDataSource,
    private val currentTimeMillis: () -> Long,
) : GigaChatTokenProvider {
    private val initialAccessToken = credentials.initialAccessToken.trim()
    private val authorizationKey = credentials.authorizationKey.trim()
    private val scope = credentials.scope.trim()
    private val mutex = Mutex()
    private var refreshedToken: NetworkAccessToken? = null

    override suspend fun getAccessToken(forceRefresh: Boolean): String = mutex.withLock {
        if (!forceRefresh) {
            refreshedToken
                ?.takeIf { token -> token.expiresAtMillis - TOKEN_REFRESH_MARGIN_MILLIS > currentTimeMillis() }
                ?.let { token -> return@withLock token.value }

            if (refreshedToken == null && initialAccessToken.isNotEmpty()) {
                return@withLock initialAccessToken
            }
        }

        if (authorizationKey.isEmpty()) {
            throw GigaChatException(
                if (initialAccessToken.isEmpty()) {
                    GigaChatFailure.NotConfigured
                } else {
                    GigaChatFailure.Authentication
                },
            )
        }
        if (scope.isEmpty()) {
            throw GigaChatException(GigaChatFailure.NotConfigured)
        }

        authDataSource.requestAccessToken(
            authorizationKey = authorizationKey,
            scope = scope,
        ).also { token -> refreshedToken = token }.value
    }

    private companion object {
        const val TOKEN_REFRESH_MARGIN_MILLIS = 60_000L
    }
}
