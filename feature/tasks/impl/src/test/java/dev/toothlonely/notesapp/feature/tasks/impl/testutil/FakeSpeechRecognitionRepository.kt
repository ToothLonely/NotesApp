package dev.toothlonely.notesapp.feature.tasks.impl.testutil

import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionEvent
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeSpeechRecognitionRepository : SpeechRecognitionRepository {
    private val mutableEvents = MutableSharedFlow<SpeechRecognitionEvent>(
        extraBufferCapacity = 8,
    )
    override val events: Flow<SpeechRecognitionEvent> = mutableEvents.asSharedFlow()

    val startedLocales = mutableListOf<String>()
    var stopCount = 0
    var cancelCount = 0
    var closeCount = 0

    override fun start(localeTag: String) {
        startedLocales += localeTag
    }

    override fun stop() {
        stopCount += 1
    }

    override fun cancel() {
        cancelCount += 1
    }

    override fun close() {
        closeCount += 1
    }

    fun emit(event: SpeechRecognitionEvent) {
        check(mutableEvents.tryEmit(event))
    }
}
