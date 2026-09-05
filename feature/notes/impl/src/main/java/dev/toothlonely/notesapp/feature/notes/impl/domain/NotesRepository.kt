package dev.toothlonely.notesapp.feature.notes.impl.domain

import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    suspend fun nextGeneratedTitleNumber(): Int

    suspend fun createNote(note: NewNote)
}
