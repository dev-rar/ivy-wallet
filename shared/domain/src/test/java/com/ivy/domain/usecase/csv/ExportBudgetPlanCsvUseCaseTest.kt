package com.ivy.domain.usecase.csv

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class ExportBudgetPlanCsvUseCaseTest {
    private lateinit var getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase
    private lateinit var useCase: ExportBudgetPlanCsvUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    private val period = BudgetPeriod.of(2026, 11)

    @Before
    fun setup() {
        getMonthlyPlanSummaryUseCase = mockk()
        useCase = ExportBudgetPlanCsvUseCase(
            getMonthlyPlanSummaryUseCase = getMonthlyPlanSummaryUseCase,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `exportCsv creates proper headers and item rows`() = runTest {
        val groupId = UUID.randomUUID()
        val item = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = groupId,
            name = "Rent & Utilities",
            plannedAmount = 75000.0,
            actualAmount = 75000.0,
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )

        val group = MonthlyGroupSummary(
            id = groupId,
            name = "Housing",
            groupType = BudgetGroupType.HOUSING,
            orderNum = 1.0,
            plannedTotal = 75000.0,
            actualTotal = 75000.0,
            items = listOf(item)
        )

        val summary = MonthlyPlanSummary(
            period = period,
            plannedIncome = 100000.0,
            carryoverAmount = 0.0,
            totalIncome = 100000.0,
            totalAllocated = 75000.0,
            leftToAssign = 25000.0,
            groups = listOf(group),
            isClosed = false
        )

        coEvery { getMonthlyPlanSummaryUseCase(period) } returns summary

        val csv = useCase.exportCsv(period)

        csv shouldContain "Period,Group,Item,Type,Planned Amount,Actual Amount,Remaining Amount,Progress"
        csv shouldContain "2026-11"
        csv shouldContain "Housing"
        csv shouldContain "Rent & Utilities"
        csv shouldContain "75,000.00"
    }
}

