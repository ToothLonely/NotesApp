package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.settings.impl.R
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.GigaChatBalanceUiState

@Composable
fun GigaChatBalanceCard(
    state: GigaChatBalanceUiState,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(modifier = Modifier.padding(NotesAppSpacing.space6)) {
            Text(
                text = stringResource(R.string.settings_balance_title),
                style = MaterialTheme.typography.titleMedium,
            )
            when (state) {
                GigaChatBalanceUiState.Unavailable -> Text(
                    text = stringResource(R.string.settings_balance_unavailable),
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GigaChatBalanceCardPreview() {
    NotesAppTheme {
        GigaChatBalanceCard(
            state = GigaChatBalanceUiState.Unavailable,
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}
