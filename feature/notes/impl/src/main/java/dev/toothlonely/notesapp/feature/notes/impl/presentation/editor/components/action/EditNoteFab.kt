package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.action

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun EditNoteFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_edit_24),
            contentDescription = label,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditNoteFabPreview() {
    NotesAppTheme {
        EditNoteFab(
            label = "Редактировать заметку",
            onClick = {},
        )
    }
}
