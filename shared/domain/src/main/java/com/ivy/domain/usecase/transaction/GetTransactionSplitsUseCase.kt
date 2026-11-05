package com.ivy.domain.usecase.transaction

import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.repository.TransactionSplitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionSplitsUseCase @Inject constructor(
    private val splitRepository: TransactionSplitRepository,
) {
    suspend operator fun invoke(transactionId: TransactionId): List<TransactionSplit> {
        return splitRepository.findByTransactionId(transactionId)
    }

    fun observe(transactionId: TransactionId): Flow<List<TransactionSplit>> {
        return splitRepository.observeByTransactionId(transactionId)
    }
}

