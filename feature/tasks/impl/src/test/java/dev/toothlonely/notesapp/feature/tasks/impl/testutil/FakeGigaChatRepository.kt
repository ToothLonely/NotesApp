package dev.toothlonely.notesapp.feature.tasks.impl.testutil

import dev.toothlonely.notesapp.core.domain.model.GigaChatBalance
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository
import kotlinx.coroutines.CompletableDeferred

class FakeGigaChatRepository : GigaChatRepository {
    val formulationRequests = mutableListOf<String>()
    var formulatedTask = "Сформулированная задача"
    var formulationFailure: Throwable? = null
    var formulationGate: CompletableDeferred<Unit>? = null

    override suspend fun getBalance(): GigaChatBalance = GigaChatBalance(totalTokens = 0)

    override suspend fun formulateTask(recognizedText: String): String {
        formulationRequests += recognizedText
        formulationGate?.await()
        formulationFailure?.let { error -> throw error }
        return formulatedTask
    }
}
