package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.SinkingFundEntity
import java.util.UUID

@Dao
interface WriteSinkingFundDao {
    @Upsert
    suspend fun save(sinkingFund: SinkingFundEntity)

    @Upsert
    suspend fun saveAll(sinkingFunds: List<SinkingFundEntity>)

    @Query("DELETE FROM sinking_funds WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM sinking_funds WHERE budgetItemId = :budgetItemId")
    suspend fun deleteByBudgetItemId(budgetItemId: UUID)

    @Query("DELETE FROM sinking_funds")
    suspend fun deleteAll()
}

