package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.topbar

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.attachment.EditorAttachmentThumbnail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteReadingTopBar(
    title: String,
    backLabel: String,
    shareLabel: String,
    attachmentThumbnailDescription: String,
    imageFileName: String?,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onImageLoadError: () -> Unit,
    onAttachmentThumbnailClick: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            SelectionContainer {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            Row {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_arrow_back_24),
                        contentDescription = backLabel,
                    )
                }
                if (imageFileName != null) {
                    EditorAttachmentThumbnail(
                        fileName = imageFileName,
                        staged = false,
                        description = attachmentThumbnailDescription,
                        enabled = true,
                        loadImage = loadImage,
                        onLoadError = onImageLoadError,
                        onClick = onAttachmentThumbnailClick,
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_share_24),
                    contentDescription = shareLabel,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NoteReadingTopBarPreview() {
    NotesAppTheme {
        NoteReadingTopBar(
            title = "Идеи для путешествия",
            backLabel = "Назад",
            shareLabel = "Поделиться заметкой",
            attachmentThumbnailDescription =
                "Прикреплённое изображение. Открыть действия",
            imageFileName = null,
            loadImage = { _, _, _, _ -> null },
            onImageLoadError = {},
            onAttachmentThumbnailClick = {},
            onShare = {},
            onBack = {},
        )
    }
}
