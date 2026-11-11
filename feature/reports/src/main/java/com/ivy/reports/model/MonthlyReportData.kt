package com.ivy.reports.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CategoryBudgetReportItem(
    val categoryName: String,
    val categoryColor: Color,
    val categoryIcon: String?,
    val budgetAmount: Double,
    val spentAmount: Double,
    val savedAmount: Double,
    val isOverspent: Boolean
)

@Immutable
data class MonthlyReportData(
    val month: Int,
    val monthName: String,
    val monthAbbrev: String,
    val income: Double,
    val expenses: Double,
    val netSavings: Double,
    val totalBudgeted: Double,
    val totalBudgetSpent: Double,
    val totalBudgetSavings: Double,
    val categoryBudgets: ImmutableList<CategoryBudgetReportItem> = persistentListOf()
)

@Immutable
data class YearlyReportState(
    val year: Int = java.time.LocalDate.now().year,
    val baseCurrency: String = "",
    val totalAnnualIncome: Double = 0.0,
    val totalAnnualExpenses: Double = 0.0,
    val netAnnualSavings: Double = 0.0,
    val annualSavingsRate: Double = 0.0,
    val totalAnnualBudgetSavings: Double = 0.0,
    val monthlyData: ImmutableList<MonthlyReportData> = persistentListOf(),
    val selectedMonthIndex: Int = (java.time.LocalDate.now().monthValue - 1).coerceIn(0, 11)
)

