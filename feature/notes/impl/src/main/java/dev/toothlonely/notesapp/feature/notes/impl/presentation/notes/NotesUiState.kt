package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode

data class NotesUiState(
    val content: NotesContentState = NotesContentState.Loading,
    val notesRevision: Long = 0L,
    val scrollToStartOnNotesRevision: Boolean = false,
    val draftQuery: String = "",
    val appliedQuery: String = "",
    val sortOrder: NotesSortOrder = NotesSortOrder.NewestFirst,
    val viewMode: NotesViewMode = NotesViewMode.List,
    val isViewModeSaving: Boolean = false,
    val hasViewModeSaveError: Boolean = false,
    val isDeleteMode: Boolean = false,
    val deleteConfirmation: DeleteNoteConfirmationUiState? = null,
    val deletingNoteIds: Set<Long> = emptySet(),
    val failedDeleteNoteId: Long? = null,
)

sealed interface NotesContentState {
    data object SearchPending : NotesContentState

    data object Loading : NotesContentState

    data object Empty : NotesContentState

    data object SearchEmpty : NotesContentState

    data class Content(
        val notes: List<Note>,
        val hasMore: Boolean = false,
        val visibleLimit: Int = NOTES_PAGE_SIZE,
    ) : NotesContentState

    data object Error : NotesContentState
}
