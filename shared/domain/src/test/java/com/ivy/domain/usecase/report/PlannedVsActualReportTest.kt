package com.ivy.domain.usecase.report

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PlannedVsActualReportTest {
    private lateinit var getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase
    private lateinit var useCase: GetPlannedVsActualReportUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    private val period = BudgetPeriod.of(2026, 11)

    @Before
    fun setup() {
        getMonthlyPlanSummaryUseCase = mockk()
        useCase = GetPlannedVsActualReportUseCase(
            getMonthlyPlanSummaryUseCase = getMonthlyPlanSummaryUseCase,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `report computes income, expense, and item variances correctly`() = runTest {
        val incomeGroupId = UUID.randomUUID()
        val foodGroupId = UUID.randomUUID()

        val salaryItem = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = incomeGroupId,
            name = "Salary",
            plannedAmount = 150000.0,
            actualAmount = 155000.0,
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )

        val groceriesItem = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = foodGroupId,
            name = "Groceries",
            plannedAmount = 40000.0,
            actualAmount = 45000.0, // overspent by 5000
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )

        val groups = listOf(
            MonthlyGroupSummary(
                id = incomeGroupId,
                name = "Income",
                groupType = BudgetGroupType.INCOME,
                orderNum = 1.0,
                plannedTotal = 150000.0,
                actualTotal = 155000.0,
                items = listOf(salaryItem)
            ),
            MonthlyGroupSummary(
                id = foodGroupId,
                name = "Food",
                groupType = BudgetGroupType.FOOD,
                orderNum = 2.0,
                plannedTotal = 40000.0,
                actualTotal = 45000.0,
                items = listOf(groceriesItem)
            )
        )

        val summary = MonthlyPlanSummary(
            period = period,
            plannedIncome = 150000.0,
            carryoverAmount = 0.0,
            totalIncome = 150000.0,
            totalAllocated = 40000.0,
            leftToAssign = 110000.0,
            groups = groups,
            isClosed = false
        )

        coEvery { getMonthlyPlanSummaryUseCase(period) } returns summary

        val report = useCase(period)

        report.period shouldBe period
        report.totalPlannedIncome shouldBe 150000.0
        report.totalActualIncome shouldBe 155000.0
        report.totalPlannedExpenses shouldBe 40000.0
        report.totalActualExpenses shouldBe 45000.0
        report.netPlanned shouldBe 110000.0
        report.netActual shouldBe 110000.0 // 155000 - 45000

        val foodReport = report.groups.first { it.groupType == BudgetGroupType.FOOD }
        foodReport.variance shouldBe -5000.0
        val groceryReport = foodReport.items.first()
        groceryReport.isOverspent shouldBe true
        groceryReport.variance shouldBe -5000.0
    }
}

