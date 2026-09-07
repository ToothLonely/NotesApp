package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun TasksErrorScreen(
    title: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotesAppSpacing.space4,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.heightIn(min = NotesAppSizes.buttonMinimumHeight),
            shape = NotesAppShapes.full,
        ) {
            Text(text = retryLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksErrorScreenPreview() {
    NotesAppTheme {
        TasksErrorScreen(
            title = "Не удалось загрузить задачи",
            retryLabel = "Повторить",
            onRetry = {},
        )
    }
}
