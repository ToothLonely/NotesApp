package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

sealed interface EditorImage {
    val fileName: String

    data class Persisted(
        override val fileName: String,
    ) : EditorImage

    data class Staged(
        override val fileName: String,
    ) : EditorImage
}
