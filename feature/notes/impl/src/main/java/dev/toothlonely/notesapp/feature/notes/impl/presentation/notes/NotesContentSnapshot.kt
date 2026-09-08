package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

internal data class NotesContentSnapshot(
    val content: NotesContentState,
    val notesRevision: Long,
    val scrollToStart: Boolean,
)
