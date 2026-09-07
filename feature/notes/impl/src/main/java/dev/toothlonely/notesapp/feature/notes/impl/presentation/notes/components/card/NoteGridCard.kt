package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.StoredNoteImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NoteGridCard(
    note: Note,
    createdDateFormat: String,
    deleteDescription: String,
    isDeleteMode: Boolean,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    val formattedDate = remember(note.createdAtMillis) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(
            Instant.ofEpochMilli(note.createdAtMillis).atZone(ZoneId.systemDefault()),
        )
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isDeleteMode) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onOpen)
                },
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box {
                StoredNoteImage(
                    fileName = note.imageFileName,
                    staged = false,
                    contentDescription = null,
                    loadImage = loadImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(NOTE_PREVIEW_ASPECT_RATIO)
                        .clip(MaterialTheme.shapes.extraSmall),
                )
                if (isDeleteMode) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(NotesAppSpacing.space2),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ) {
                        IconButton(
                            onClick = onDelete,
                            enabled = !isDeleting,
                            modifier = Modifier
                                .size(NotesAppSizes.minimumTouchTarget)
                                .semantics { contentDescription = deleteDescription },
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(NotesAppSizes.standardIcon),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Icon(
                                    painter = painterResource(DesignSystemR.drawable.ic_delete_24),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(NotesAppSpacing.space3)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = createdDateFormat.format(formattedDate),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private const val NOTE_PREVIEW_ASPECT_RATIO = 4f / 3f

@Preview(showBackground = true)
@Composable
private fun NoteGridCardPreview() {
    NotesAppTheme {
        NoteGridCard(
            note = Note(1, "Первая заметка", "Текст", 1_750_000_000_000),
            createdDateFormat = "Создано %s",
            deleteDescription = "Удалить заметку «Первая заметка»",
            isDeleteMode = true,
            isDeleting = false,
            onOpen = {},
            onDelete = {},
            loadImage = { _, _, _, _ -> null },
        )
    }
}
