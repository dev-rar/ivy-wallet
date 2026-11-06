package com.ivy.domain.model.report

import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetPeriod

data class PlannedVsActualReport(
    val period: BudgetPeriod,
    val totalPlannedIncome: Double,
    val totalActualIncome: Double,
    val totalPlannedExpenses: Double,
    val totalActualExpenses: Double,
    val netPlanned: Double,
    val netActual: Double,
    val groups: List<PlannedVsActualGroupReport>
)

data class PlannedVsActualGroupReport(
    val name: String,
    val groupType: BudgetGroupType,
    val plannedTotal: Double,
    val actualTotal: Double,
    val variance: Double,
    val items: List<PlannedVsActualItemReport>
)

data class PlannedVsActualItemReport(
    val name: String,
    val plannedAmount: Double,
    val actualAmount: Double,
    val variance: Double, // planned - actual (positive = savings, negative = overspent)
    val isOverspent: Boolean
)

