package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

sealed interface NoteEditorEvent {
    data object SaveSucceeded : NoteEditorEvent

    data object CloseEditor : NoteEditorEvent
}
