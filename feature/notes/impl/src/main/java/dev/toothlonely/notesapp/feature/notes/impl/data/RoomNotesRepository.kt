package dev.toothlonely.notesapp.feature.notes.impl.data

import dev.toothlonely.notesapp.core.data.database.NotesDao
import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNotesRepository(
    private val notesDao: NotesDao,
    private val timeProvider: TimeProvider,
) : NotesRepository {
    override fun observeNotes(): Flow<List<Note>> = notesDao.observeNotes().map { notes ->
        notes.map { it.asExternalModel() }
    }

    override suspend fun nextGeneratedTitleNumber(): Int = notesDao.nextGeneratedTitleNumber()

    override suspend fun createNote(note: NewNote) {
        notesDao.insert(note.asEntity(createdAtMillis = timeProvider.currentTimeMillis()))
    }
}
