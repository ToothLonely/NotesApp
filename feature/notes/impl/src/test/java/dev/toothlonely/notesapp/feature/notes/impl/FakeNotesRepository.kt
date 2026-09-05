package dev.toothlonely.notesapp.feature.notes.impl

import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNotesRepository : NotesRepository {
    val notes = MutableStateFlow<List<Note>>(emptyList())
    val createdNotes = mutableListOf<NewNote>()
    var nextTitleNumber = 1
    var observeFailure: Throwable? = null
    var preparationFailure: Throwable? = null
    var saveFailure: Throwable? = null

    override fun observeNotes(): Flow<List<Note>> {
        observeFailure?.let { throw it }
        return notes
    }

    override suspend fun nextGeneratedTitleNumber(): Int {
        preparationFailure?.let { throw it }
        return nextTitleNumber
    }

    override suspend fun createNote(note: NewNote) {
        saveFailure?.let { throw it }
        createdNotes += note
    }
}
