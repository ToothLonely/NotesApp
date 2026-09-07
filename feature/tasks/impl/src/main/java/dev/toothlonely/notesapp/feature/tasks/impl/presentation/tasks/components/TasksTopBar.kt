package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    title: String,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.semantics { heading() },
            )
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun TasksTopBarPreview() {
    NotesAppTheme {
        TasksTopBar(title = "Задачи")
    }
}
