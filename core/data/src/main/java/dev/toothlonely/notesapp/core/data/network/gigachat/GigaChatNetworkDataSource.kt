package dev.toothlonely.notesapp.core.data.network.gigachat

import dev.toothlonely.notesapp.core.domain.repository.GigaChatException
import dev.toothlonely.notesapp.core.domain.repository.GigaChatFailure
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

internal interface GigaChatNetworkDataSource {
    suspend fun getBalance(accessToken: String): NetworkBalanceResponse

    suspend fun formulateTask(
        accessToken: String,
        recognizedText: String,
    ): String
}

internal class RetrofitGigaChatNetworkDataSource(
    private val api: GigaChatApi,
    private val json: Json,
) : GigaChatNetworkDataSource {
    override suspend fun getBalance(accessToken: String): NetworkBalanceResponse = execute(
        request = { api.getBalance(authorization = "Bearer $accessToken") },
        decode = json::decodeFromString,
    )

    override suspend fun formulateTask(
        accessToken: String,
        recognizedText: String,
    ): String {
        val request = NetworkChatRequest(
            model = GIGACHAT_MODEL,
            messages = listOf(
                NetworkChatMessage(
                    role = "system",
                    content = TASK_SYSTEM_PROMPT,
                ),
                NetworkChatMessage(
                    role = "user",
                    content = recognizedText,
                ),
            ),
            maxTokens = TASK_RESPONSE_MAX_TOKENS,
        )
        val body = json.encodeToString(request)
            .toRequestBody(JSON_MEDIA_TYPE)
        val response = execute<NetworkChatResponse>(
            request = {
                api.createChatCompletion(
                    authorization = "Bearer $accessToken",
                    request = body,
                )
            },
            decode = json::decodeFromString,
        )
        return response.choices.firstOrNull()?.message?.content
            ?.trim()
            ?.removeSurrounding("\"")
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?: throw GigaChatException(GigaChatFailure.InvalidResponse)
    }

    private suspend fun <T> execute(
        request: suspend () -> retrofit2.Response<okhttp3.ResponseBody>,
        decode: (String) -> T,
    ): T = try {
        val response = request()
        if (!response.isSuccessful) {
            response.errorBody()?.close()
            throw response.code().asGigaChatException()
        }
        val responseBody = response.body()
            ?: throw GigaChatException(GigaChatFailure.InvalidResponse)
        responseBody.use { body -> decode(body.string()) }
    } catch (error: GigaChatException) {
        throw error
    } catch (error: IOException) {
        throw GigaChatException(GigaChatFailure.Network, error)
    } catch (error: SerializationException) {
        throw GigaChatException(GigaChatFailure.InvalidResponse, error)
    }

    private companion object {
        const val GIGACHAT_MODEL = "GigaChat-3-Pro"
        const val TASK_RESPONSE_MAX_TOKENS = 128
        const val TASK_SYSTEM_PROMPT =
            "Сформулируй из сообщения пользователя краткое название задачи. " +
                "Сохрани важные даты, время и детали. Верни только текст задачи без кавычек, " +
                "префиксов и пояснений."
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
