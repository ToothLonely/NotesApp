package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.Note

sealed interface NotesUiState {
    data object Loading : NotesUiState

    data object Empty : NotesUiState

    data class Content(
        val notes: List<Note>,
    ) : NotesUiState

    data object Error : NotesUiState
}
