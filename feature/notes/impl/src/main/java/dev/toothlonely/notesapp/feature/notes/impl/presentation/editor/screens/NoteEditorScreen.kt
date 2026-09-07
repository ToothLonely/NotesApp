package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.CameraPermissionUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorAttachmentError
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorMode
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorSaveError
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.feedback.CameraPermissionDialog
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.action.EditNoteFab
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.topbar.NoteEditorTopBar
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.topbar.NoteReadingTopBar

@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onEdit: () -> Unit,
    onRetryPreparation: () -> Unit,
    cameraPermissionState: CameraPermissionUiState,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    onImageLoadError: (String, Boolean) -> Unit,
    onAttachmentClick: () -> Unit,
    onAttachmentThumbnailClick: () -> Unit,
    onDismissAttachmentError: () -> Unit,
    onCameraPermissionAction: () -> Unit,
    onDismissCameraPermission: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state as? NoteEditorUiState.Content
    val isReading = content?.mode == NoteEditorMode.Reading
    val titlePlaceholder =
        if (content == null || isReading) ""
        else stringResource(R.string.note_title_placeholder)
    val attachmentErrorMessage = when (content?.attachmentError) {
        NoteEditorAttachmentError.SelectedImageUnavailable ->
            stringResource(R.string.note_image_source_error)
        NoteEditorAttachmentError.StoredImageUnavailable ->
            stringResource(R.string.note_image_read_error)
        NoteEditorAttachmentError.InputTooLarge ->
            stringResource(R.string.note_image_too_large_error)
        NoteEditorAttachmentError.UnsupportedImage ->
            stringResource(R.string.note_image_unsupported_error)
        NoteEditorAttachmentError.WriteFailed ->
            stringResource(R.string.note_image_write_error)
        null -> null
    }
    val saveErrorMessage = when (content?.saveError) {
        NoteEditorSaveError.Note -> stringResource(R.string.note_save_error)
        NoteEditorSaveError.Image -> stringResource(R.string.note_image_save_error)
        null -> stringResource(R.string.note_save_error)
    }
    val cameraPermissionMessage = when (cameraPermissionState) {
        CameraPermissionUiState.Hidden -> null
        CameraPermissionUiState.Denied ->
            stringResource(R.string.note_camera_permission_denied)
        CameraPermissionUiState.PermanentlyDenied ->
            stringResource(R.string.note_camera_permission_permanently_denied)
    }
    val cameraPermissionActionLabel = when (cameraPermissionState) {
        CameraPermissionUiState.Hidden -> null
        CameraPermissionUiState.Denied -> stringResource(R.string.note_camera_permission_retry)
        CameraPermissionUiState.PermanentlyDenied ->
            stringResource(R.string.note_camera_permission_settings)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (isReading) NoteReadingTopBar(
                title = content.title,
                backLabel = stringResource(R.string.note_editor_back),
                attachmentThumbnailDescription =
                    stringResource(R.string.note_attachment_thumbnail_description),
                imageFileName = content.image?.fileName,
                loadImage = loadImage,
                onImageLoadError = {
                    content.image?.let { image ->
                        onImageLoadError(image.fileName, image is EditorImage.Staged)
                    }
                },
                onAttachmentThumbnailClick = onAttachmentThumbnailClick,
                onBack = onBack,
            )
            else NoteEditorTopBar(
                title = content?.title.orEmpty(),
                placeholder = titlePlaceholder,
                titleLabel = stringResource(R.string.note_title_label),
                backLabel = stringResource(R.string.note_editor_back),
                attachmentLabel = stringResource(
                    if (content?.image == null) {
                        R.string.note_add_image
                    } else {
                        R.string.note_replace_image
                    },
                ),
                attachmentThumbnailDescription =
                    stringResource(R.string.note_attachment_thumbnail_description),
                imageFileName = content?.image?.fileName,
                isImageStaged = content?.image is EditorImage.Staged,
                enabled = content?.isSaving == false && content.isClosing.not(),
                attachmentActionsEnabled = content?.isSaving == false &&
                    content.isProcessingImage.not() &&
                    content.isClosing.not(),
                loadImage = loadImage,
                onImageLoadError = {
                    content?.image?.let { image ->
                        onImageLoadError(image.fileName, image is EditorImage.Staged)
                    }
                },
                onTitleChanged = onTitleChanged,
                onAttachmentClick = onAttachmentClick,
                onAttachmentThumbnailClick = onAttachmentThumbnailClick,
                onBack = onBack,
            )
        },
        floatingActionButton = {
            if (isReading) EditNoteFab(
                label = stringResource(R.string.note_edit),
                onClick = onEdit,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state) {
                is NoteEditorUiState.Loading -> NoteEditorLoadingScreen(
                    label = stringResource(
                        if (state.isExistingNote) R.string.note_editor_loading_existing
                        else R.string.note_editor_loading_new
                    ),
                    modifier = Modifier.fillMaxSize(),
                )

                NoteEditorUiState.NotFound -> NoteEditorErrorScreen(
                    message = stringResource(R.string.note_editor_not_found),
                    retryLabel = stringResource(R.string.note_editor_back),
                    onRetry = onBack,
                    modifier = Modifier.fillMaxSize(),
                )

                NoteEditorUiState.Error -> NoteEditorErrorScreen(
                    message = stringResource(R.string.note_editor_error),
                    retryLabel = stringResource(R.string.retry),
                    onRetry = onRetryPreparation,
                    modifier = Modifier.fillMaxSize(),
                )

                is NoteEditorUiState.Content -> when (state.mode) {
                    NoteEditorMode.Reading -> NoteEditorReadingScreen(
                        body = state.body,
                        attachmentErrorMessage = attachmentErrorMessage,
                        attachmentErrorDismissLabel =
                            stringResource(R.string.note_image_error_dismiss),
                        onDismissAttachmentError = onDismissAttachmentError,
                        modifier = Modifier
                            .widthIn(max = NotesAppSizes.maximumContentWidth)
                            .fillMaxSize()
                            .navigationBarsPadding(),
                    )

                    NoteEditorMode.Creating,
                    NoteEditorMode.Editing,
                        -> NoteEditorContentScreen(
                        body = state.body,
                        bodyLabel = stringResource(R.string.note_body_label),
                        saveLabel = stringResource(R.string.note_save),
                        savingLabel = stringResource(R.string.note_saving),
                        saveErrorMessage = saveErrorMessage,
                        saveRetryLabel = stringResource(R.string.retry),
                        attachmentErrorMessage = attachmentErrorMessage,
                        attachmentErrorDismissLabel =
                            stringResource(R.string.note_image_error_dismiss),
                        imageProcessingLabel =
                            stringResource(
                                if (state.isClosing) R.string.note_editor_closing
                                else R.string.note_image_processing,
                            ),
                        isSaving = state.isSaving,
                        isClosing = state.isClosing,
                        hasSaveError = state.saveError != null,
                        isProcessingImage = state.isProcessingImage,
                        isSaveEnabled = state.isSaveEnabled,
                        onBodyChanged = onBodyChanged,
                        onSave = onSave,
                        onDismissAttachmentError = onDismissAttachmentError,
                        modifier = Modifier
                            .widthIn(max = NotesAppSizes.maximumContentWidth)
                            .fillMaxSize()
                            .padding(horizontal = NotesAppSpacing.space4)
                            .navigationBarsPadding()
                            .imePadding(),
                    )
                }
            }
        }
    }

    if (cameraPermissionMessage != null && cameraPermissionActionLabel != null) {
        CameraPermissionDialog(
            title = stringResource(R.string.note_camera_permission_dialog_title),
            message = cameraPermissionMessage,
            actionLabel = cameraPermissionActionLabel,
            dismissLabel = stringResource(R.string.note_camera_permission_cancel),
            onAction = onCameraPermissionAction,
            onDismiss = onDismissCameraPermission,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorScreenPreview() {
    NotesAppTheme {
        NoteEditorScreen(
            state = NoteEditorUiState.Content(
                mode = NoteEditorMode.Reading,
                title = "Идеи для путешествия",
                body = "Посмотреть старый город утром, затем пройти вдоль набережной.",
                generatedTitleNumber = 1,
            ),
            onTitleChanged = {},
            onBodyChanged = {},
            onSave = {},
            onEdit = {},
            onRetryPreparation = {},
            cameraPermissionState = CameraPermissionUiState.Hidden,
            loadImage = { _, _, _, _ -> null },
            onImageLoadError = { _, _ -> },
            onAttachmentClick = {},
            onAttachmentThumbnailClick = {},
            onDismissAttachmentError = {},
            onCameraPermissionAction = {},
            onDismissCameraPermission = {},
            onBack = {},
        )
    }
}
