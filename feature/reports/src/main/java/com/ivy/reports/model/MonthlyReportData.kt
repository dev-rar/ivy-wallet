package com.ivy.reports.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.UUID

@Immutable
data class CategoryBudgetReportItem(
    val categoryId: UUID?,
    val categoryName: String,
    val categoryColor: Color,
    val categoryIcon: String?,
    val budgetAmount: Double,
    val spentAmount: Double,
    val savedAmount: Double,
    val isOverspent: Boolean,
    val hasBudget: Boolean,
    val incomeAmount: Double = 0.0,
    val monthlyBalance: Double = 0.0
)

@Immutable
data class CategoryHistoricalPoint(
    val monthName: String,
    val monthAbbrev: String,
    val spent: Double,
    val budget: Double
)

@Immutable
data class CategoryTrendSheetData(
    val categoryId: UUID?,
    val categoryName: String,
    val categoryColor: Color,
    val categoryIcon: String?,
    val history: ImmutableList<CategoryHistoricalPoint> = persistentListOf(),
    val averageMonthlySpent: Double = 0.0,
    val highestMonthName: String = "",
    val highestSpent: Double = 0.0,
    val isIncomeTrend: Boolean = false
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
    val selectedMonthIndex: Int = (java.time.LocalDate.now().monthValue - 1).coerceIn(0, 11),
    val selectedCategoryTrend: CategoryTrendSheetData? = null
)
