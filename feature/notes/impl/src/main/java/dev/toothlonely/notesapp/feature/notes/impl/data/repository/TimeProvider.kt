package dev.toothlonely.notesapp.feature.notes.impl.data.repository

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}
