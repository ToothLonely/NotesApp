package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

data class DeleteNoteConfirmationUiState(
    val noteId: Long,
    val noteTitle: String,
    val isDeleting: Boolean = false,
)
