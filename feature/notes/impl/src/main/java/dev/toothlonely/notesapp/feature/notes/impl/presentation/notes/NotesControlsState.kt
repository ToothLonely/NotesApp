package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode

internal data class NotesControlsState(
    val draftQuery: String = "",
    val appliedQuery: String = "",
    val isSearchPending: Boolean = false,
    val visibleLimit: Int = NOTES_PAGE_SIZE,
    val sortOrder: NotesSortOrder = NotesSortOrder.NewestFirst,
    val isViewModeSaving: Boolean = false,
    val failedViewMode: NotesViewMode? = null,
    val isDeleteMode: Boolean = false,
    val deletingNoteIds: Set<Long> = emptySet(),
    val failedDeleteNoteId: Long? = null,
    val deleteConfirmation: DeleteNoteConfirmationUiState? = null,
)

internal const val NOTES_PAGE_SIZE = 20
