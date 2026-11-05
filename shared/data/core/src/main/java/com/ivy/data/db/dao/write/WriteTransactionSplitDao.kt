package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.TransactionSplitEntity
import java.util.UUID

@Dao
interface WriteTransactionSplitDao {
    @Upsert
    suspend fun save(split: TransactionSplitEntity)

    @Upsert
    suspend fun saveAll(splits: List<TransactionSplitEntity>)

    @Query("DELETE FROM transaction_splits WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM transaction_splits WHERE transactionId = :transactionId")
    suspend fun deleteByTransactionId(transactionId: UUID)

    @Query("DELETE FROM transaction_splits")
    suspend fun deleteAll()
}

