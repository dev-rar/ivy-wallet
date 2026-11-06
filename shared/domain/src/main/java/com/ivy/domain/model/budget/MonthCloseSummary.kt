package com.ivy.domain.model.budget

import com.ivy.data.model.budget.BudgetPeriod

data class MonthCloseSummary(
    val period: BudgetPeriod,
    val isClosed: Boolean,
    val totalIncome: Double,
    val totalSpent: Double,
    val netSurplus: Double,
    val plannedIncome: Double,
    val plannedAllocated: Double,
    val unspentCategoriesCount: Int,
    val overspentCategoriesCount: Int,
    val carryoverAmount: Double,
)

