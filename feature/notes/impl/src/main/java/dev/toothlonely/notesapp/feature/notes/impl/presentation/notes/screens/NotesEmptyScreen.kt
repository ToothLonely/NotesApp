package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.icon.NotesAppIcons
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun NotesEmptyScreen(
    title: String,
    description: String,
    actionLabel: String,
    onCreateNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotesAppSpacing.space3,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            imageVector = NotesAppIcons.Note,
            contentDescription = null,
            modifier = Modifier.size(NotesAppSizes.emptyStateIcon),
            tint = MaterialTheme.colorScheme.outline,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            onClick = onCreateNote,
            modifier = Modifier.heightIn(min = NotesAppSizes.buttonMinimumHeight),
            shape = NotesAppShapes.full,
        ) {
            Text(text = actionLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesEmptyScreenPreview() {
    NotesAppTheme {
        NotesEmptyScreen(
            title = "Заметок пока нет",
            description = "Создайте первую заметку",
            actionLabel = "Создать заметку",
            onCreateNote = {},
        )
    }
}
