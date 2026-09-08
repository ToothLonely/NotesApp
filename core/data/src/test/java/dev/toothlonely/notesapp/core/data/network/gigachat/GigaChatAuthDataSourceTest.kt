package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class GigaChatAuthDataSourceTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `oauth request sends basic key scope and unique request id`() = runTest {
        val api = FakeGigaChatAuthApi().apply {
            response = Response.success(
                """{"access_token":"fresh-token","expires_at":1700001800000}"""
                    .toResponseBody(),
            )
        }
        val dataSource = RetrofitGigaChatAuthDataSource(
            api = api,
            json = json,
            requestIdProvider = { "3f2504e0-4f89-41d3-9a0c-0305e82c3301" },
        )

        val token = dataSource.requestAccessToken(
            authorizationKey = "encoded-key",
            scope = "GIGACHAT_API_PERS",
        )

        assertEquals("fresh-token", token.value)
        assertEquals(1_700_001_800_000L, token.expiresAtMillis)
        assertEquals("Basic encoded-key", api.authorization)
        assertEquals("3f2504e0-4f89-41d3-9a0c-0305e82c3301", api.requestId)
        assertEquals("GIGACHAT_API_PERS", api.scope)
    }

    @Test
    fun `oauth authentication failure is mapped to domain failure`() = runTest {
        val api = FakeGigaChatAuthApi().apply {
            response = Response.error(401, "details".toResponseBody())
        }
        val dataSource = RetrofitGigaChatAuthDataSource(
            api = api,
            json = json,
            requestIdProvider = { "request-id" },
        )

        val error = try {
            dataSource.requestAccessToken("invalid-key", "GIGACHAT_API_PERS")
            throw AssertionError("Expected GigaChatException")
        } catch (error: GigaChatException) {
            error
        }

        assertEquals(GigaChatFailure.Authentication, error.failure)
    }
}

private class FakeGigaChatAuthApi : GigaChatAuthApi {
    var response: Response<ResponseBody> = Response.success("{}".toResponseBody())
    var authorization: String? = null
    var requestId: String? = null
    var scope: String? = null

    override suspend fun getAccessToken(
        authorization: String,
        requestId: String,
        scope: String,
    ): Response<ResponseBody> {
        this.authorization = authorization
        this.requestId = requestId
        this.scope = scope
        return response
    }
}
