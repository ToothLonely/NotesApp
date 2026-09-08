package dev.toothlonely.notesapp.core.data.network.gigachat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class NetworkAccessToken(
    @SerialName("access_token") val value: String,
    @SerialName("expires_at") val expiresAtMillis: Long,
)

@Serializable
internal data class NetworkBalanceResponse(
    val balance: List<NetworkBalanceItem> = emptyList(),
)

@Serializable
internal data class NetworkBalanceItem(
    val usage: String = "",
    val value: Long,
)

@Serializable
internal data class NetworkChatRequest(
    val model: String,
    val messages: List<NetworkChatMessage>,
    val stream: Boolean = false,
    @SerialName("max_tokens") val maxTokens: Int,
)

@Serializable
internal data class NetworkChatMessage(
    val role: String,
    val content: String,
)

@Serializable
internal data class NetworkChatResponse(
    val choices: List<NetworkChatChoice> = emptyList(),
)

@Serializable
internal data class NetworkChatChoice(
    val message: NetworkChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null,
)
