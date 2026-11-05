package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.TransactionSplitDao
import com.ivy.data.db.dao.write.WriteTransactionSplitDao
import com.ivy.data.db.entity.TransactionSplitEntity
import com.ivy.data.model.CategoryId
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.model.TransactionSplitId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionSplitRepository @Inject constructor(
    private val splitDao: TransactionSplitDao,
    private val writeSplitDao: WriteTransactionSplitDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun findByTransactionId(transactionId: TransactionId): List<TransactionSplit> =
        withContext(dispatchersProvider.io) {
            splitDao.findByTransactionId(transactionId.value).map { it.toDomain() }
        }

    fun observeByTransactionId(transactionId: TransactionId): Flow<List<TransactionSplit>> =
        splitDao.observeByTransactionId(transactionId.value).map { list -> list.map { it.toDomain() } }

    suspend fun findByTransactionIds(transactionIds: Collection<UUID>): List<TransactionSplit> =
        withContext(dispatchersProvider.io) {
            if (transactionIds.isEmpty()) emptyList()
            else splitDao.findByTransactionIds(transactionIds.toList()).map { it.toDomain() }
        }

    suspend fun findByBudgetItemId(budgetItemId: UUID): List<TransactionSplit> =
        withContext(dispatchersProvider.io) {
            splitDao.findByBudgetItemId(budgetItemId).map { it.toDomain() }
        }

    suspend fun saveSplits(transactionId: TransactionId, splits: List<TransactionSplit>): Unit =
        withContext(dispatchersProvider.io) {
            writeSplitDao.deleteByTransactionId(transactionId.value)
            if (splits.isNotEmpty()) {
                val entities = splits.mapIndexed { index, split ->
                    TransactionSplitEntity(
                        id = split.id.value,
                        transactionId = transactionId.value,
                        budgetItemId = split.budgetItemId,
                        categoryId = split.categoryId?.value,
                        amount = split.amount,
                        note = split.note,
                        orderNum = if (split.orderNum != 0.0) split.orderNum else index.toDouble(),
                    )
                }
                writeSplitDao.saveAll(entities)
            }
        }

    suspend fun deleteByTransactionId(transactionId: TransactionId): Unit =
        withContext(dispatchersProvider.io) {
            writeSplitDao.deleteByTransactionId(transactionId.value)
        }

    private fun TransactionSplitEntity.toDomain() = TransactionSplit(
        id = TransactionSplitId(id),
        transactionId = TransactionId(transactionId),
        budgetItemId = budgetItemId,
        categoryId = categoryId?.let { CategoryId(it) },
        amount = amount,
        note = note,
        orderNum = orderNum,
    )
}
