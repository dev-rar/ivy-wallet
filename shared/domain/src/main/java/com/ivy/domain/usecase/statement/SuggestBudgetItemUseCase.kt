package com.ivy.domain.usecase.statement

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.MonthlyBudgetDao
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.budget.BudgetPeriod
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

data class BudgetSuggestion(
    val categoryId: UUID?,
    val budgetItemId: UUID?,
    val confidence: Double // 0.0 to 1.0
)

class SuggestBudgetItemUseCase @Inject constructor(
    private val transactionDao: TransactionDao,
    private val monthlyBudgetDao: MonthlyBudgetDao,
    private val dispatchers: DispatchersProvider,
) {
    suspend fun suggest(
        description: String,
        period: BudgetPeriod
    ): BudgetSuggestion = withContext(dispatchers.io) {
        val query = description.trim().lowercase()
        if (query.isBlank()) {
            return@withContext BudgetSuggestion(null, null, 0.0)
        }

        // 1. Check current month's budget items for direct name match
        val budgetItems = monthlyBudgetDao.findItemsByPeriod(period.value)
        val directItemMatch = budgetItems.find { item ->
            val itemName = item.name.lowercase()
            query.contains(itemName) || itemName.contains(query)
        }
        if (directItemMatch != null) {
            return@withContext BudgetSuggestion(
                categoryId = directItemMatch.categoryId,
                budgetItemId = directItemMatch.id,
                confidence = 0.95
            )
        }

        // 2. Check transaction history for description similarity
        val recentTransactions = transactionDao.findAll()
        val matchingTrn = recentTransactions.firstOrNull { trn ->
            val title = trn.title?.lowercase().orEmpty()
            val desc = trn.description?.lowercase().orEmpty()
            (title.isNotBlank() && (query.contains(title) || title.contains(query))) ||
                    (desc.isNotBlank() && (query.contains(desc) || desc.contains(query)))
        }

        if (matchingTrn != null && matchingTrn.categoryId != null) {
            val matchingItem = budgetItems.find { it.categoryId == matchingTrn.categoryId }
            return@withContext BudgetSuggestion(
                categoryId = matchingTrn.categoryId,
                budgetItemId = matchingItem?.id,
                confidence = 0.75
            )
        }

        BudgetSuggestion(null, null, 0.0)
    }
}

