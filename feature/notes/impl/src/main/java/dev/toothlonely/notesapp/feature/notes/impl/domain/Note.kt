package dev.toothlonely.notesapp.feature.notes.impl.domain

data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val createdAtMillis: Long,
    val generatedTitleNumber: Int? = null,
    val updatedAtMillis: Long = createdAtMillis,
)
