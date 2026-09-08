package dev.toothlonely.notesapp.feature.notes.impl.domain.repository

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun observeNotes(): Flow<List<Note>>

    fun observeNotesWindow(limit: Int, sortOrder: NotesSortOrder): Flow<List<Note>>

    fun observeNote(noteId: Long): Flow<Note?>

    suspend fun nextGeneratedTitleNumber(): Int

    suspend fun createNote(note: NewNote)

    suspend fun updateNote(note: NoteUpdate): Boolean

    suspend fun deleteNote(noteId: Long): Boolean

    suspend fun cleanupOrphanedImages()
}
