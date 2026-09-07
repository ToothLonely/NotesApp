package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.NotesViewMode

data class NotesUiState(
    val content: NotesContentState = NotesContentState.Loading,
    val draftQuery: String = "",
    val appliedQuery: String = "",
    val sortOrder: NotesSortOrder = NotesSortOrder.NewestFirst,
    val viewMode: NotesViewMode = NotesViewMode.List,
    val isViewModeSaving: Boolean = false,
    val hasViewModeSaveError: Boolean = false,
    val isDeleteMode: Boolean = false,
    val deletingNoteIds: Set<Long> = emptySet(),
    val failedDeleteNoteId: Long? = null,
)

sealed interface NotesContentState {
    data object Loading : NotesContentState

    data object Empty : NotesContentState

    data object SearchEmpty : NotesContentState

    data class Content(
        val notes: List<Note>,
    ) : NotesContentState

    data object Error : NotesContentState
}
