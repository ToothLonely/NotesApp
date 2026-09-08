package dev.toothlonely.notesapp.core.domain.speech

import kotlinx.coroutines.flow.Flow

interface SpeechRecognitionRepository {
    val events: Flow<SpeechRecognitionEvent>

    fun start(localeTag: String)

    fun stop()

    fun cancel()

    fun close()
}

sealed interface SpeechRecognitionEvent {
    data object Listening : SpeechRecognitionEvent

    data object Processing : SpeechRecognitionEvent

    data class Result(
        val text: String,
    ) : SpeechRecognitionEvent

    data class Error(
        val failure: SpeechRecognitionFailure,
    ) : SpeechRecognitionEvent

    data object Canceled : SpeechRecognitionEvent
}

enum class SpeechRecognitionFailure {
    Unavailable,
    NoSpeech,
    NoMatch,
    Network,
    Audio,
    Busy,
    Unknown,
}
