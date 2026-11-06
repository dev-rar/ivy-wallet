package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.domain.model.budget.MonthCloseSummary
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CloseMonthPlanUseCase @Inject constructor(
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend fun getSummary(period: BudgetPeriod): MonthCloseSummary =
        withContext(dispatchersProvider.io) {
            val planSummary = getMonthlyPlanSummaryUseCase(period)
            val plan = monthlyBudgetRepository.getOrCreatePlan(period)

            val nonIncomeGroups = planSummary.groups.filter { it.groupType != BudgetGroupType.INCOME }
            val totalSpent = nonIncomeGroups.sumOf { it.actualTotal }
            val incomeGroups = planSummary.groups.filter { it.groupType == BudgetGroupType.INCOME }
            val actualIncome = incomeGroups.sumOf { it.actualTotal }
            val effectiveIncome = if (actualIncome > 0.0) actualIncome + plan.carryoverAmount else planSummary.totalIncome
            val netSurplus = effectiveIncome - totalSpent

            val allItems = nonIncomeGroups.flatMap { it.items }
            val unspentCount = allItems.count { it.actualAmount < it.plannedAmount }
            val overspentCount = allItems.count { it.actualAmount > it.plannedAmount }

            MonthCloseSummary(
                period = period,
                isClosed = plan.isClosed,
                totalIncome = effectiveIncome,
                totalSpent = totalSpent,
                netSurplus = netSurplus,
                plannedIncome = plan.plannedIncome,
                plannedAllocated = planSummary.totalAllocated,
                unspentCategoriesCount = unspentCount,
                overspentCategoriesCount = overspentCount,
                carryoverAmount = plan.carryoverAmount
            )
        }

    suspend fun closeMonth(period: BudgetPeriod, rolloverSurplus: Boolean): MonthCloseSummary =
        withContext(dispatchersProvider.io) {
            val summary = getSummary(period)
            val currentPlan = monthlyBudgetRepository.getOrCreatePlan(period)
            monthlyBudgetRepository.savePlan(currentPlan.copy(isClosed = true))

            if (rolloverSurplus && summary.netSurplus > 0.0) {
                val nextPeriod = period.next()
                val nextPlan = monthlyBudgetRepository.getOrCreatePlan(nextPeriod)
                monthlyBudgetRepository.savePlan(
                    nextPlan.copy(carryoverAmount = nextPlan.carryoverAmount + summary.netSurplus)
                )
            }
            summary.copy(isClosed = true)
        }

    suspend fun reopenMonth(period: BudgetPeriod): Unit =
        withContext(dispatchersProvider.io) {
            val currentPlan = monthlyBudgetRepository.getOrCreatePlan(period)
            monthlyBudgetRepository.savePlan(currentPlan.copy(isClosed = false))
        }
}

