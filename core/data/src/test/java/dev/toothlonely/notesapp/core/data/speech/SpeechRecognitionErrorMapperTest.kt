package dev.toothlonely.notesapp.core.data.speech

import android.speech.SpeechRecognizer
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechRecognitionErrorMapperTest {
    @Test
    fun `platform errors are mapped to stable domain failures`() {
        assertEquals(
            SpeechRecognitionFailure.NoSpeech,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT.asSpeechRecognitionFailure(),
        )
        assertEquals(
            SpeechRecognitionFailure.Network,
            SpeechRecognizer.ERROR_NETWORK.asSpeechRecognitionFailure(),
        )
        assertEquals(
            SpeechRecognitionFailure.Busy,
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY.asSpeechRecognitionFailure(),
        )
    }
}
