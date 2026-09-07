package dev.toothlonely.notesapp.feature.notes.impl.domain.model

sealed interface NoteImageUpdate {
    data object Keep : NoteImageUpdate

    data object Remove : NoteImageUpdate

    data class Replace(
        val stagedFileName: String,
    ) : NoteImageUpdate
}
