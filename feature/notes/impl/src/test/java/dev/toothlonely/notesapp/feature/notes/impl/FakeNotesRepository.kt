package dev.toothlonely.notesapp.feature.notes.impl

import dev.toothlonely.notesapp.feature.notes.impl.domain.NewNote
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NoteImageUpdate
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNotesRepository : NotesRepository {
    val notes = MutableStateFlow<List<Note>>(emptyList())
    val createdNotes = mutableListOf<NewNote>()
    val updatedNotes = mutableListOf<NoteUpdate>()
    val deletedNoteIds = mutableListOf<Long>()
    var nextTitleNumber = 1
    var observeFailure: Throwable? = null
    var observeNoteFailure: Throwable? = null
    var preparationFailure: Throwable? = null
    var saveFailure: Throwable? = null
    var updateFailure: Throwable? = null
    var deleteFailure: Throwable? = null
    var deleteGate: CompletableDeferred<Unit>? = null
    var currentTimeMillis = 1_000L
    var cleanupCount = 0

    override fun observeNotes(): Flow<List<Note>> {
        observeFailure?.let { throw it }
        return notes.map { notes ->
            notes.sortedWith(
                compareByDescending<Note>(Note::updatedAtMillis)
                    .thenByDescending(Note::id),
            )
        }
    }

    override fun observeNote(noteId: Long): Flow<Note?> {
        observeNoteFailure?.let { throw it }
        return notes.map { notes -> notes.find { it.id == noteId } }
    }

    override suspend fun nextGeneratedTitleNumber(): Int {
        preparationFailure?.let { throw it }
        return nextTitleNumber
    }

    override suspend fun createNote(note: NewNote) {
        saveFailure?.let { throw it }
        createdNotes += note
    }

    override suspend fun updateNote(note: NoteUpdate): Boolean {
        updateFailure?.let { throw it }
        val existingNote = notes.value.find { it.id == note.id } ?: return false
        updatedNotes += note
        notes.value = notes.value.map { currentNote ->
            if (currentNote.id == note.id) {
                existingNote.copy(
                    title = note.title,
                    content = note.content,
                    generatedTitleNumber = note.generatedTitleNumber,
                    imageFileName = when (val imageUpdate = note.imageUpdate) {
                        NoteImageUpdate.Keep -> existingNote.imageFileName
                        NoteImageUpdate.Remove -> null
                        is NoteImageUpdate.Replace -> imageUpdate.stagedFileName
                    },
                    updatedAtMillis = currentTimeMillis,
                )
            } else {
                currentNote
            }
        }
        return true
    }

    override suspend fun deleteNote(noteId: Long): Boolean {
        deleteFailure?.let { throw it }
        deleteGate?.await()
        if (notes.value.none { it.id == noteId }) return false
        deletedNoteIds += noteId
        notes.value = notes.value.filterNot { it.id == noteId }
        return true
    }

    override suspend fun cleanupOrphanedImages() {
        cleanupCount += 1
    }
}
