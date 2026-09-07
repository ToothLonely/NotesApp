package dev.toothlonely.notesapp.feature.notes.impl.domain.model

data class NewNote(
    val title: String,
    val content: String,
    val generatedTitleNumber: Int?,
    val stagedImageFileName: String? = null,
)
