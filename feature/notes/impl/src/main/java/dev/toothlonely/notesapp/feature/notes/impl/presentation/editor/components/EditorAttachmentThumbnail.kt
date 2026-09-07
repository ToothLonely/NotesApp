package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.StoredNoteImage

@Composable
fun EditorAttachmentThumbnail(
    fileName: String,
    staged: Boolean,
    description: String,
    enabled: Boolean,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onLoadError: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(NotesAppSizes.minimumTouchTarget)
            .semantics { contentDescription = description }
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        StoredNoteImage(
            fileName = fileName,
            staged = staged,
            contentDescription = null,
            loadImage = loadImage,
            onLoadError = onLoadError,
            modifier = Modifier
                .size(NotesAppSizes.editorAttachmentThumbnail)
                .clip(CircleShape),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorAttachmentThumbnailPreview() {
    NotesAppTheme {
        EditorAttachmentThumbnail(
            fileName = "preview.jpg",
            staged = false,
            description = "Прикреплённое изображение. Открыть действия",
            enabled = true,
            loadImage = { _, _, _, _ -> null },
            onLoadError = {},
            onClick = {},
        )
    }
}
