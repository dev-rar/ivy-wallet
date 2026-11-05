package com.ivy.budgets.model

import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import java.util.UUID

data class EditBudgetItemModalData(
    val itemId: UUID? = null,
    val groupId: UUID,
    val period: BudgetPeriod,
    val initialName: String = "",
    val initialPlannedAmount: Double = 0.0,
    val initialCategoryId: UUID? = null,
    val initialItemType: BudgetItemType = BudgetItemType.STANDARD
)

