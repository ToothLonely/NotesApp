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
            Note(
                id = 5,
                title = "Заголовок",
                content = "Текст",
                createdAtMillis = 123,
                generatedTitleNumber = 7,
                updatedAtMillis = 456,
                imageFileName = "image.jpg",
            ),
            NoteEntity(
                id = 5,
                title = "Заголовок",
                content = "Текст",
                createdAtMillis = 123,
                generatedTitleNumber = 7,
                updatedAtMillis = 456,
                imageFileName = "image.jpg",
            ).asDomainModel(),
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
                updatedAtMillis = 456,
                imageFileName = "image.jpg",
            ),
            NewNote(
                title = "Заметка 2",
                content = "",
                generatedTitleNumber = 2,
            ).asEntity(
                createdAtMillis = 456,
                imageFileName = "image.jpg",
            ),
        )
    }
}
