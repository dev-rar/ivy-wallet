package com.ivy.domain.usecase.budget

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.BillStatus
import com.ivy.domain.model.budget.BudgetItemBillSummary
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject

class GetBudgetItemBillsUseCase @Inject constructor(
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val transactionDao: TransactionDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun getBillsForPeriod(period: BudgetPeriod): Map<UUID, List<BudgetItemBillSummary>> =
        withContext(dispatchersProvider.io) {
            val rules = plannedPaymentRuleDao.findAll()
            val startInstant = period.startInstant()
            val endInstant = period.endInstant()
            val monthTransactions = transactionDao.findAllBetween(startInstant, endInstant)
                .filter { it.type == TransactionType.EXPENSE }

            val billsByBudgetItem = mutableMapOf<UUID, MutableList<BudgetItemBillSummary>>()
            val now = Instant.now()
            val zone = ZoneId.systemDefault()

            for (rule in rules) {
                val budgetItemId = rule.budgetItemId ?: continue

                // Determine due date in this period
                val ruleStartDate = rule.startDate
                val dueDate: Instant? = if (ruleStartDate != null) {
                    val startZdt = ruleStartDate.atZone(zone)
                    val periodZdt = period.toYearMonth().atDay(minOf(startZdt.dayOfMonth, period.toYearMonth().lengthOfMonth()))
                        .atTime(startZdt.toLocalTime())
                        .atZone(zone)
                    periodZdt.toInstant()
                } else null

                // Determine if paid: a transaction with recurringRuleId == rule.id, or matching budgetItemId / title
                val isPaid = monthTransactions.any { trn ->
                    trn.recurringRuleId == rule.id ||
                            (trn.description?.contains(rule.title.orEmpty(), ignoreCase = true) == true && trn.amount >= rule.amount * 0.95)
                }

                val status = when {
                    isPaid -> BillStatus.PAID
                    dueDate != null && dueDate.isBefore(now) -> BillStatus.OVERDUE
                    dueDate != null && dueDate.isBefore(now.plus(3, ChronoUnit.DAYS)) -> BillStatus.DUE_SOON
                    else -> BillStatus.SCHEDULED
                }

                val summary = BudgetItemBillSummary(
                    ruleId = rule.id,
                    budgetItemId = budgetItemId,
                    title = rule.title ?: "Recurring Bill",
                    amount = rule.amount,
                    dueDate = dueDate,
                    status = status
                )

                billsByBudgetItem.getOrPut(budgetItemId) { mutableListOf() }.add(summary)
            }

            billsByBudgetItem
        }
}

