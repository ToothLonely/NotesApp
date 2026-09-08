package dev.toothlonely.notesapp.feature.tasks.impl.domain.model

data class Task(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long = createdAtMillis,
)
