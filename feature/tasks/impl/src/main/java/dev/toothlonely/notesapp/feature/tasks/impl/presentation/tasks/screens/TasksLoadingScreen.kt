package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun TasksLoadingScreen(
    label: String,
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
        CircularProgressIndicator()
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksLoadingScreenPreview() {
    NotesAppTheme {
        TasksLoadingScreen(label = "Загружаем задачи…")
    }
}
