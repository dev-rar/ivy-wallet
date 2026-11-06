package com.ivy.domain.usecase.statement

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.write.WriteTransactionDao
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.CategoryId
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.model.TransactionSplitId
import com.ivy.data.repository.TransactionSplitRepository
import com.ivy.domain.model.statement.StatementTransaction
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class ImportStatementTransactionsUseCase @Inject constructor(
    private val writeTransactionDao: WriteTransactionDao,
    private val splitRepository: TransactionSplitRepository,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun import(
        transactions: List<StatementTransaction>,
        accountId: UUID,
        skipDuplicates: Boolean = true
    ): Int = withContext(dispatchers.io) {
        val toImport = transactions.filter { !skipDuplicates || !it.isDuplicate }
        if (toImport.isEmpty()) return@withContext 0

        for (trn in toImport) {
            val trnId = trn.id
            val entity = TransactionEntity(
                id = trnId,
                accountId = accountId,
                type = trn.type,
                amount = trn.amount,
                title = trn.description,
                dateTime = trn.date,
                categoryId = trn.suggestedCategoryId
            )
            writeTransactionDao.save(entity)

            // If a budget item was matched, create a split
            if (trn.suggestedBudgetItemId != null) {
                val split = TransactionSplit(
                    id = TransactionSplitId(UUID.randomUUID()),
                    transactionId = TransactionId(trnId),
                    budgetItemId = trn.suggestedBudgetItemId,
                    categoryId = trn.suggestedCategoryId?.let { CategoryId(it) },
                    amount = trn.amount,
                    note = trn.description
                )
                splitRepository.saveSplits(TransactionId(trnId), listOf(split))
            }
        }

        toImport.size
    }
}

