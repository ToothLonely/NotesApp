package dev.toothlonely.notesapp.core.data.repository

import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatNetworkDataSource
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatTokenProvider
import dev.toothlonely.notesapp.core.domain.model.GigaChatBalance
import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository

internal class NetworkGigaChatRepository(
    private val tokenProvider: GigaChatTokenProvider,
    private val networkDataSource: GigaChatNetworkDataSource,
) : GigaChatRepository {
    override suspend fun getBalance(): GigaChatBalance = authorizedRequest { token ->
        val balance = networkDataSource.getBalance(token)
        if (balance.balance.isEmpty()) {
            throw GigaChatException(GigaChatFailure.InvalidResponse)
        }
        GigaChatBalance(
            totalTokens = balance.balance.sumOf { item -> item.value },
        )
    }

    override suspend fun formulateTask(recognizedText: String): String {
        if (recognizedText.isBlank()) {
            throw GigaChatException(GigaChatFailure.InvalidResponse)
        }
        return authorizedRequest { token ->
            networkDataSource.formulateTask(
                accessToken = token,
                recognizedText = recognizedText.trim(),
            )
        }
    }

    private suspend fun <T> authorizedRequest(block: suspend (String) -> T): T {
        val token = tokenProvider.getAccessToken()
        return try {
            block(token)
        } catch (error: GigaChatException) {
            if (error.failure != GigaChatFailure.Authentication) throw error
            block(tokenProvider.getAccessToken(forceRefresh = true))
        }
    }
}
