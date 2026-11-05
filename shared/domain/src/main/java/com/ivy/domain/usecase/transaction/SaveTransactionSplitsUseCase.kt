package com.ivy.domain.usecase.transaction

import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.repository.TransactionSplitRepository
import javax.inject.Inject
import kotlin.math.abs

class SaveTransactionSplitsUseCase @Inject constructor(
    private val splitRepository: TransactionSplitRepository,
) {
    suspend operator fun invoke(
        transactionId: TransactionId,
        parentAmount: Double,
        splits: List<TransactionSplit>,
    ): Result<Unit> {
        if (splits.isEmpty()) {
            splitRepository.deleteByTransactionId(transactionId)
            return Result.success(Unit)
        }

        if (splits.any { it.amount <= 0.0 }) {
            return Result.failure(IllegalArgumentException("All splits must have an amount greater than 0"))
        }

        val totalSplit = splits.sumOf { it.amount }
        if (abs(totalSplit - parentAmount) > 0.01) {
            return Result.failure(
                IllegalArgumentException("Sum of splits ($totalSplit) must match transaction amount ($parentAmount)")
            )
        }

        splitRepository.saveSplits(transactionId, splits)
        return Result.success(Unit)
    }
}

