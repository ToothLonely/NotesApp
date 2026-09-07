package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun InlineTaskEditor(
    title: String,
    placeholder: String,
    modeDescription: String,
    errorMessage: String?,
    confirmDescription: String,
    cancelDescription: String,
    savingDescription: String,
    isSaving: Boolean,
    onTitleChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { stateDescription = modeDescription },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(NotesAppSpacing.space3),
            verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = NotesAppSizes.taskEditorFieldMinimumHeight)
                    .focusRequester(focusRequester),
                enabled = !isSaving,
                placeholder = { Text(text = placeholder) },
                supportingText = errorMessage?.let { message ->
                    {
                        Text(
                            text = message,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Polite
                            },
                        )
                    }
                },
                isError = errorMessage != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (!isSaving) onConfirm() },
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier.size(NotesAppSizes.minimumTouchTarget),
                    enabled = !isSaving,
                ) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_close_24),
                        contentDescription = cancelDescription,
                    )
                }
                IconButton(
                    onClick = onConfirm,
                    modifier = Modifier.size(NotesAppSizes.minimumTouchTarget),
                    enabled = !isSaving,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(NotesAppSizes.standardIcon)
                                .semantics { stateDescription = savingDescription },
                        )
                    } else {
                        Icon(
                            painter = painterResource(DesignSystemR.drawable.ic_check_24),
                            contentDescription = confirmDescription,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineTaskEditorPreview() {
    NotesAppTheme {
        InlineTaskEditor(
            title = "Новая задача",
            placeholder = "Название задачи",
            modeDescription = "Создание задачи",
            errorMessage = null,
            confirmDescription = "Сохранить задачу",
            cancelDescription = "Отменить создание задачи",
            savingDescription = "Сохраняем задачу",
            isSaving = false,
            onTitleChange = {},
            onConfirm = {},
            onCancel = {},
        )
    }
}
