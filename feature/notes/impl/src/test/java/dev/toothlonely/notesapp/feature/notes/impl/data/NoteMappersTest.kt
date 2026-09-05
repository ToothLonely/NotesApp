package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteMappersTest {
    @Test
    fun `entity maps to domain note`() {
        assertEquals(
            Note(id = 5, title = "Заголовок", content = "Текст", createdAtMillis = 123),
            NoteEntity(
                id = 5,
                title = "Заголовок",
                content = "Текст",
                createdAtMillis = 123,
                generatedTitleNumber = null,
            ).asExternalModel(),
        )
    }

    @Test
    fun `new note maps persistence metadata`() {
        assertEquals(
            NoteEntity(
                title = "Заметка 2",
                content = "",
                createdAtMillis = 456,
                generatedTitleNumber = 2,
            ),
            NewNote(
                title = "Заметка 2",
                content = "",
                generatedTitleNumber = 2,
            ).asEntity(createdAtMillis = 456),
        )
    }
}
