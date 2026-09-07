package dev.toothlonely.notesapp.feature.notes.impl.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTitleGeneratorTest {
    private val generator = NoteTitleGenerator("Заметка %d")

    @Test
    fun `blank title uses stable generated title number`() {
        assertEquals(
            ResolvedNoteTitle(value = "Заметка 7", generatedTitleNumber = 7),
            generator.resolve(input = "   ", number = 7),
        )
    }

    @Test
    fun `entered title is trimmed and is not marked as generated`() {
        assertEquals(
            ResolvedNoteTitle(value = "Покупки", generatedTitleNumber = null),
            generator.resolve(input = "  Покупки  ", number = 7),
        )
    }
}
