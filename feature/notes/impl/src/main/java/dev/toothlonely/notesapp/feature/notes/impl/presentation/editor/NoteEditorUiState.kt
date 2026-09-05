package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

sealed interface NoteEditorUiState {
    data object Loading : NoteEditorUiState

    data class Content(
        val title: String,
        val body: String,
        val generatedTitleNumber: Int,
        val resolvedGeneratedTitleNumber: Int? = null,
        val isSaving: Boolean = false,
        val hasSaveError: Boolean = false,
    ) : NoteEditorUiState {
        val isSaveEnabled: Boolean
            get() = (title.isNotBlank() || body.isNotBlank()) && !isSaving
    }

    data object Error : NoteEditorUiState
}
