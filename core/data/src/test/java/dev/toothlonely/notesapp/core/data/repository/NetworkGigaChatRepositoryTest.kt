package dev.toothlonely.notesapp.core.data.repository

import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatNetworkDataSource
import dev.toothlonely.notesapp.core.data.network.gigachat.GigaChatTokenProvider
import dev.toothlonely.notesapp.core.data.network.gigachat.NetworkBalanceItem
import dev.toothlonely.notesapp.core.data.network.gigachat.NetworkBalanceResponse
import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkGigaChatRepositoryTest {
    @Test
    fun `balance values are summed and unauthorized request refreshes once`() = runTest {
        val tokenProvider = FakeTokenProvider()
        val network = FakeGigaChatNetworkDataSource().apply {
            failFirstBalanceWithAuthentication = true
            balance = NetworkBalanceResponse(
                listOf(
                    NetworkBalanceItem("GigaChat", 100),
                    NetworkBalanceItem("GigaChat-Pro", 50),
                ),
            )
        }
        val repository = NetworkGigaChatRepository(tokenProvider, network)

        assertEquals(150L, repository.getBalance().totalTokens)
        assertEquals(listOf(false, true), tokenProvider.forceRefreshRequests)
        assertEquals(listOf("initial", "refreshed"), network.balanceTokens)
    }

    @Test
    fun `recognized task text is trimmed before network request`() = runTest {
        val network = FakeGigaChatNetworkDataSource().apply {
            formulatedTask = "Купить молоко"
        }
        val repository = NetworkGigaChatRepository(FakeTokenProvider(), network)

        assertEquals("Купить молоко", repository.formulateTask("  купи молоко  "))
        assertEquals("купи молоко", network.recognizedText)
    }
}

private class FakeTokenProvider : GigaChatTokenProvider {
    val forceRefreshRequests = mutableListOf<Boolean>()

    override suspend fun getAccessToken(forceRefresh: Boolean): String {
        forceRefreshRequests += forceRefresh
        return if (forceRefresh) "refreshed" else "initial"
    }
}

private class FakeGigaChatNetworkDataSource : GigaChatNetworkDataSource {
    var balance = NetworkBalanceResponse(emptyList())
    var formulatedTask = ""
    var recognizedText: String? = null
    var failFirstBalanceWithAuthentication = false
    val balanceTokens = mutableListOf<String>()

    override suspend fun getBalance(accessToken: String): NetworkBalanceResponse {
        balanceTokens += accessToken
        if (failFirstBalanceWithAuthentication) {
            failFirstBalanceWithAuthentication = false
            throw GigaChatException(GigaChatFailure.Authentication)
        }
        return balance
    }

    override suspend fun formulateTask(
        accessToken: String,
        recognizedText: String,
    ): String {
        this.recognizedText = recognizedText
        return formulatedTask
    }
}
