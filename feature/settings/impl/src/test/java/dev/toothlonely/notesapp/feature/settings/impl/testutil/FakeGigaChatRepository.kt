package dev.toothlonely.notesapp.feature.settings.impl.testutil

import dev.toothlonely.notesapp.core.domain.model.GigaChatBalance
import dev.toothlonely.notesapp.core.domain.repository.GigaChatRepository
import kotlinx.coroutines.CompletableDeferred

class FakeGigaChatRepository : GigaChatRepository {
    var balance = GigaChatBalance(totalTokens = 12_480)
    var balanceFailure: Throwable? = null
    var balanceGate: CompletableDeferred<Unit>? = null
    var balanceRequestCount = 0

    override suspend fun getBalance(): GigaChatBalance {
        balanceRequestCount += 1
        balanceGate?.await()
        balanceFailure?.let { throw it }
        return balance
    }

    override suspend fun formulateTask(recognizedText: String): String =
        error("Task formulation is not used by Settings")
}
