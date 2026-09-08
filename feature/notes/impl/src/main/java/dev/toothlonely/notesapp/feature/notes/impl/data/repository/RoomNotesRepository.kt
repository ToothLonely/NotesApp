package dev.toothlonely.notesapp.feature.notes.impl.data.repository

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteImageUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NotesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RoomNotesRepository(
    private val notesDao: NotesDao,
    private val timeProvider: TimeProvider,
    private val imageStorage: NoteImageStorage,
) : NotesRepository {
    private val imageOperationMutex = Mutex()

    override fun observeNotes(): Flow<List<Note>> = notesDao.observeNotes().map { notes ->
        notes.map { it.asDomainModel() }
    }

    override fun observeNotesWindow(limit: Int, sortOrder: NotesSortOrder): Flow<List<Note>> {
        require(limit > 0)
        val notes = when (sortOrder) {
            NotesSortOrder.NewestFirst -> notesDao.observeNewestNotes(limit)
            NotesSortOrder.OldestFirst -> notesDao.observeOldestNotes(limit)
        }
        return notes.map { entities -> entities.map { it.asDomainModel() } }
    }

    override fun observeNote(noteId: Long): Flow<Note?> = notesDao.observeNote(noteId).map { note ->
        note?.asDomainModel()
    }

    override suspend fun nextGeneratedTitleNumber(): Int = notesDao.nextGeneratedTitleNumber()

    override suspend fun createNote(note: NewNote): Unit = imageOperationMutex.withLock {
        var promotedImageFileName: String? = null
        try {
            promotedImageFileName = note.stagedImageFileName?.let { stagedFileName ->
                imageStorage.promote(stagedFileName)
                stagedFileName
            }
            val createdAtMillis = timeProvider.currentTimeMillis()
            notesDao.insert(
                note.asEntity(
                    createdAtMillis = createdAtMillis,
                    imageFileName = promotedImageFileName,
                ),
            )
            Unit
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            promotedImageFileName?.let { fileName ->
                runCatching { imageStorage.delete(fileName) }
            }
            throw error
        }
    }

    override suspend fun updateNote(note: NoteUpdate): Boolean = imageOperationMutex.withLock {
        val existingNote = notesDao.getNote(note.id) ?: return@withLock false
        var promotedImageFileName: String? = null
        val newImageFileName = when (val imageUpdate = note.imageUpdate) {
            NoteImageUpdate.Keep -> existingNote.imageFileName
            NoteImageUpdate.Remove -> null
            is NoteImageUpdate.Replace -> imageUpdate.stagedFileName.also { stagedFileName ->
                imageStorage.promote(stagedFileName)
                promotedImageFileName = stagedFileName
            }
        }

        val updated = try {
            notesDao.update(
                noteId = note.id,
                title = note.title,
                content = note.content,
                generatedTitleNumber = note.generatedTitleNumber,
                updatedAtMillis = timeProvider.currentTimeMillis(),
                imageFileName = newImageFileName,
            ) > 0
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            promotedImageFileName?.let { fileName ->
                runCatching { imageStorage.delete(fileName) }
            }
            throw error
        }

        if (!updated) {
            promotedImageFileName?.let { fileName ->
                runCatching { imageStorage.delete(fileName) }
            }
            return@withLock false
        }
        val oldImageFileName = existingNote.imageFileName
        if (oldImageFileName != null && oldImageFileName != newImageFileName) {
            runCatching { imageStorage.delete(oldImageFileName) }
        }
        true
    }

    override suspend fun deleteNote(noteId: Long): Boolean = imageOperationMutex.withLock {
        val existingNote = notesDao.getNote(noteId) ?: return@withLock false
        val deleted = notesDao.delete(noteId) > 0
        if (deleted) {
            existingNote.imageFileName?.let { fileName ->
                runCatching { imageStorage.delete(fileName) }
            }
        }
        deleted
    }

    override suspend fun cleanupOrphanedImages() = imageOperationMutex.withLock {
        imageStorage.cleanup(
            referencedFileNames = notesDao.getImageFileNames().toSet(),
            staleStagingCutoffMillis =
                timeProvider.currentTimeMillis() - STALE_STAGING_AGE_MILLIS,
        )
    }

    private companion object {
        const val STALE_STAGING_AGE_MILLIS = 24L * 60L * 60L * 1_000L
    }
}
