package dev.toothlonely.notesapp.feature.notes.impl.domain.repository

import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode
import kotlinx.coroutines.flow.Flow

interface NotesViewModeRepository {
    fun observeViewMode(): Flow<NotesViewMode>

    suspend fun setViewMode(viewMode: NotesViewMode)
}
