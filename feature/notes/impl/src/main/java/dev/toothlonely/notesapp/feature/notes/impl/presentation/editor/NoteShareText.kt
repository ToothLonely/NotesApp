package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

internal fun buildNoteShareText(
    title: String,
    body: String,
): String = if (body.isBlank()) {
    title
} else {
    "$title\n\n$body"
}
