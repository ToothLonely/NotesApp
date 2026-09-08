package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder

internal data class SearchCriteria(
    val appliedQuery: String,
    val sortOrder: NotesSortOrder,
    val visibleLimit: Int = NOTES_PAGE_SIZE,
    val isSearchPending: Boolean = false,
)
