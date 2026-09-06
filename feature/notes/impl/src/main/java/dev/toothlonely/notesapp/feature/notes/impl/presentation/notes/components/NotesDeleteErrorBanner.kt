package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.icon.NotesAppIcons
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun NotesDeleteErrorBanner(
    message: String,
    retryLabel: String,
    dismissLabel: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(
                start = NotesAppSpacing.space4,
                top = NotesAppSpacing.space2,
                end = NotesAppSpacing.space2,
                bottom = NotesAppSpacing.space2,
            ),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) {
                Text(text = retryLabel)
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = NotesAppIcons.Close,
                    contentDescription = dismissLabel,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesDeleteErrorBannerPreview() {
    NotesAppTheme {
        NotesDeleteErrorBanner(
            message = "Не удалось удалить заметку",
            retryLabel = "Повторить",
            dismissLabel = "Закрыть сообщение об ошибке",
            onRetry = {},
            onDismiss = {},
        )
    }
}
