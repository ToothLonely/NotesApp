package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GigaChatTokenProviderTest {
    @Test
    fun `configured access token is used without oauth request`() = runTest {
        val authDataSource = FakeGigaChatAuthDataSource()
        val provider = ConfigurableGigaChatTokenProvider(
            credentials = credentials(accessToken = "local-token"),
            authDataSource = authDataSource,
            currentTimeMillis = { 1_000L },
        )

        assertEquals("local-token", provider.getAccessToken())
        assertEquals(0, authDataSource.requestCount)
    }

    @Test
    fun `authorization key obtains and caches token until refresh margin`() = runTest {
        var now = 1_000L
        val authDataSource = FakeGigaChatAuthDataSource().apply {
            tokens += NetworkAccessToken("first", expiresAtMillis = 100_000L)
            tokens += NetworkAccessToken("second", expiresAtMillis = 200_000L)
        }
        val provider = ConfigurableGigaChatTokenProvider(
            credentials = credentials(authorizationKey = "authorization-key"),
            authDataSource = authDataSource,
            currentTimeMillis = { now },
        )

        assertEquals("first", provider.getAccessToken())
        now = 20_000L
        assertEquals("first", provider.getAccessToken())
        now = 40_001L
        assertEquals("second", provider.getAccessToken())
        assertEquals(2, authDataSource.requestCount)
    }

    @Test
    fun `expired static access token cannot be refreshed without authorization key`() = runTest {
        val provider = ConfigurableGigaChatTokenProvider(
            credentials = credentials(accessToken = "expired-token"),
            authDataSource = FakeGigaChatAuthDataSource(),
            currentTimeMillis = { 0L },
        )

        val error = try {
            provider.getAccessToken(forceRefresh = true)
            throw AssertionError("Expected GigaChatException")
        } catch (error: GigaChatException) {
            error
        }
        assertEquals(GigaChatFailure.Authentication, error.failure)
    }

    @Test
    fun `missing access token and authorization key reports not configured`() = runTest {
        val provider = ConfigurableGigaChatTokenProvider(
            credentials = credentials(),
            authDataSource = FakeGigaChatAuthDataSource(),
            currentTimeMillis = { 0L },
        )

        val error = try {
            provider.getAccessToken()
            throw AssertionError("Expected GigaChatException")
        } catch (error: GigaChatException) {
            error
        }

        assertEquals(GigaChatFailure.NotConfigured, error.failure)
    }

    private fun credentials(
        accessToken: String = "",
        authorizationKey: String = "",
    ) = GigaChatCredentials(
        initialAccessToken = accessToken,
        authorizationKey = authorizationKey,
        scope = "GIGACHAT_API_PERS",
    )
}

private class FakeGigaChatAuthDataSource : GigaChatAuthDataSource {
    val tokens = ArrayDeque<NetworkAccessToken>()
    var requestCount = 0

    override suspend fun requestAccessToken(
        authorizationKey: String,
        scope: String,
    ): NetworkAccessToken {
        requestCount += 1
        return tokens.removeFirst()
    }
}
