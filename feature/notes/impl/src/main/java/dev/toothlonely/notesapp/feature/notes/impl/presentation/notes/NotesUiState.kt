package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.Note

data class NotesUiState(
    val content: NotesContentState = NotesContentState.Loading,
    val isDeleteMode: Boolean = false,
    val deletingNoteIds: Set<Long> = emptySet(),
    val failedDeleteNoteId: Long? = null,
)

sealed interface NotesContentState {
    data object Loading : NotesContentState

    data object Empty : NotesContentState

    data class Content(
        val notes: List<Note>,
    ) : NotesContentState

    data object Error : NotesContentState
}
