package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.core.data.database.model.NoteEntity
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.FakeNoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomNotesRepositoryTest {
    private val operationLog = mutableListOf<String>()
    private val dao = FakeNotesDao(operationLog)
    private val imageStorage = FakeNoteImageStorage(operationLog)
    private var currentTimeMillis = 123L
    private val repository = RoomNotesRepository(
        notesDao = dao,
        timeProvider = TimeProvider { currentTimeMillis },
        imageStorage = imageStorage,
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

    @Test
    fun `create promotes staged image before persisting its file name`() = runTest {
        val stagedFileName = imageStorage.nextStagedFileName

        repository.createNote(
            NewNote(
                title = "С изображением",
                content = "Текст",
                generatedTitleNumber = null,
                stagedImageFileName = stagedFileName,
            ),
        )

        assertEquals(listOf(stagedFileName), imageStorage.promotedFileNames)
        assertEquals(stagedFileName, dao.inserted.single().imageFileName)
        assertEquals(
            listOf("image:promote:$stagedFileName", "room:insert"),
            operationLog,
        )
    }

    @Test
    fun `failed create removes newly promoted image`() = runTest {
        val stagedFileName = imageStorage.nextStagedFileName
        dao.insertFailure = IllegalStateException("Room write failed")

        val result = runCatching {
            repository.createNote(
                NewNote(
                    title = "С изображением",
                    content = "",
                    generatedTitleNumber = null,
                    stagedImageFileName = stagedFileName,
                ),
            )
        }

        assertEquals(true, result.isFailure)
        assertEquals(listOf(stagedFileName), imageStorage.deletedFileNames)
    }

    @Test
    fun `replacement updates Room then removes old image`() = runTest {
        val oldFileName = "00000000-0000-0000-0000-000000000010.jpg"
        val newFileName = imageStorage.nextStagedFileName
        dao.notes.value = listOf(
            NoteEntity(
                id = 9,
                title = "Старая",
                content = "Текст",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = oldFileName,
            ),
        )

        assertEquals(
            true,
            repository.updateNote(
                NoteUpdate(
                    id = 9,
                    title = "Новая",
                    content = "Текст",
                    generatedTitleNumber = null,
                    imageUpdate = NoteImageUpdate.Replace(newFileName),
                ),
            ),
        )

        assertEquals(newFileName, dao.notes.value.single().imageFileName)
        assertEquals(listOf(newFileName), imageStorage.promotedFileNames)
        assertEquals(listOf(oldFileName), imageStorage.deletedFileNames)
        assertEquals(
            listOf(
                "image:promote:$newFileName",
                "room:update",
                "image:delete:$oldFileName",
            ),
            operationLog,
        )
    }

    @Test
    fun `failed Room replacement removes new image and preserves old reference`() = runTest {
        val oldFileName = "00000000-0000-0000-0000-000000000010.jpg"
        val newFileName = imageStorage.nextStagedFileName
        dao.notes.value = listOf(
            NoteEntity(
                id = 9,
                title = "Старая",
                content = "Текст",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = oldFileName,
            ),
        )
        dao.updateFailure = IllegalStateException("Room write failed")

        val result = runCatching {
            repository.updateNote(
                NoteUpdate(
                    id = 9,
                    title = "Новая",
                    content = "Текст",
                    generatedTitleNumber = null,
                    imageUpdate = NoteImageUpdate.Replace(newFileName),
                ),
            )
        }

        assertEquals(true, result.isFailure)
        assertEquals(oldFileName, dao.notes.value.single().imageFileName)
        assertEquals(listOf(newFileName), imageStorage.deletedFileNames)
    }

    @Test
    fun `removing attachment clears Room reference and removes old file`() = runTest {
        val oldFileName = "00000000-0000-0000-0000-000000000010.jpg"
        dao.notes.value = listOf(
            NoteEntity(
                id = 9,
                title = "Заметка",
                content = "Текст",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = oldFileName,
            ),
        )

        repository.updateNote(
            NoteUpdate(
                id = 9,
                title = "Заметка",
                content = "Текст",
                generatedTitleNumber = null,
                imageUpdate = NoteImageUpdate.Remove,
            ),
        )

        assertEquals(null, dao.notes.value.single().imageFileName)
        assertEquals(listOf(oldFileName), imageStorage.deletedFileNames)
        assertEquals(
            listOf("room:update", "image:delete:$oldFileName"),
            operationLog,
        )
    }

    @Test
    fun `deleting note removes its image after Room deletion`() = runTest {
        val imageFileName = "00000000-0000-0000-0000-000000000010.jpg"
        dao.notes.value = listOf(
            NoteEntity(
                id = 3,
                title = "Заметка",
                content = "",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = imageFileName,
            ),
        )

        assertEquals(true, repository.deleteNote(3))

        assertEquals(emptyList<NoteEntity>(), dao.notes.value)
        assertEquals(listOf(imageFileName), imageStorage.deletedFileNames)
        assertEquals(
            listOf("room:delete", "image:delete:$imageFileName"),
            operationLog,
        )
    }

    @Test
    fun `file deletion failure leaves Room deletion successful for later cleanup`() = runTest {
        val imageFileName = "00000000-0000-0000-0000-000000000010.jpg"
        dao.notes.value = listOf(
            NoteEntity(
                id = 3,
                title = "Заметка",
                content = "",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = imageFileName,
            ),
        )
        imageStorage.deleteFailure = IllegalStateException("File is busy")

        assertEquals(true, repository.deleteNote(3))
        assertEquals(emptyList<NoteEntity>(), dao.notes.value)
    }

    @Test
    fun `cleanup passes Room references and stale staging cutoff to storage`() = runTest {
        dao.notes.value = listOf(
            NoteEntity(
                id = 3,
                title = "Заметка",
                content = "",
                createdAtMillis = 100,
                generatedTitleNumber = null,
                imageFileName = "referenced.jpg",
            ),
        )
        currentTimeMillis = 100_000_000L

        repository.cleanupOrphanedImages()

        assertEquals(
            FakeNoteImageStorage.CleanupCall(
                referencedFileNames = setOf("referenced.jpg"),
                staleStagingCutoffMillis = 13_600_000L,
            ),
            imageStorage.cleanupCalls.single(),
        )
    }
}

private class FakeNotesDao(
    private val operationLog: MutableList<String>,
) : NotesDao {
    val notes = MutableStateFlow<List<NoteEntity>>(emptyList())
    val inserted = mutableListOf<NoteEntity>()
    var nextNumber = 1
    var insertFailure: Throwable? = null
    var updateFailure: Throwable? = null

    override fun observeNotes(): Flow<List<NoteEntity>> = notes

    override fun observeNote(noteId: Long): Flow<NoteEntity?> = notes.map { notes ->
        notes.find { it.id == noteId }
    }

    override suspend fun getNote(noteId: Long): NoteEntity? =
        notes.value.find { it.id == noteId }

    override suspend fun getImageFileNames(): List<String> =
        notes.value.mapNotNull(NoteEntity::imageFileName)

    override suspend fun insert(note: NoteEntity): Long {
        insertFailure?.let { throw it }
        operationLog += "room:insert"
        inserted += note
        return inserted.size.toLong()
    }

    override suspend fun update(
        noteId: Long,
        title: String,
        content: String,
        generatedTitleNumber: Int?,
        updatedAtMillis: Long,
        imageFileName: String?,
    ): Int {
        updateFailure?.let { throw it }
        operationLog += "room:update"
        val existing = notes.value.find { it.id == noteId } ?: return 0
        notes.value = notes.value.map { storedNote ->
            if (storedNote.id == noteId) {
                existing.copy(
                    title = title,
                    content = content,
                    generatedTitleNumber = generatedTitleNumber,
                    updatedAtMillis = updatedAtMillis,
                    imageFileName = imageFileName,
                )
            } else {
                storedNote
            }
        }
        return 1
    }

    override suspend fun delete(noteId: Long): Int {
        operationLog += "room:delete"
        val newNotes = notes.value.filterNot { it.id == noteId }
        if (newNotes.size == notes.value.size) return 0
        notes.value = newNotes
        return 1
    }

    override suspend fun nextGeneratedTitleNumber(): Int = nextNumber
}
