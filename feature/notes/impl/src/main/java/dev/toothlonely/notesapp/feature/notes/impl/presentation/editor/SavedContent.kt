package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage

internal data class SavedContent(
    val title: String,
    val body: String,
    val resolvedGeneratedTitleNumber: Int?,
    val image: EditorImage.Persisted?,
)
