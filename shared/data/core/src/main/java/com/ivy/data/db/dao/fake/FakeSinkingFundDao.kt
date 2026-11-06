package com.ivy.data.db.dao.fake

import com.ivy.data.db.dao.read.SinkingFundDao
import com.ivy.data.db.dao.write.WriteSinkingFundDao
import com.ivy.data.db.entity.SinkingFundEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class FakeSinkingFundDao : SinkingFundDao, WriteSinkingFundDao {
    val funds = mutableListOf<SinkingFundEntity>()

    override suspend fun findByBudgetItemId(budgetItemId: UUID): SinkingFundEntity? {
        return funds.find { it.budgetItemId == budgetItemId }
    }

    override fun observeByBudgetItemId(budgetItemId: UUID): Flow<SinkingFundEntity?> {
        return flowOf(funds.find { it.budgetItemId == budgetItemId })
    }

    override suspend fun findById(id: UUID): SinkingFundEntity? {
        return funds.find { it.id == id }
    }

    override suspend fun findAll(): List<SinkingFundEntity> {
        return funds.toList()
    }

    override fun observeAll(): Flow<List<SinkingFundEntity>> {
        return flowOf(funds.toList())
    }

    override suspend fun save(sinkingFund: SinkingFundEntity) {
        funds.removeAll { it.id == sinkingFund.id || it.budgetItemId == sinkingFund.budgetItemId }
        funds.add(sinkingFund)
    }

    override suspend fun saveAll(sinkingFunds: List<SinkingFundEntity>) {
        sinkingFunds.forEach { save(it) }
    }

    override suspend fun deleteById(id: UUID) {
        funds.removeAll { it.id == id }
    }

    override suspend fun deleteByBudgetItemId(budgetItemId: UUID) {
        funds.removeAll { it.budgetItemId == budgetItemId }
    }

    override suspend fun deleteAll() {
        funds.clear()
    }
}

