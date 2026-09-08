package dev.toothlonely.notesapp.core.data.speech

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionEvent
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AndroidSpeechRecognitionRepository(
    private val context: Context,
) : SpeechRecognitionRepository {
    private val _events = MutableSharedFlow<SpeechRecognitionEvent>(
        extraBufferCapacity = EVENT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: Flow<SpeechRecognitionEvent> = _events.asSharedFlow()

    private var recognizer: SpeechRecognizer? = null
    private var isActive = false

    override fun start(localeTag: String) {
        if (isActive) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _events.tryEmit(
                SpeechRecognitionEvent.Error(SpeechRecognitionFailure.Unavailable),
            )
            return
        }

        val currentRecognizer = try {
            recognizer ?: createRecognizer().also { createdRecognizer ->
                createdRecognizer.setRecognitionListener(listener)
                recognizer = createdRecognizer
            }
        } catch (_: Throwable) {
            _events.tryEmit(SpeechRecognitionEvent.Error(SpeechRecognitionFailure.Unavailable))
            return
        }
        isActive = true
        _events.tryEmit(SpeechRecognitionEvent.Listening)
        try {
            currentRecognizer.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                },
            )
        } catch (_: Throwable) {
            isActive = false
            _events.tryEmit(SpeechRecognitionEvent.Error(SpeechRecognitionFailure.Unknown))
        }
    }

    override fun stop() {
        if (!isActive) return
        _events.tryEmit(SpeechRecognitionEvent.Processing)
        recognizer?.stopListening()
    }

    override fun cancel() {
        if (!isActive) return
        isActive = false
        recognizer?.cancel()
        _events.tryEmit(SpeechRecognitionEvent.Canceled)
    }

    override fun close() {
        isActive = false
        recognizer?.destroy()
        recognizer = null
    }

    private fun createRecognizer(): SpeechRecognizer =
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        ) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) = Unit

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            if (isActive) _events.tryEmit(SpeechRecognitionEvent.Processing)
        }

        override fun onError(error: Int) {
            if (!isActive) return
            isActive = false
            _events.tryEmit(SpeechRecognitionEvent.Error(error.asSpeechRecognitionFailure()))
        }

        override fun onResults(results: Bundle?) {
            if (!isActive) return
            isActive = false
            val text = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
            _events.tryEmit(
                if (text.isNullOrEmpty()) {
                    SpeechRecognitionEvent.Error(SpeechRecognitionFailure.NoMatch)
                } else {
                    SpeechRecognitionEvent.Result(text)
                },
            )
        }

        override fun onPartialResults(partialResults: Bundle?) = Unit

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private companion object {
        const val EVENT_BUFFER_CAPACITY = 8
    }
}

internal fun Int.asSpeechRecognitionFailure(): SpeechRecognitionFailure = when (this) {
    SpeechRecognizer.ERROR_NO_MATCH -> SpeechRecognitionFailure.NoMatch
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> SpeechRecognitionFailure.NoSpeech
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        -> SpeechRecognitionFailure.Network
    SpeechRecognizer.ERROR_AUDIO -> SpeechRecognitionFailure.Audio
    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> SpeechRecognitionFailure.Busy
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
    SpeechRecognizer.ERROR_SERVER,
    SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
    SpeechRecognizer.ERROR_CLIENT,
    SpeechRecognizer.ERROR_TOO_MANY_REQUESTS,
    SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
    SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE,
        -> SpeechRecognitionFailure.Unknown
    else -> SpeechRecognitionFailure.Unknown
}
