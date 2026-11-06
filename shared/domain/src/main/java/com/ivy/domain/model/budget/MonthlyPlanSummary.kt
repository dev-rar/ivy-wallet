package com.ivy.domain.model.budget

import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import java.util.UUID

data class MonthlyPlanSummary(
    val period: BudgetPeriod,
    val plannedIncome: Double,
    val carryoverAmount: Double,
    val totalIncome: Double,
    val totalAllocated: Double,
    val leftToAssign: Double,
    val groups: List<MonthlyGroupSummary>,
    val isClosed: Boolean = false
)

data class MonthlyGroupSummary(
    val id: UUID,
    val name: String,
    val groupType: BudgetGroupType,
    val orderNum: Double,
    val plannedTotal: Double,
    val actualTotal: Double,
    val items: List<MonthlyItemSummary>
)

data class MonthlyItemSummary(
    val id: UUID,
    val groupId: UUID,
    val name: String,
    val plannedAmount: Double,
    val actualAmount: Double,
    val itemType: BudgetItemType,
    val categoryId: CategoryId?,
    val categoryName: String? = null,
    val categoryColor: Int? = null,
    val categoryIcon: String? = null,
    val sinkingFundProgress: SinkingFundProgress? = null,
    val bills: List<BudgetItemBillSummary> = emptyList()
) {
    val remainingAmount: Double
        get() = plannedAmount - actualAmount

    val progress: Float
        get() = if (plannedAmount > 0.0) {
            (actualAmount / plannedAmount).coerceIn(0.0, 1.0).toFloat()
        } else 0f
}

