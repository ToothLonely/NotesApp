package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class GigaChatNetworkDataSourceTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `balance response is decoded from official schema`() = runTest {
        val api = FakeGigaChatApi().apply {
            balanceResponse = Response.success(
                """{"balance":[{"usage":"GigaChat","value":12480}]}""".toResponseBody(),
            )
        }
        val dataSource = RetrofitGigaChatNetworkDataSource(api, json)

        val response = dataSource.getBalance("access-token")

        assertEquals(12_480L, response.balance.single().value)
        assertEquals("Bearer access-token", api.balanceAuthorization)
    }

    @Test
    fun `task request uses system and user messages and reads first choice`() = runTest {
        val api = FakeGigaChatApi().apply {
            chatResponse = Response.success(
                """{"choices":[{"message":{"role":"assistant","content":" Купить молоко "}}]}"""
                    .toResponseBody(),
            )
        }
        val dataSource = RetrofitGigaChatNetworkDataSource(api, json)

        assertEquals("Купить молоко", dataSource.formulateTask("token", "купи молоко"))
        assertEquals("Bearer token", api.chatAuthorization)
        val request = requireNotNull(api.chatRequestBody).bodyAsString()
        assertTrue(request.contains("\"model\":\"GigaChat-3-Pro\""))
        assertTrue(request.contains("\"role\":\"system\""))
        assertTrue(request.contains("\"role\":\"user\""))
        assertTrue(request.contains("купи молоко"))
    }

    @Test
    fun `unauthorized response is mapped without exposing response body`() = runTest {
        val api = FakeGigaChatApi().apply {
            balanceResponse = Response.error(
                401,
                "secret server details".toResponseBody(),
            )
        }
        val dataSource = RetrofitGigaChatNetworkDataSource(api, json)

        val error = try {
            dataSource.getBalance("expired")
            throw AssertionError("Expected GigaChatException")
        } catch (error: GigaChatException) {
            error
        }
        assertEquals(GigaChatFailure.Authentication, error.failure)
    }
}

private class FakeGigaChatApi : GigaChatApi {
    var balanceResponse: Response<ResponseBody> = Response.success("{}".toResponseBody())
    var chatResponse: Response<ResponseBody> = Response.success("{}".toResponseBody())
    var balanceAuthorization: String? = null
    var chatAuthorization: String? = null
    var chatRequestBody: RequestBody? = null

    override suspend fun getBalance(authorization: String): Response<ResponseBody> {
        balanceAuthorization = authorization
        return balanceResponse
    }

    override suspend fun createChatCompletion(
        authorization: String,
        request: RequestBody,
    ): Response<ResponseBody> {
        chatAuthorization = authorization
        chatRequestBody = request
        return chatResponse
    }
}

private fun RequestBody.bodyAsString(): String {
    val buffer = okio.Buffer()
    writeTo(buffer)
    return buffer.readUtf8()
}
