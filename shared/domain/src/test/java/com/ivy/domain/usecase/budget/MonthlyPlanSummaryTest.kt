package com.ivy.domain.usecase.budget

import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import io.kotest.matchers.shouldBe
import org.junit.Test
import java.util.UUID

class MonthlyPlanSummaryTest {
    @Test
    fun `zero-based calculation with full allocation`() {
        val period = BudgetPeriod.of(2026, 11)
        val housingGroupId = UUID.randomUUID()
        val foodGroupId = UUID.randomUUID()

        val rentItem = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = housingGroupId,
            name = "Rent",
            plannedAmount = 60000.0,
            actualAmount = 60000.0,
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )
        val groceriesItem = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = foodGroupId,
            name = "Groceries",
            plannedAmount = 40000.0,
            actualAmount = 35000.0,
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )

        val groups = listOf(
            MonthlyGroupSummary(
                id = housingGroupId,
                name = "Housing",
                groupType = BudgetGroupType.HOUSING,
                orderNum = 1.0,
                plannedTotal = 60000.0,
                actualTotal = 60000.0,
                items = listOf(rentItem)
            ),
            MonthlyGroupSummary(
                id = foodGroupId,
                name = "Food",
                groupType = BudgetGroupType.FOOD,
                orderNum = 2.0,
                plannedTotal = 40000.0,
                actualTotal = 35000.0,
                items = listOf(groceriesItem)
            )
        )

        val plan = MonthlyPlanSummary(
            period = period,
            plannedIncome = 100000.0,
            carryoverAmount = 0.0,
            totalIncome = 100000.0,
            totalAllocated = 100000.0,
            leftToAssign = 0.0,
            groups = groups
        )

        plan.totalIncome shouldBe 100000.0
        plan.totalAllocated shouldBe 100000.0
        plan.leftToAssign shouldBe 0.0

        rentItem.remainingAmount shouldBe 0.0
        rentItem.progress shouldBe 1.0f

        groceriesItem.remainingAmount shouldBe 5000.0
        groceriesItem.progress shouldBe 0.875f
    }
}

