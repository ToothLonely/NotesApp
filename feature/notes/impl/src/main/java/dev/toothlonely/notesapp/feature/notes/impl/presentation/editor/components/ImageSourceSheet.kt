package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSourceSheet(
    title: String,
    chooseFileLabel: String,
    takePhotoLabel: String,
    onChooseFile: () -> Unit,
    onTakePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(bottom = NotesAppSpacing.space4)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = NotesAppSpacing.space4),
            )
            ListItem(
                headlineContent = { Text(chooseFileLabel) },
                leadingContent = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_image_24),
                        contentDescription = null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable(onClick = onChooseFile),
            )
            ListItem(
                headlineContent = { Text(takePhotoLabel) },
                leadingContent = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_photo_camera_24),
                        contentDescription = null,
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable(onClick = onTakePhoto),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ImageSourceSheetPreview() {
    NotesAppTheme {
        ImageSourceSheet(
            title = "Добавить изображение",
            chooseFileLabel = "Выбрать файл",
            takePhotoLabel = "Сделать фото",
            onChooseFile = {},
            onTakePhoto = {},
            onDismiss = {},
        )
    }
}
