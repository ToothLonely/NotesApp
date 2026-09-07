package dev.toothlonely.notesapp.feature.notes.impl.domain

import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(noteId: Long): Flow<Note?>

    suspend fun nextGeneratedTitleNumber(): Int

    suspend fun createNote(note: NewNote)

    suspend fun updateNote(note: NoteUpdate): Boolean

    suspend fun deleteNote(noteId: Long): Boolean

    suspend fun cleanupOrphanedImages()
}
