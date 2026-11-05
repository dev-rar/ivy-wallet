package com.ivy.data.db.dao.read

import androidx.room.Dao
import androidx.room.Query
import com.ivy.data.db.entity.InvestmentValuationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface InvestmentValuationDao {
    @Query("SELECT * FROM investment_valuations WHERE accountId = :accountId ORDER BY valuationDate DESC")
    suspend fun findByAccountId(accountId: UUID): List<InvestmentValuationEntity>

    @Query("SELECT * FROM investment_valuations WHERE accountId = :accountId ORDER BY valuationDate DESC")
    fun observeByAccountId(accountId: UUID): Flow<List<InvestmentValuationEntity>>

    @Query("SELECT * FROM investment_valuations WHERE accountId = :accountId ORDER BY valuationDate DESC LIMIT 1")
    suspend fun findLatestByAccountId(accountId: UUID): InvestmentValuationEntity?

    @Query("SELECT * FROM investment_valuations WHERE id = :id")
    suspend fun findById(id: UUID): InvestmentValuationEntity?

    @Query("SELECT * FROM investment_valuations ORDER BY valuationDate DESC")
    suspend fun findAll(): List<InvestmentValuationEntity>
}

