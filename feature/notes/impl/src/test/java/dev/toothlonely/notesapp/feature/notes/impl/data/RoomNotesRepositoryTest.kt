package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomNotesRepositoryTest {
    private val dao = FakeNotesDao()
    private var currentTimeMillis = 123L
    private val repository = RoomNotesRepository(
        notesDao = dao,
        timeProvider = TimeProvider { currentTimeMillis },
    )

    @Test
    fun `observed entities are mapped and updates remain reactive`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(
                id = 1,
                title = "Заголовок",
                content = "Текст",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                updatedAtMillis = 150,
            ),
        )

        assertEquals(
            listOf(
                Note(
                    id = 1,
                    title = "Заголовок",
                    content = "Текст",
                    createdAtMillis = 100,
                    updatedAtMillis = 150,
                ),
            ),
            repository.observeNotes().first(),
        )

        dao.notes.value = listOf(
            NoteEntity(2, "Новая", "", 200, generatedTitleNumber = 2),
        )
        assertEquals(
            listOf(Note(2, "Новая", "", 200, generatedTitleNumber = 2)),
            repository.observeNotes().first(),
        )
    }

    @Test
    fun `single note is observed by id and mapped`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(5, "Заголовок", "Текст", 100, generatedTitleNumber = 3),
        )

        assertEquals(
            Note(5, "Заголовок", "Текст", 100, generatedTitleNumber = 3),
            repository.observeNote(5).first(),
        )
        assertEquals(null, repository.observeNote(6).first())
    }

    @Test
    fun `create maps draft and supplies matching creation and update times`() = runTest {
        repository.createNote(NewNote("Заметка 3", "Текст", generatedTitleNumber = 3))

        assertEquals(
            NoteEntity(
                title = "Заметка 3",
                content = "Текст",
                createdAtMillis = 123,
                generatedTitleNumber = 3,
                updatedAtMillis = 123,
            ),
            dao.inserted.single(),
        )
    }

    @Test
    fun `next generated number comes from persistent storage`() = runTest {
        dao.nextNumber = 8

        assertEquals(8, repository.nextGeneratedTitleNumber())
    }

    @Test
    fun `update changes modification time while preserving id and creation time`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(4, "Старый", "Старый текст", 456, generatedTitleNumber = null),
        )
        currentTimeMillis = 789

        val updated = repository.updateNote(
            NoteUpdate(4, "Новый", "Новый текст", generatedTitleNumber = 9),
        )

        assertEquals(true, updated)
        assertEquals(
            NoteEntity(
                id = 4,
                title = "Новый",
                content = "Новый текст",
                createdAtMillis = 456,
                generatedTitleNumber = 9,
                updatedAtMillis = 789,
            ),
            dao.notes.value.single(),
        )
        assertEquals(1, dao.notes.value.size)
    }

    @Test
    fun `update reports a missing note`() = runTest {
        assertEquals(
            false,
            repository.updateNote(
                NoteUpdate(404, "Заголовок", "Текст", generatedTitleNumber = null),
            ),
        )
    }

    @Test
    fun `delete removes matching note and reports missing note`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(1, "Первая", "", 100, generatedTitleNumber = null),
            NoteEntity(2, "Вторая", "", 200, generatedTitleNumber = null),
        )

        assertEquals(true, repository.deleteNote(1))
        assertEquals(listOf(2L), dao.notes.value.map(NoteEntity::id))
        assertEquals(false, repository.deleteNote(404))
    }
}

private class FakeNotesDao : NotesDao {
    val notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val inserted = mutableListOf<NoteEntity>()
    var nextNumber = 1

    override fun observeNotes(): Flow<List<NoteEntity>> = notes

    override fun observeNote(noteId: Long): Flow<NoteEntity?> = notes.map { notes ->
        notes.find { it.id == noteId }
    }

    override suspend fun insert(note: NoteEntity): Long {
        inserted += note
        return inserted.size.toLong()
    }

    override suspend fun update(
        noteId: Long,
        title: String,
        content: String,
        generatedTitleNumber: Int?,
        updatedAtMillis: Long,
    ): Int {
        val existing = notes.value.find { it.id == noteId } ?: return 0
        notes.value = notes.value.map { note ->
            if (note.id == noteId) {
                existing.copy(
                    title = title,
                    content = content,
                    generatedTitleNumber = generatedTitleNumber,
                    updatedAtMillis = updatedAtMillis,
                )
            } else {
                note
            }
        }
        return 1
    }

    override suspend fun delete(noteId: Long): Int {
        val newNotes = notes.value.filterNot { it.id == noteId }
        if (newNotes.size == notes.value.size) return 0
        notes.value = newNotes
        return 1
    }

    override suspend fun nextGeneratedTitleNumber(): Int = nextNumber
}
