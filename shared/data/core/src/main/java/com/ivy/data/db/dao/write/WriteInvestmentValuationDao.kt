package com.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ivy.data.db.entity.InvestmentValuationEntity
import java.util.UUID

@Dao
interface WriteInvestmentValuationDao {
    @Upsert
    suspend fun save(valuation: InvestmentValuationEntity)

    @Upsert
    suspend fun saveAll(valuations: List<InvestmentValuationEntity>)

    @Query("DELETE FROM investment_valuations WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM investment_valuations WHERE accountId = :accountId")
    suspend fun deleteByAccountId(accountId: UUID)

    @Query("DELETE FROM investment_valuations")
    suspend fun deleteAll()
}

