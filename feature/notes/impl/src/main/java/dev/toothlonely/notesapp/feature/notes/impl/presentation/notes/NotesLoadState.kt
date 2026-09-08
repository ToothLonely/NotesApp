package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note

internal sealed interface NotesLoadState {
    val notesRevision: Long
    val scrollToStart: Boolean

    data object Loading : NotesLoadState {
        override val notesRevision: Long = 0L
        override val scrollToStart: Boolean = false
    }

    data object Error : NotesLoadState {
        override val notesRevision: Long = 0L
        override val scrollToStart: Boolean = false
    }

    data class Loaded(
        val notes: List<Note>,
        override val notesRevision: Long,
        override val scrollToStart: Boolean,
    ) : NotesLoadState
}
