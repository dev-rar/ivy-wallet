package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.TransactionSplitEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TransactionSplitDao {
    @Query("SELECT * FROM transaction_splits WHERE transactionId = :transactionId ORDER BY orderNum ASC")
    suspend fun findByTransactionId(transactionId: UUID): List<TransactionSplitEntity>

    @Query("SELECT * FROM transaction_splits WHERE transactionId = :transactionId ORDER BY orderNum ASC")
    fun observeByTransactionId(transactionId: UUID): Flow<List<TransactionSplitEntity>>

    @Query("SELECT * FROM transaction_splits WHERE budgetItemId = :budgetItemId ORDER BY orderNum ASC")
    suspend fun findByBudgetItemId(budgetItemId: UUID): List<TransactionSplitEntity>

    @Query("SELECT * FROM transaction_splits")
    suspend fun findAll(): List<TransactionSplitEntity>

    @Query("SELECT * FROM transaction_splits WHERE transactionId IN (:transactionIds)")
    suspend fun findByTransactionIds(transactionIds: List<UUID>): List<TransactionSplitEntity>
}

