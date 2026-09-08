package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode

internal data class NotesControlsState(
    val draftQuery: String = "",
    val appliedQuery: String = "",
    val sortOrder: NotesSortOrder = NotesSortOrder.NewestFirst,
    val isViewModeSaving: Boolean = false,
    val failedViewMode: NotesViewMode? = null,
    val isDeleteMode: Boolean = false,
    val deletingNoteIds: Set<Long> = emptySet(),
    val failedDeleteNoteId: Long? = null,
    val deleteConfirmation: DeleteNoteConfirmationUiState? = null,
)
