package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.toothlonely.notesapp.core.designsystem.component.AppSnackbarHost
import dev.toothlonely.notesapp.core.domain.speech.SpeechRecognitionFailure
import dev.toothlonely.notesapp.feature.tasks.impl.R
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens.TasksScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksRoute(
    bottomNavigationPadding: PaddingValues,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context.findActivity()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCreationMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val taskDeletedMessage = stringResource(R.string.tasks_deleted)
    val voiceError = state.voiceInput as? TasksVoiceInputUiState.Error
    val voiceErrorMessage = voiceError?.let { error ->
        stringResource(error.failure.messageResource)
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
        isCreationMenuExpanded = false
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
        enabled = state.voiceInput.isBusy || (
            state.editor != null &&
                state.editor?.isSaving == false &&
                state.deleteConfirmation == null
            ),
    ) {
        if (state.voiceInput.isBusy) {
            viewModel.cancelVoiceInput()
        } else {
            viewModel.cancelTaskEditor()
        }
    }

    DisposableEffect(viewModel) {
        onDispose(viewModel::cancelVoiceInput)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (
            state.voiceInput is TasksVoiceInputUiState.PermissionDenied &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.dismissVoiceInputMessage()
        }
    }

    LaunchedEffect(viewModel, taskDeletedMessage) {
        viewModel.events.collect { event ->
            when (event) {
                TasksEvent.TaskDeleted -> launch {
                    snackbarHostState.showSnackbar(taskDeletedMessage)
                }
            }
        }
    }

    LaunchedEffect(voiceErrorMessage) {
        if (voiceErrorMessage != null) {
            snackbarHostState.showSnackbar(voiceErrorMessage)
            viewModel.dismissVoiceInputMessage()
        }
    }

    TasksScreen(
        state = state,
        bottomNavigationPadding = bottomNavigationPadding,
        isCreationMenuExpanded = isCreationMenuExpanded,
        onRequestCreationMenu = { isCreationMenuExpanded = true },
        onDismissCreationMenu = { isCreationMenuExpanded = false },
        onAddTextTask = {
            isCreationMenuExpanded = false
            viewModel.startCreatingTask()
        },
        onAddVoiceTask = requestVoiceInput,
        onStopVoiceInput = viewModel::stopVoiceInput,
        onDismissVoiceInput = viewModel::dismissVoiceInputMessage,
        onMicrophonePermissionAction = {
            val permission = state.voiceInput as? TasksVoiceInputUiState.PermissionDenied
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
        onDraftQueryChange = viewModel::updateDraftQuery,
        onSearch = viewModel::applySearch,
        onClearSearch = viewModel::clearSearch,
        onStatusFilterChange = viewModel::changeStatusFilter,
        onSortOrderChange = viewModel::changeSortOrder,
        onResetSearchAndFilter = viewModel::resetSearchAndFilter,
        onDraftTitleChange = viewModel::updateDraftTitle,
        onConfirmTask = viewModel::confirmTaskEditor,
        onCancelTask = viewModel::cancelTaskEditor,
        onEditTask = viewModel::startEditingTask,
        onRequestDeleteTask = viewModel::requestTaskDeletion,
        onConfirmDeleteTask = viewModel::confirmTaskDeletion,
        onCancelDeleteTask = viewModel::cancelTaskDeletion,
        onRetryDeleteTask = viewModel::retryTaskDeletion,
        onDismissDeleteError = viewModel::dismissTaskDeletionError,
        onToggleTaskStatus = viewModel::toggleTaskStatus,
        onRetryStatusUpdate = viewModel::retryStatusUpdate,
        onDismissStatusUpdateError = viewModel::dismissStatusUpdateError,
        onRetryLoading = viewModel::retryLoading,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    )
}

private val TasksVoiceFailure.messageResource: Int
    get() = when (this) {
        is TasksVoiceFailure.Speech -> when (failure) {
            SpeechRecognitionFailure.Unavailable -> R.string.tasks_voice_unavailable
            SpeechRecognitionFailure.NoSpeech -> R.string.tasks_voice_no_speech
            SpeechRecognitionFailure.NoMatch -> R.string.tasks_voice_no_match
            SpeechRecognitionFailure.Network -> R.string.tasks_voice_speech_network_error
            SpeechRecognitionFailure.Audio -> R.string.tasks_voice_audio_error
            SpeechRecognitionFailure.Busy -> R.string.tasks_voice_busy_error
            SpeechRecognitionFailure.Unknown -> R.string.tasks_voice_speech_error
        }
        TasksVoiceFailure.GigaChat -> R.string.tasks_voice_gigachat_error
        TasksVoiceFailure.InappropriateInput -> R.string.tasks_voice_inappropriate_input
        TasksVoiceFailure.Storage -> R.string.tasks_voice_storage_error
    }

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
