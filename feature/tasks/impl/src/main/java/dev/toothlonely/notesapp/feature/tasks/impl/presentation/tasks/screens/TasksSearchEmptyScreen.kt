package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun TasksSearchEmptyScreen(
    title: String,
    resetLabel: String,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            NotesAppSpacing.space2,
            Alignment.CenterVertically,
        ),
    ) {
        Text(text = title)
        TextButton(onClick = onReset) {
            Text(text = resetLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksSearchEmptyScreenPreview() {
    NotesAppTheme {
        TasksSearchEmptyScreen(
            title = "Нет задач по выбранным условиям",
            resetLabel = "Сбросить фильтры",
            onReset = {},
        )
    }
}
