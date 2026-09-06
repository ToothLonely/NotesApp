package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

sealed interface NotesEvent {
    data object NoteDeleted : NotesEvent
}
