package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotesRepository(
    private val notesDao: NotesDao,
    private val timeProvider: TimeProvider,
) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> = notesDao.observeNotes().map { notes ->
        notes.map { it.asDomainModel() }
    }

    override fun observeNote(noteId: Long): Flow<Note?> = notesDao.observeNote(noteId).map { note ->
        note?.asDomainModel()
    }

    override suspend fun nextGeneratedTitleNumber(): Int = notesDao.nextGeneratedTitleNumber()

    override suspend fun createNote(note: NewNote) {
        val createdAtMillis = timeProvider.currentTimeMillis()
        notesDao.insert(note.asEntity(createdAtMillis = createdAtMillis))
    }

    override suspend fun updateNote(note: NoteUpdate): Boolean = notesDao.update(
        noteId = note.id,
        title = note.title,
        content = note.content,
        generatedTitleNumber = note.generatedTitleNumber,
        updatedAtMillis = timeProvider.currentTimeMillis(),
    ) > 0

    override suspend fun deleteNote(noteId: Long): Boolean = notesDao.delete(noteId) > 0
}
