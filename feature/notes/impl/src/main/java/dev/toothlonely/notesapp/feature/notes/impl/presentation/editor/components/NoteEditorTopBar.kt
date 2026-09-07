package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorTopBar(
    title: String,
    placeholder: String,
    titleLabel: String,
    backLabel: String,
    attachmentLabel: String,
    attachmentThumbnailDescription: String,
    imageFileName: String?,
    isImageStaged: Boolean,
    enabled: Boolean,
    attachmentActionsEnabled: Boolean,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onImageLoadError: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onAttachmentClick: () -> Unit,
    onAttachmentThumbnailClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            InlineNoteTitleField(
                title = title,
                placeholder = placeholder,
                label = titleLabel,
                enabled = enabled,
                onTitleChanged = onTitleChanged,
            )
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
                        staged = isImageStaged,
                        description = attachmentThumbnailDescription,
                        enabled = attachmentActionsEnabled,
                        loadImage = loadImage,
                        onLoadError = onImageLoadError,
                        onClick = onAttachmentThumbnailClick,
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onAttachmentClick,
                enabled = attachmentActionsEnabled,
            ) {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_attach_file_24),
                    contentDescription = attachmentLabel,
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
private fun NoteEditorTopBarPreview() {
    NotesAppTheme {
        NoteEditorTopBar(
            title = "",
            placeholder = "Заголовок вашей заметки",
            titleLabel = "Заголовок заметки",
            backLabel = "Назад",
            attachmentLabel = "Добавить изображение",
            attachmentThumbnailDescription =
                "Прикреплённое изображение. Открыть действия",
            imageFileName = null,
            isImageStaged = false,
            enabled = true,
            attachmentActionsEnabled = true,
            loadImage = { _, _, _, _ -> null },
            onImageLoadError = {},
            onTitleChanged = {},
            onAttachmentClick = {},
            onAttachmentThumbnailClick = {},
            onBack = {},
        )
    }
}
