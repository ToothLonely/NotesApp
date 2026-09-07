package dev.toothlonely.notesapp.feature.tasks.impl.data.repository

fun interface TaskTimeProvider {
    fun currentTimeMillis(): Long
}
