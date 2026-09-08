package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun TaskCreationMenu(
    expanded: Boolean,
    textLabel: String,
    voiceLabel: String,
    onTextSelected: () -> Unit,
    onVoiceSelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(text = textLabel) },
            onClick = onTextSelected,
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_edit_24),
                    contentDescription = null,
                )
            },
        )
        DropdownMenuItem(
            text = { Text(text = voiceLabel) },
            onClick = onVoiceSelected,
            leadingIcon = {
                Icon(
                    painter = painterResource(DesignSystemR.drawable.ic_mic_24),
                    contentDescription = null,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskCreationMenuPreview() {
    NotesAppTheme {
        TaskCreationMenu(
            expanded = true,
            textLabel = "Ввести текст",
            voiceLabel = "Продиктовать",
            onTextSelected = {},
            onVoiceSelected = {},
            onDismiss = {},
        )
    }
}
