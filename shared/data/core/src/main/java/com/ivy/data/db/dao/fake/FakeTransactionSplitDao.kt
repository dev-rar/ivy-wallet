package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.TransactionSplitDao
import com.ivy.data.db.dao.write.WriteTransactionSplitDao
import com.ivy.data.db.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class FakeTransactionSplitDao : TransactionSplitDao, WriteTransactionSplitDao {
    val splits = mutableListOf<TransactionSplitEntity>()

    override suspend fun findByTransactionId(transactionId: UUID): List<TransactionSplitEntity> {
        return splits.filter { it.transactionId == transactionId }.sortedBy { it.orderNum }
    }

    override fun observeByTransactionId(transactionId: UUID): Flow<List<TransactionSplitEntity>> {
        return flowOf(splits.filter { it.transactionId == transactionId }.sortedBy { it.orderNum })
    }

    override suspend fun findByBudgetItemId(budgetItemId: UUID): List<TransactionSplitEntity> {
        return splits.filter { it.budgetItemId == budgetItemId }.sortedBy { it.orderNum }
    }

    override suspend fun findAll(): List<TransactionSplitEntity> {
        return splits.toList()
    }

    override suspend fun findByTransactionIds(transactionIds: List<UUID>): List<TransactionSplitEntity> {
        return splits.filter { it.transactionId in transactionIds }
    }

    override suspend fun save(split: TransactionSplitEntity) {
        splits.removeAll { it.id == split.id }
        splits.add(split)
    }

    override suspend fun saveAll(splits: List<TransactionSplitEntity>) {
        splits.forEach { save(it) }
    }

    override suspend fun deleteById(id: UUID) {
        splits.removeAll { it.id == id }
    }

    override suspend fun deleteByTransactionId(transactionId: UUID) {
        splits.removeAll { it.transactionId == transactionId }
    }

    override suspend fun deleteAll() {
        splits.clear()
    }
}

