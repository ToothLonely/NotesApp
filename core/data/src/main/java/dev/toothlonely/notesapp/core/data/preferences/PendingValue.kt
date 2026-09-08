package dev.toothlonely.notesapp.core.data.preferences

internal data class PendingValue<T>(
    val operationId: Long,
    val value: T,
)
