package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorMode

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
        val voiceInput: NoteVoiceInputUiState = NoteVoiceInputUiState.Idle,
        val isSaving: Boolean = false,
        val saveError: NoteEditorSaveError? = null,
    ) : NoteEditorUiState {
        val isSaveEnabled: Boolean
            get() = mode != NoteEditorMode.Reading &&
                (title.isNotBlank() || body.isNotBlank()) &&
                !isSaving &&
                !isProcessingImage &&
                !isClosing &&
                !voiceInput.isBusy
    }

    data object NotFound : NoteEditorUiState

    data object Error : NoteEditorUiState
}

sealed interface NoteVoiceInputUiState {
    data object Idle : NoteVoiceInputUiState

    data class Recording(
        val durationSeconds: Int = 0,
    ) : NoteVoiceInputUiState

    data object Processing : NoteVoiceInputUiState

    data class PermissionDenied(
        val canRequestAgain: Boolean,
    ) : NoteVoiceInputUiState

    data class Error(
        val failure: SpeechRecognitionFailure,
    ) : NoteVoiceInputUiState
}

val NoteVoiceInputUiState.isBusy: Boolean
    get() = this is NoteVoiceInputUiState.Recording ||
        this is NoteVoiceInputUiState.Processing

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
