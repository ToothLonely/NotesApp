package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.StoredNoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageAttachmentActionsSheet(
    fileName: String,
    staged: Boolean,
    previewDescription: String,
    replaceLabel: String,
    deleteLabel: String,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onImageLoadError: () -> Unit,
    onReplace: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(
                start = NotesAppSpacing.space4,
                end = NotesAppSpacing.space4,
                bottom = NotesAppSpacing.space4,
            ),
            verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space3),
        ) {
            StoredNoteImage(
                fileName = fileName,
                staged = staged,
                contentDescription = previewDescription,
                loadImage = loadImage,
                onLoadError = onImageLoadError,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(IMAGE_PREVIEW_ASPECT_RATIO)
                    .clip(MaterialTheme.shapes.extraLarge),
            )
            Button(
                onClick = onReplace,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(replaceLabel)
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(deleteLabel, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

private const val IMAGE_PREVIEW_ASPECT_RATIO = 4f / 3f

@Preview(showBackground = true)
@Composable
private fun ImageAttachmentActionsSheetPreview() {
    NotesAppTheme {
        ImageAttachmentActionsSheet(
            fileName = "preview.jpg",
            staged = false,
            previewDescription = "Прикреплённое изображение",
            replaceLabel = "Заменить изображение",
            deleteLabel = "Удалить изображение",
            loadImage = { _, _, _, _ -> null },
            onImageLoadError = {},
            onReplace = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}
