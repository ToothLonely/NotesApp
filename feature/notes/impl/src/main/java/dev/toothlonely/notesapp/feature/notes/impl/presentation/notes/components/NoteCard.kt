package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.icon.NotesAppIcons
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun NoteCard(
    note: Note,
    createdDateFormat: String,
    deleteDescription: String,
    isDeleteMode: Boolean,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
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
        Row(
            modifier = Modifier.padding(NotesAppSpacing.space3),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier
                    .size(
                        width = NotesAppSizes.noteListPreviewWidth,
                        height = NotesAppSizes.noteListPreviewHeight,
                    ),
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = NotesAppIcons.Note,
                        contentDescription = null,
                        modifier = Modifier.size(NotesAppSizes.standardIcon),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space1),
            ) {
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
            if (isDeleteMode) {
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
                            imageVector = NotesAppIcons.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteCardPreview() {
    NotesAppTheme {
        NoteCard(
            note = Note(1, "Первая заметка", "Текст", 1_750_000_000_000),
            createdDateFormat = "Создано %s",
            deleteDescription = "Удалить заметку «Первая заметка»",
            isDeleteMode = true,
            isDeleting = false,
            onOpen = {},
            onDelete = {},
        )
    }
}
