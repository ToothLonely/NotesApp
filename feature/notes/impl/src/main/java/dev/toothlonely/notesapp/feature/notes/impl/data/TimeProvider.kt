package dev.toothlonely.notesapp.feature.notes.impl.data

fun interface TimeProvider {
    fun currentTimeMillis(): Long
}
