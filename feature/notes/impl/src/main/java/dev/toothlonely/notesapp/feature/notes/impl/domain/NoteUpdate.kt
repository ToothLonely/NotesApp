package dev.toothlonely.notesapp.feature.notes.impl.domain

data class NoteUpdate(
    val id: Long,
    val title: String,
    val content: String,
    val generatedTitleNumber: Int?,
)
