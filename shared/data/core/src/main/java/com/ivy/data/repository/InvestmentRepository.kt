package com.ivy.data.repository

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.InvestmentValuationDao
import com.ivy.data.db.dao.write.WriteInvestmentValuationDao
import com.ivy.data.db.entity.InvestmentValuationEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.InvestmentValuation
import com.ivy.data.model.InvestmentValuationId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepository @Inject constructor(
    private val valuationDao: InvestmentValuationDao,
    private val writeValuationDao: WriteInvestmentValuationDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun getValuations(accountId: AccountId): List<InvestmentValuation> =
        withContext(dispatchersProvider.io) {
            valuationDao.findByAccountId(accountId.value).map { it.toDomain() }
        }

    fun observeValuations(accountId: AccountId): Flow<List<InvestmentValuation>> =
        valuationDao.observeByAccountId(accountId.value).map { list -> list.map { it.toDomain() } }

    suspend fun getLatestValuation(accountId: AccountId): InvestmentValuation? =
        withContext(dispatchersProvider.io) {
            valuationDao.findLatestByAccountId(accountId.value)?.toDomain()
        }

    suspend fun recordValuation(
        accountId: AccountId,
        amount: Double,
        date: Instant = Instant.now(),
        notes: String? = null,
    ): InvestmentValuation = withContext(dispatchersProvider.io) {
        val entity = InvestmentValuationEntity(
            id = UUID.randomUUID(),
            accountId = accountId.value,
            valuationDate = date,
            valuationAmount = amount,
            notes = notes,
        )
        writeValuationDao.save(entity)
        entity.toDomain()
    }

    suspend fun deleteValuation(id: UUID) = withContext(dispatchersProvider.io) {
        writeValuationDao.deleteById(id)
    }

    suspend fun deleteAllForAccount(accountId: AccountId) = withContext(dispatchersProvider.io) {
        writeValuationDao.deleteByAccountId(accountId.value)
    }

    private fun InvestmentValuationEntity.toDomain() = InvestmentValuation(
        id = InvestmentValuationId(id),
        accountId = AccountId(accountId),
        valuationDate = valuationDate,
        valuationAmount = valuationAmount,
        notes = notes,
    )
}

