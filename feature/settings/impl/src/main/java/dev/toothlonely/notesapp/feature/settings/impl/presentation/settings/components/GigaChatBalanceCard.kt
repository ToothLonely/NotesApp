package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.settings.impl.R
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.GigaChatBalanceUiState
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.formatGigaChatTokenCount

@Composable
fun GigaChatBalanceCard(
    state: GigaChatBalanceUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val formattedTokenCount = if (state is GigaChatBalanceUiState.Content) {
        formatGigaChatTokenCount(state.tokenCount)
    } else {
        ""
    }
    val balanceContentDescription = when (state) {
        GigaChatBalanceUiState.Loading -> stringResource(R.string.settings_balance_loading)
        is GigaChatBalanceUiState.Content -> stringResource(
            R.string.settings_balance_content_description,
            formattedTokenCount,
        )
        GigaChatBalanceUiState.Error -> stringResource(R.string.settings_balance_error)
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = balanceContentDescription
            },
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
                GigaChatBalanceUiState.Loading -> Row(
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        NotesAppSpacing.space2,
                    ),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(NotesAppSizes.standardIcon),
                        strokeWidth = NotesAppSizes.selectedOutlineWidth,
                    )
                    Text(
                        text = stringResource(R.string.settings_balance_loading),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is GigaChatBalanceUiState.Content -> Text(
                    text = stringResource(R.string.settings_balance_value, formattedTokenCount),
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                    style = MaterialTheme.typography.headlineMedium,
                )
                GigaChatBalanceUiState.Error -> Column(
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                ) {
                    Text(
                        text = stringResource(R.string.settings_balance_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(onClick = onRetry) {
                        Text(text = stringResource(R.string.settings_balance_retry))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GigaChatBalanceCardPreview() {
    NotesAppTheme {
        GigaChatBalanceCard(
            state = GigaChatBalanceUiState.Content(tokenCount = 12_480),
            onRetry = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}
