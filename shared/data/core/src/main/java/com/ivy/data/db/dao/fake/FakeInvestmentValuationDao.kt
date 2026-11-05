package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.InvestmentValuationDao
import com.ivy.data.db.dao.write.WriteInvestmentValuationDao
import com.ivy.data.db.entity.InvestmentValuationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class FakeInvestmentValuationDao : InvestmentValuationDao, WriteInvestmentValuationDao {
    val valuations = mutableListOf<InvestmentValuationEntity>()

    override suspend fun findByAccountId(accountId: UUID): List<InvestmentValuationEntity> {
        return valuations.filter { it.accountId == accountId }.sortedByDescending { it.valuationDate }
    }

    override fun observeByAccountId(accountId: UUID): Flow<List<InvestmentValuationEntity>> {
        return flowOf(valuations.filter { it.accountId == accountId }.sortedByDescending { it.valuationDate })
    }

    override suspend fun findLatestByAccountId(accountId: UUID): InvestmentValuationEntity? {
        return valuations.filter { it.accountId == accountId }.maxByOrNull { it.valuationDate }
    }

    override suspend fun findById(id: UUID): InvestmentValuationEntity? {
        return valuations.find { it.id == id }
    }

    override suspend fun findAll(): List<InvestmentValuationEntity> {
        return valuations.sortedByDescending { it.valuationDate }
    }

    override suspend fun save(valuation: InvestmentValuationEntity) {
        valuations.removeAll { it.id == valuation.id }
        valuations.add(valuation)
    }

    override suspend fun saveAll(valuations: List<InvestmentValuationEntity>) {
        valuations.forEach { save(it) }
    }

    override suspend fun deleteById(id: UUID) {
        valuations.removeAll { it.id == id }
    }

    override suspend fun deleteByAccountId(accountId: UUID) {
        valuations.removeAll { it.accountId == accountId }
    }

    override suspend fun deleteAll() {
        valuations.clear()
    }
}

