package com.ivy.domain.usecase.budget

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.data.repository.TransactionSplitRepository
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class GetMonthlyPlanSummaryUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionDao: TransactionDao,
    private val splitRepository: TransactionSplitRepository,
    private val categoryRepository: CategoryRepository,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(period: BudgetPeriod): MonthlyPlanSummary =
        withContext(dispatchersProvider.io) {
            val plan = monthlyBudgetRepository.getOrCreatePlan(period)
            val groups = monthlyBudgetRepository.findGroups(period)
            val items = monthlyBudgetRepository.findItems(period)
            val categories = categoryRepository.findAll().associateBy { it.id.value }

            val startInstant = period.startInstant()
            val endInstant = period.endInstant()
            val transactions = transactionDao.findAllBetween(startInstant, endInstant)
            val trnIds = transactions.map { it.id }.toSet()
            val allSplits = splitRepository.findByTransactionIds(trnIds)
            val splitsByTrnId = allSplits.groupBy { it.transactionId.value }

            val actualByBudgetItemId = mutableMapOf<UUID, Double>()
            val expenseByCategory = mutableMapOf<UUID, Double>()
            val incomeByCategory = mutableMapOf<UUID, Double>()

            for (trn in transactions) {
                val splits = splitsByTrnId[trn.id]
                if (splits.isNullOrEmpty()) {
                    if (trn.categoryId != null) {
                        when (trn.type) {
                            TransactionType.EXPENSE -> {
                                expenseByCategory[trn.categoryId!!] =
                                    (expenseByCategory[trn.categoryId!!] ?: 0.0) + trn.amount
                            }
                            TransactionType.INCOME -> {
                                incomeByCategory[trn.categoryId!!] =
                                    (incomeByCategory[trn.categoryId!!] ?: 0.0) + trn.amount
                            }
                            else -> {}
                        }
                    }
                } else {
                    for (split in splits) {
                        if (split.budgetItemId != null) {
                            actualByBudgetItemId[split.budgetItemId!!] =
                                (actualByBudgetItemId[split.budgetItemId!!] ?: 0.0) + split.amount
                        } else if (split.categoryId != null) {
                            when (trn.type) {
                                TransactionType.EXPENSE -> {
                                    expenseByCategory[split.categoryId!!.value] =
                                        (expenseByCategory[split.categoryId!!.value] ?: 0.0) + split.amount
                                }
                                TransactionType.INCOME -> {
                                    incomeByCategory[split.categoryId!!.value] =
                                        (incomeByCategory[split.categoryId!!.value] ?: 0.0) + split.amount
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }

            val itemsByGroupId = items.groupBy { it.groupId }

            val groupSummaries = groups.map { group ->
                val groupItems = itemsByGroupId[group.id].orEmpty()
                val groupType = runCatching { BudgetGroupType.valueOf(group.groupType) }.getOrDefault(BudgetGroupType.CUSTOM)
                val isIncomeGroup = groupType == BudgetGroupType.INCOME

                val itemSummaries = groupItems.map { item ->
                    val cat = item.categoryId?.let { categories[it] }
                    val itemType = runCatching { BudgetItemType.valueOf(item.itemType) }.getOrDefault(BudgetItemType.STANDARD)

                    val directActual = actualByBudgetItemId[item.id] ?: 0.0
                    val categoryActual = if (item.categoryId != null) {
                        if (isIncomeGroup) {
                            incomeByCategory[item.categoryId] ?: 0.0
                        } else {
                            expenseByCategory[item.categoryId] ?: 0.0
                        }
                    } else 0.0

                    val actual = directActual + categoryActual

                    MonthlyItemSummary(
                        id = item.id,
                        groupId = item.groupId,
                        name = item.name,
                        plannedAmount = item.plannedAmount,
                        actualAmount = actual,
                        itemType = itemType,
                        categoryId = item.categoryId?.let { CategoryId(it) },
                        categoryName = cat?.name?.value,
                        categoryColor = cat?.color?.value,
                        categoryIcon = cat?.icon?.id
                    )
                }

                val plannedTotal = itemSummaries.sumOf { it.plannedAmount }
                val actualTotal = itemSummaries.sumOf { it.actualAmount }

                MonthlyGroupSummary(
                    id = group.id,
                    name = group.name,
                    groupType = groupType,
                    orderNum = group.orderNum,
                    plannedTotal = plannedTotal,
                    actualTotal = actualTotal,
                    items = itemSummaries
                )
            }

            // Total Income = Planned Income + Carryover
            val totalIncome = plan.plannedIncome + plan.carryoverAmount

            // Total Allocated = Sum of all non-income planned items
            val totalAllocated = groupSummaries
                .filter { it.groupType != BudgetGroupType.INCOME }
                .sumOf { it.plannedTotal }

            val leftToAssign = totalIncome - totalAllocated

            MonthlyPlanSummary(
                period = period,
                plannedIncome = plan.plannedIncome,
                carryoverAmount = plan.carryoverAmount,
                totalIncome = totalIncome,
                totalAllocated = totalAllocated,
                leftToAssign = leftToAssign,
                groups = groupSummaries
            )
        }
}
