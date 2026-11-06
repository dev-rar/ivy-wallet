package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.SinkingFundEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface SinkingFundDao {
    @Query("SELECT * FROM sinking_funds WHERE budgetItemId = :budgetItemId LIMIT 1")
    suspend fun findByBudgetItemId(budgetItemId: UUID): SinkingFundEntity?

    @Query("SELECT * FROM sinking_funds WHERE budgetItemId = :budgetItemId LIMIT 1")
    fun observeByBudgetItemId(budgetItemId: UUID): Flow<SinkingFundEntity?>

    @Query("SELECT * FROM sinking_funds WHERE id = :id")
    suspend fun findById(id: UUID): SinkingFundEntity?

    @Query("SELECT * FROM sinking_funds")
    suspend fun findAll(): List<SinkingFundEntity>

    @Query("SELECT * FROM sinking_funds")
    fun observeAll(): Flow<List<SinkingFundEntity>>
}

