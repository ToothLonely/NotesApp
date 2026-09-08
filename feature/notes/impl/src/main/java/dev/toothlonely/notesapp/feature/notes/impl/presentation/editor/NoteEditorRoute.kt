package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import dev.toothlonely.notesapp.feature.notes.impl.navigation.noteContainerTransition
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.core.designsystem.component.AppSnackbarHost
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorage
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.attachment.ImageAttachmentActionsSheet
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.attachment.ImageSourceSheet
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.CameraPermissionUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.EditorImage
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorArgs
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.model.NoteEditorMode
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens.NoteEditorScreen
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.NoteImageLoader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteEditorRoute(
    noteId: Long?,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel(
        parameters = { parametersOf(NoteEditorArgs(noteId)) },
    ),
    imageStorage: NoteImageStorage = koinInject(),
    imageLoader: NoteImageLoader = koinInject(),
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content = state.value as? NoteEditorUiState.Content
    val snackbarHostState = remember { SnackbarHostState() }
    val shareChooserTitle = stringResource(R.string.note_share_chooser_title)
    val voiceError = content?.voiceInput as? NoteVoiceInputUiState.Error
    val voiceErrorMessage = voiceError?.let { error ->
        stringResource(error.failure.messageResource)
    }
    var attachmentSheet by rememberSaveable { mutableStateOf(AttachmentSheet.Hidden) }
    var cameraPermissionState by rememberSaveable {
        mutableStateOf(CameraPermissionUiState.Hidden)
    }
    var captureUri by rememberSaveable { mutableStateOf<String?>(null) }
    var captureStagingFileName by rememberSaveable { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        uri?.let { viewModel.attachImage(it.toString()) }
    }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            captured ->
        val uri = captureUri
        val stagingFileName = captureStagingFileName
        captureUri = null
        captureStagingFileName = null
        if (captured && uri != null && stagingFileName != null) {
            viewModel.attachImage(
                sourceUri = uri,
                disposableSourceFileName = stagingFileName,
            )
        } else if (stagingFileName != null) {
            viewModel.discardCameraCapture(stagingFileName)
        }
    }
    val startCameraCapture: () -> Unit = {
        coroutineScope.launch {
            try {
                val target = imageStorage.createCameraCaptureTarget()
                captureUri = target.uri
                captureStagingFileName = target.stagingFileName
                runCatching { takePicture.launch(Uri.parse(target.uri)) }
                    .onFailure {
                        captureUri = null
                        captureStagingFileName = null
                        viewModel.discardCameraCapture(target.stagingFileName)
                        viewModel.onCameraPreparationFailed()
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                viewModel.onCameraPreparationFailed()
            }
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            cameraPermissionState = CameraPermissionUiState.Hidden
            startCameraCapture()
        } else {
            cameraPermissionState = if (
                activity != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA,
                )
            ) {
                CameraPermissionUiState.Denied
            } else {
                CameraPermissionUiState.PermanentlyDenied
            }
        }
    }
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.dismissVoiceInputMessage()
            viewModel.startVoiceInput()
        } else {
            val canRequestAgain = activity != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.RECORD_AUDIO,
                )
            viewModel.onMicrophonePermissionDenied(canRequestAgain)
        }
    }
    val requestVoiceInput: () -> Unit = {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        val shouldShowRationale = activity?.let { currentActivity ->
            ActivityCompat.shouldShowRequestPermissionRationale(
                currentActivity,
                Manifest.permission.RECORD_AUDIO,
            )
        } == true
        when {
            permissionGranted -> viewModel.startVoiceInput()
            shouldShowRationale -> viewModel.onMicrophonePermissionDenied(
                canRequestAgain = true,
            )
            else -> microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler(
        enabled = content?.mode == NoteEditorMode.Editing ||
            content?.mode == NoteEditorMode.Creating ||
            content?.isSaving == true,
    ) {
        viewModel.onBack()
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::cancelVoiceInput)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NoteEditorEvent.CloseEditor,
                NoteEditorEvent.SaveSucceeded,
                    -> onBack()
            }
        }
    }

    LaunchedEffect(voiceErrorMessage) {
        if (voiceErrorMessage != null) {
            snackbarHostState.showSnackbar(voiceErrorMessage)
            viewModel.dismissVoiceInputMessage()
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionState = CameraPermissionUiState.Hidden
        }
        if (
            content?.voiceInput is NoteVoiceInputUiState.PermissionDenied &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.dismissVoiceInputMessage()
        }
    }

    NoteEditorScreen(
        modifier = Modifier.noteContainerTransition(noteId, isEditor = true),
        state = state.value,
        onTitleChanged = viewModel::onTitleChanged,
        onBodyChanged = viewModel::onBodyChanged,
        onSave = viewModel::save,
        onEdit = viewModel::startEditing,
        onShare = {
            content?.let { note ->
                context.shareNote(
                    title = note.title,
                    body = note.body,
                    chooserTitle = shareChooserTitle,
                )
            }
        },
        onRetryPreparation = viewModel::retryPreparation,
        cameraPermissionState = cameraPermissionState,
        loadImage = imageLoader::load,
        onImageLoadError = viewModel::onImageLoadFailed,
        onAttachmentClick = { attachmentSheet = AttachmentSheet.Source },
        onAttachmentThumbnailClick = { attachmentSheet = AttachmentSheet.Actions },
        onDismissAttachmentError = viewModel::dismissAttachmentError,
        onCameraPermissionAction = {
            when (cameraPermissionState) {
                CameraPermissionUiState.Hidden -> Unit
                CameraPermissionUiState.Denied -> {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                CameraPermissionUiState.PermanentlyDenied -> context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        },
        onDismissCameraPermission = {
            cameraPermissionState = CameraPermissionUiState.Hidden
        },
        onStartVoiceInput = requestVoiceInput,
        onStopVoiceInput = viewModel::stopVoiceInput,
        onDismissVoiceInput = viewModel::dismissVoiceInputMessage,
        onMicrophonePermissionAction = {
            val permission = content?.voiceInput as? NoteVoiceInputUiState.PermissionDenied
            if (permission?.canRequestAgain == true) {
                microphonePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${context.packageName}"),
                    ),
                )
            }
        },
        onBack = viewModel::onBack,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    )

    if (attachmentSheet == AttachmentSheet.Source) {
        ImageSourceSheet(
            title = stringResource(R.string.note_image_source_title),
            chooseFileLabel = stringResource(R.string.note_choose_image),
            takePhotoLabel = stringResource(R.string.note_take_photo),
            onChooseFile = {
                attachmentSheet = AttachmentSheet.Hidden
                photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            onTakePhoto = {
                attachmentSheet = AttachmentSheet.Hidden
                val permissionGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                val shouldShowRationale = activity?.let { currentActivity ->
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        currentActivity,
                        Manifest.permission.CAMERA,
                    )
                } == true
                if (permissionGranted) {
                    cameraPermissionState = CameraPermissionUiState.Hidden
                    startCameraCapture()
                } else if (shouldShowRationale) {
                    cameraPermissionState = CameraPermissionUiState.Denied
                } else {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            onDismiss = { attachmentSheet = AttachmentSheet.Hidden },
        )
    }

    if (attachmentSheet == AttachmentSheet.Actions && content?.image != null) {
        ImageAttachmentActionsSheet(
            fileName = content.image.fileName,
            staged = content.image is EditorImage.Staged,
            previewDescription = stringResource(R.string.note_attached_image),
            replaceLabel = stringResource(R.string.note_replace_image),
            deleteLabel = stringResource(R.string.note_delete_image),
            loadImage = imageLoader::load,
            onImageLoadError = {
                viewModel.onImageLoadFailed(
                    fileName = content.image.fileName,
                    staged = content.image is EditorImage.Staged,
                )
            },
            onReplace = {
                if (content.mode == NoteEditorMode.Reading) viewModel.startEditing()
                attachmentSheet = AttachmentSheet.Source
            },
            onDelete = {
                if (content.mode == NoteEditorMode.Reading) viewModel.startEditing()
                viewModel.removeImage()
                attachmentSheet = AttachmentSheet.Hidden
            },
            onDismiss = { attachmentSheet = AttachmentSheet.Hidden },
        )
    }
}

private val SpeechRecognitionFailure.messageResource: Int
    get() = when (this) {
        SpeechRecognitionFailure.Unavailable -> R.string.note_voice_unavailable
        SpeechRecognitionFailure.NoSpeech -> R.string.note_voice_no_speech
        SpeechRecognitionFailure.NoMatch -> R.string.note_voice_no_match
        SpeechRecognitionFailure.Network -> R.string.note_voice_network_error
        SpeechRecognitionFailure.Audio -> R.string.note_voice_audio_error
        SpeechRecognitionFailure.Busy -> R.string.note_voice_busy_error
        SpeechRecognitionFailure.Unknown -> R.string.note_voice_error
    }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
