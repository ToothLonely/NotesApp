package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal interface GigaChatAuthDataSource {
    suspend fun requestAccessToken(
        authorizationKey: String,
        scope: String,
    ): NetworkAccessToken
}

internal class RetrofitGigaChatAuthDataSource(
    private val api: GigaChatAuthApi,
    private val json: Json,
    private val requestIdProvider: () -> String,
) : GigaChatAuthDataSource {
    override suspend fun requestAccessToken(
        authorizationKey: String,
        scope: String,
    ): NetworkAccessToken = try {
        val response = api.getAccessToken(
            authorization = "Basic $authorizationKey",
            requestId = requestIdProvider(),
            scope = scope,
        )
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw response.code().asGigaChatException()
        }
        val responseBody = response.body()
            ?: throw GigaChatException(GigaChatFailure.InvalidResponse)
        val token = responseBody.use { body ->
            json.decodeFromString<NetworkAccessToken>(body.string())
        }
        if (token.value.isBlank() || token.expiresAtMillis <= 0L) {
            throw GigaChatException(GigaChatFailure.InvalidResponse)
        }
        token
    } catch (error: GigaChatException) {
        throw error
    } catch (error: IOException) {
        throw GigaChatException(GigaChatFailure.Network, error)
    } catch (error: SerializationException) {
        throw GigaChatException(GigaChatFailure.InvalidResponse, error)
    }
}

internal fun Int.asGigaChatException(): GigaChatException = GigaChatException(
    failure = when (this) {
        401 -> GigaChatFailure.Authentication
        403 -> GigaChatFailure.AccessDenied
        429 -> GigaChatFailure.RateLimited
        in 500..599 -> GigaChatFailure.Service
        else -> GigaChatFailure.InvalidResponse
    },
)
