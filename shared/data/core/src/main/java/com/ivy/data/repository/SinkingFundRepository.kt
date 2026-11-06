package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.SinkingFundDao
import com.ivy.data.db.dao.write.WriteSinkingFundDao
import com.ivy.data.db.entity.SinkingFundEntity
import com.ivy.data.model.budget.SinkingFund
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SinkingFundRepository @Inject constructor(
    private val sinkingFundDao: SinkingFundDao,
    private val writeSinkingFundDao: WriteSinkingFundDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun getSinkingFund(budgetItemId: UUID): SinkingFund? =
        withContext(dispatchersProvider.io) {
            sinkingFundDao.findByBudgetItemId(budgetItemId)?.toDomain()
        }

    fun observeSinkingFund(budgetItemId: UUID): Flow<SinkingFund?> =
        sinkingFundDao.observeByBudgetItemId(budgetItemId).map { it?.toDomain() }

    suspend fun getAllSinkingFunds(): List<SinkingFund> =
        withContext(dispatchersProvider.io) {
            sinkingFundDao.findAll().map { it.toDomain() }
        }

    fun observeAllSinkingFunds(): Flow<List<SinkingFund>> =
        sinkingFundDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveSinkingFund(sinkingFund: SinkingFund): Unit =
        withContext(dispatchersProvider.io) {
            writeSinkingFundDao.save(sinkingFund.toEntity())
        }

    suspend fun deleteSinkingFund(id: UUID): Unit =
        withContext(dispatchersProvider.io) {
            writeSinkingFundDao.deleteById(id)
        }

    suspend fun deleteByBudgetItemId(budgetItemId: UUID): Unit =
        withContext(dispatchersProvider.io) {
            writeSinkingFundDao.deleteByBudgetItemId(budgetItemId)
        }

    private fun SinkingFundEntity.toDomain() = SinkingFund(
        id = id,
        budgetItemId = budgetItemId,
        targetAmount = targetAmount,
        targetDate = targetDate,
        openingBalance = openingBalance,
        notes = notes
    )

    private fun SinkingFund.toEntity() = SinkingFundEntity(
        id = id,
        budgetItemId = budgetItemId,
        targetAmount = targetAmount,
        targetDate = targetDate,
        openingBalance = openingBalance,
        notes = notes
    )
}

