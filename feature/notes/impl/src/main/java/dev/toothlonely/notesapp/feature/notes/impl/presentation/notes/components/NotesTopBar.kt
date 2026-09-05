package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(title: String) {
    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NotesTopBarPreview() {
    NotesAppTheme {
        NotesTopBar(title = "Заметки")
    }
}
