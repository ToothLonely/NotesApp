package dev.toothlonely.notesapp.feature.notes.impl.testutil

import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.CameraCaptureTarget
import dev.toothlonely.notesapp.feature.notes.impl.domain.repository.NoteImageStorage
import kotlinx.coroutines.CompletableDeferred

class FakeNoteImageStorage(
    private val operationLog: MutableList<String>? = null,
) : NoteImageStorage {
    val stagedSourceUris = mutableListOf<String>()
    val stagedDisposableFileNames = mutableListOf<String?>()
    val promotedFileNames = mutableListOf<String>()
    val discardedFileNames = mutableListOf<String>()
    val deletedFileNames = mutableListOf<String>()
    val cleanupCalls = mutableListOf<CleanupCall>()
    val storedBytes = mutableMapOf<String, ByteArray>()
    var nextStagedFileName = "00000000-0000-0000-0000-000000000001.jpg"
    var stageFailure: Throwable? = null
    var stageGate: CompletableDeferred<Unit>? = null
    var promoteFailure: Throwable? = null
    var deleteFailure: Throwable? = null

    override suspend fun createCameraCaptureTarget(): CameraCaptureTarget = CameraCaptureTarget(
        uri = "content://camera/capture",
        stagingFileName = "capture-00000000-0000-0000-0000-000000000001.jpg",
    )

    override suspend fun stageImage(
        sourceUri: String,
        disposableSourceFileName: String?,
    ): String {
        stagedSourceUris += sourceUri
        stagedDisposableFileNames += disposableSourceFileName
        stageGate?.await()
        stageFailure?.let { throw it }
        return nextStagedFileName
    }

    override suspend fun promote(stagedFileName: String) {
        promoteFailure?.let { throw it }
        operationLog?.add("image:promote:$stagedFileName")
        promotedFileNames += stagedFileName
    }

    override suspend fun discardStaged(stagedFileName: String) {
        discardedFileNames += stagedFileName
    }

    override suspend fun read(fileName: String, staged: Boolean): ByteArray =
        storedBytes.getValue(fileName)

    override suspend fun delete(fileName: String) {
        deleteFailure?.let { throw it }
        operationLog?.add("image:delete:$fileName")
        deletedFileNames += fileName
        storedBytes.remove(fileName)
    }

    override suspend fun cleanup(
        referencedFileNames: Set<String>,
        staleStagingCutoffMillis: Long,
    ) {
        cleanupCalls += CleanupCall(referencedFileNames, staleStagingCutoffMillis)
    }

    data class CleanupCall(
        val referencedFileNames: Set<String>,
        val staleStagingCutoffMillis: Long,
    )
}
