package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

sealed interface NoteEditorUiState {
    data class Loading(
        val isExistingNote: Boolean,
    ) : NoteEditorUiState

    data class Content(
        val mode: NoteEditorMode = NoteEditorMode.Creating,
        val title: String,
        val body: String,
        val generatedTitleNumber: Int,
        val resolvedGeneratedTitleNumber: Int? = null,
        val image: EditorImage? = null,
        val isProcessingImage: Boolean = false,
        val isClosing: Boolean = false,
        val attachmentError: NoteEditorAttachmentError? = null,
        val isSaving: Boolean = false,
        val saveError: NoteEditorSaveError? = null,
    ) : NoteEditorUiState {
        val isSaveEnabled: Boolean
            get() = mode != NoteEditorMode.Reading &&
                (title.isNotBlank() || body.isNotBlank()) &&
                !isSaving &&
                !isProcessingImage &&
                !isClosing
    }

    data object NotFound : NoteEditorUiState

    data object Error : NoteEditorUiState
}

enum class NoteEditorAttachmentError {
    SelectedImageUnavailable,
    StoredImageUnavailable,
    InputTooLarge,
    UnsupportedImage,
    WriteFailed,
}

enum class NoteEditorSaveError {
    Note,
    Image,
}
