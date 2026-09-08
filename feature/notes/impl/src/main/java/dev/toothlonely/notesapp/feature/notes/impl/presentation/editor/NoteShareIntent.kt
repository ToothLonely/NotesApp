package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import android.content.Context
import android.content.Intent

internal fun Context.shareNote(
    title: String,
    body: String,
    chooserTitle: String,
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = NOTE_SHARE_MIME_TYPE
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, buildNoteShareText(title = title, body = body))
    }
    startActivity(Intent.createChooser(sendIntent, chooserTitle))
}

private const val NOTE_SHARE_MIME_TYPE = "text/plain"
