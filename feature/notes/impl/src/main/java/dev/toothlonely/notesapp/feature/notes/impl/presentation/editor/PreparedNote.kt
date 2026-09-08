package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage

internal data class PreparedNote(
    val title: String,
    val body: String,
    val generatedTitleNumber: Int,
    val resolvedGeneratedTitleNumber: Int?,
    val image: EditorImage.Persisted?,
)
