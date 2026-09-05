package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomNotesRepositoryTest {
    private val dao = FakeNotesDao()
    private val repository = RoomNotesRepository(
        notesDao = dao,
        timeProvider = TimeProvider { 123L },
    )

    @Test
    fun `observed entities are mapped and updates remain reactive`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(1, "Заголовок", "Текст", 100, generatedTitleNumber = null),
        )

        assertEquals(
            listOf(Note(1, "Заголовок", "Текст", 100)),
            repository.observeNotes().first(),
        )

        dao.notes.value = listOf(
            NoteEntity(2, "Новая", "", 200, generatedTitleNumber = 2),
        )
        assertEquals(
            listOf(Note(2, "Новая", "", 200)),
            repository.observeNotes().first(),
        )
    }

    @Test
    fun `create maps draft and supplies current time`() = runTest {
        repository.createNote(NewNote("Заметка 3", "Текст", generatedTitleNumber = 3))

        assertEquals(
            NoteEntity(
                title = "Заметка 3",
                content = "Текст",
                createdAtMillis = 123,
                generatedTitleNumber = 3,
            ),
            dao.inserted.single(),
        )
    }

    @Test
    fun `next generated number comes from persistent storage`() = runTest {
        dao.nextNumber = 8

        assertEquals(8, repository.nextGeneratedTitleNumber())
    }
}

private class FakeNotesDao : NotesDao {
    val notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val inserted = mutableListOf<NoteEntity>()
    var nextNumber = 1

    override fun observeNotes(): Flow<List<NoteEntity>> = notes

    override suspend fun insert(note: NoteEntity): Long {
        inserted += note
        return inserted.size.toLong()
    }

    override suspend fun nextGeneratedTitleNumber(): Int = nextNumber
}
