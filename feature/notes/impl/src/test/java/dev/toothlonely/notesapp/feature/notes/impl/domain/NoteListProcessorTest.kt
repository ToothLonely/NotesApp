package dev.toothlonely.notesapp.feature.notes.impl.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteListProcessorTest {
    private val processor = NoteListProcessor()

    @Test
    fun `query matches substrings in title before content case insensitively`() {
        val notes = listOf(
            note(id = 1, title = "Рабочий план", content = "Покупки"),
            note(id = 2, title = "ПОКУПКИ", content = "Другое"),
        )

        val result = processor.process(
            notes = notes,
            appliedQuery = "покуп",
            sortOrder = NotesSortOrder.NewestFirst,
        )

        assertEquals(listOf(2L, 1L), result.map(Note::id))
    }

    @Test
    fun `title and content groups are sorted independently and notes are not duplicated`() {
        val notes = listOf(
            note(
                id = 1,
                title = "Проект альфа",
                content = "Проект также есть в тексте",
                updatedAtMillis = 100,
            ),
            note(id = 2, title = "Проект бета", updatedAtMillis = 300),
            note(id = 3, title = "Личное", content = "Проект гамма", updatedAtMillis = 400),
            note(id = 4, title = "Работа", content = "Проект дельта", updatedAtMillis = 200),
        )

        val newest = processor.process(notes, "проект", NotesSortOrder.NewestFirst)
        val oldest = processor.process(notes, "проект", NotesSortOrder.OldestFirst)

        assertEquals(listOf(2L, 1L, 3L, 4L), newest.map(Note::id))
        assertEquals(listOf(1L, 2L, 4L, 3L), oldest.map(Note::id))
    }

    @Test
    fun `exact and fuzzy title matches precede exact and fuzzy content matches`() {
        val notes = listOf(
            note(id = 1, title = "Хута", updatedAtMillis = 100),
            note(id = 2, title = "Хуета", updatedAtMillis = 200),
            note(id = 3, title = "Другое", content = "хута", updatedAtMillis = 300),
            note(id = 4, title = "Заметка", content = "хуета", updatedAtMillis = 400),
        )

        val result = processor.process(notes, "хута", NotesSortOrder.NewestFirst)

        assertEquals(listOf(1L, 2L, 3L, 4L), result.map(Note::id))
    }

    @Test
    fun `short query requires an exact substring`() {
        val notes = listOf(note(id = 1, title = "Кот"))

        val result = processor.process(notes, "кт", NotesSortOrder.NewestFirst)

        assertEquals(emptyList<Note>(), result)
    }

    @Test
    fun `newest and oldest use updated time then id as stable tie breaker`() {
        val notes = listOf(
            note(id = 3, updatedAtMillis = 200),
            note(id = 1, updatedAtMillis = 100),
            note(id = 2, updatedAtMillis = 100),
        )

        val newest = processor.process(notes, "", NotesSortOrder.NewestFirst)
        val oldest = processor.process(notes, "", NotesSortOrder.OldestFirst)

        assertEquals(listOf(3L, 2L, 1L), newest.map(Note::id))
        assertEquals(listOf(1L, 2L, 3L), oldest.map(Note::id))
    }

    private fun note(
        id: Long,
        title: String = "Заметка $id",
        content: String = "Текст",
        updatedAtMillis: Long = id,
    ) = Note(
        id = id,
        title = title,
        content = content,
        createdAtMillis = id,
        updatedAtMillis = updatedAtMillis,
    )
}
