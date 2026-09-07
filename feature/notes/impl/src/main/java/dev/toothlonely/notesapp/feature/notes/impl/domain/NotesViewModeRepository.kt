package dev.toothlonely.notesapp.feature.notes.impl.domain

import kotlinx.coroutines.flow.Flow

interface NotesViewModeRepository {
    fun observeViewMode(): Flow<NotesViewMode>

    suspend fun setViewMode(viewMode: NotesViewMode)
}
