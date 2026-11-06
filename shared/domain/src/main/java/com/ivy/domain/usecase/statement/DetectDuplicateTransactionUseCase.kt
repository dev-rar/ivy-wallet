package com.ivy.domain.usecase.statement

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.TransactionDao
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

class DetectDuplicateTransactionUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun isDuplicate(
        accountId: UUID?,
        amount: Double,
        date: Instant,
        toleranceHours: Long = 48
    ): Boolean = withContext(dispatchers.io) {
        val start = date.minus(toleranceHours, ChronoUnit.HOURS)
        val end = date.plus(toleranceHours, ChronoUnit.HOURS)

        val transactions = transactionDao.findAllBetween(start, end)
        transactions.any { trn ->
            val matchesAccount = accountId == null || trn.accountId == accountId
            val matchesAmount = abs(trn.amount - amount) < 0.01
            matchesAccount && matchesAmount
        }
    }
}

