package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun InlineNoteTitleField(
    title: String,
    placeholder: String,
    label: String,
    enabled: Boolean,
    onTitleChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = title,
        onValueChange = onTitleChanged,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = label },
        enabled = enabled,
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (title.isBlank()) {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun InlineNoteTitleFieldPreview() {
    NotesAppTheme {
        InlineNoteTitleField(
            title = "",
            placeholder = "Заголовок вашей заметки",
            label = "Заголовок заметки",
            enabled = true,
            onTitleChanged = {},
        )
    }
}
