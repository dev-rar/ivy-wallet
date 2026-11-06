package com.ivy.domain.usecase.report

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.report.PlannedVsActualGroupReport
import com.ivy.domain.model.report.PlannedVsActualItemReport
import com.ivy.domain.model.report.PlannedVsActualReport
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetPlannedVsActualReportUseCase @Inject constructor(
    private val getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(period: BudgetPeriod): PlannedVsActualReport =
        withContext(dispatchersProvider.io) {
            val summary = getMonthlyPlanSummaryUseCase(period)

            val incomeGroups = summary.groups.filter { it.groupType == BudgetGroupType.INCOME }
            val expenseGroups = summary.groups.filter { it.groupType != BudgetGroupType.INCOME }

            val totalPlannedIncome = summary.totalIncome
            val totalActualIncome = incomeGroups.sumOf { it.actualTotal }

            val totalPlannedExpenses = expenseGroups.sumOf { it.plannedTotal }
            val totalActualExpenses = expenseGroups.sumOf { it.actualTotal }

            val groupReports = summary.groups.map { group ->
                val items = group.items.map { item ->
                    val variance = item.plannedAmount - item.actualAmount
                    PlannedVsActualItemReport(
                        name = item.name,
                        plannedAmount = item.plannedAmount,
                        actualAmount = item.actualAmount,
                        variance = variance,
                        isOverspent = item.actualAmount > item.plannedAmount
                    )
                }

                val variance = group.plannedTotal - group.actualTotal
                PlannedVsActualGroupReport(
                    name = group.name,
                    groupType = group.groupType,
                    plannedTotal = group.plannedTotal,
                    actualTotal = group.actualTotal,
                    variance = variance,
                    items = items
                )
            }

            PlannedVsActualReport(
                period = period,
                totalPlannedIncome = totalPlannedIncome,
                totalActualIncome = totalActualIncome,
                totalPlannedExpenses = totalPlannedExpenses,
                totalActualExpenses = totalActualExpenses,
                netPlanned = totalPlannedIncome - totalPlannedExpenses,
                netActual = totalActualIncome - totalActualExpenses,
                groups = groupReports
            )
        }
}

