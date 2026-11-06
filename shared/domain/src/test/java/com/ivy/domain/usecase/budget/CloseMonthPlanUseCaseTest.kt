package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeMonthlyBudgetDao
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import com.ivy.data.model.CategoryId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class CloseMonthPlanUseCaseTest {
    private lateinit var monthlyBudgetDao: FakeMonthlyBudgetDao
    private lateinit var monthlyBudgetRepository: MonthlyBudgetRepository
    private lateinit var getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase
    private lateinit var closeMonthPlanUseCase: CloseMonthPlanUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    private val period = BudgetPeriod.of(2026, 11)

    @Before
    fun setup() {
        monthlyBudgetDao = FakeMonthlyBudgetDao()
        monthlyBudgetRepository = MonthlyBudgetRepository(
            monthlyBudgetDao = monthlyBudgetDao,
            writeMonthlyBudgetDao = monthlyBudgetDao,
            dispatchersProvider = dispatchers
        )
        getMonthlyPlanSummaryUseCase = mockk()

        closeMonthPlanUseCase = CloseMonthPlanUseCase(
            monthlyBudgetRepository = monthlyBudgetRepository,
            getMonthlyPlanSummaryUseCase = getMonthlyPlanSummaryUseCase,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `getSummary correctly summarizes surplus and unspent items`() = runTest {
        // Arrange
        val plan = MonthlyBudgetPlanEntity(
            id = UUID.randomUUID(),
            period = period.value,
            plannedIncome = 150000.0,
            carryoverAmount = 10000.0,
            isClosed = false
        )
        monthlyBudgetDao.savePlan(plan)

        val foodGroupId = UUID.randomUUID()
        val item1 = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = foodGroupId,
            name = "Groceries",
            plannedAmount = 30000.0,
            actualAmount = 25000.0, // unspent
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )
        val item2 = MonthlyItemSummary(
            id = UUID.randomUUID(),
            groupId = foodGroupId,
            name = "Dining Out",
            plannedAmount = 10000.0,
            actualAmount = 12000.0, // overspent
            itemType = BudgetItemType.STANDARD,
            categoryId = CategoryId(UUID.randomUUID())
        )

        val groups = listOf(
            MonthlyGroupSummary(
                id = foodGroupId,
                name = "Food",
                groupType = BudgetGroupType.FOOD,
                orderNum = 1.0,
                plannedTotal = 40000.0,
                actualTotal = 37000.0,
                items = listOf(item1, item2)
            )
        )

        val planSummary = MonthlyPlanSummary(
            period = period,
            plannedIncome = 150000.0,
            carryoverAmount = 10000.0,
            totalIncome = 160000.0,
            totalAllocated = 40000.0,
            leftToAssign = 120000.0,
            groups = groups,
            isClosed = false
        )

        coEvery { getMonthlyPlanSummaryUseCase(period) } returns planSummary

        // Act
        val summary = closeMonthPlanUseCase.getSummary(period)

        // Assert
        summary.period shouldBe period
        summary.isClosed shouldBe false
        summary.totalIncome shouldBe 160000.0
        summary.totalSpent shouldBe 37000.0
        summary.netSurplus shouldBe 123000.0
        summary.unspentCategoriesCount shouldBe 1
        summary.overspentCategoriesCount shouldBe 1
    }

    @Test
    fun `closeMonth with rollover transfers net surplus to next month plan`() = runTest {
        // Arrange
        val plan = MonthlyBudgetPlanEntity(
            id = UUID.randomUUID(),
            period = period.value,
            plannedIncome = 100000.0,
            carryoverAmount = 0.0,
            isClosed = false
        )
        monthlyBudgetDao.savePlan(plan)

        val groups = listOf(
            MonthlyGroupSummary(
                id = UUID.randomUUID(),
                name = "Expenses",
                groupType = BudgetGroupType.LIFESTYLE,
                orderNum = 1.0,
                plannedTotal = 80000.0,
                actualTotal = 70000.0,
                items = emptyList()
            )
        )

        val planSummary = MonthlyPlanSummary(
            period = period,
            plannedIncome = 100000.0,
            carryoverAmount = 0.0,
            totalIncome = 100000.0,
            totalAllocated = 80000.0,
            leftToAssign = 20000.0,
            groups = groups,
            isClosed = false
        )

        coEvery { getMonthlyPlanSummaryUseCase(period) } returns planSummary

        // Act
        val summary = closeMonthPlanUseCase.closeMonth(period, rolloverSurplus = true)

        // Assert
        summary.isClosed shouldBe true
        val closedPlan = monthlyBudgetDao.findPlanByPeriod(period.value)
        closedPlan?.isClosed shouldBe true

        val nextPeriod = period.next()
        val nextPlan = monthlyBudgetDao.findPlanByPeriod(nextPeriod.value)
        nextPlan?.carryoverAmount shouldBe 30000.0 // 100000 - 70000 = 30000 surplus
    }

    @Test
    fun `reopenMonth marks month as open`() = runTest {
        val plan = MonthlyBudgetPlanEntity(
            id = UUID.randomUUID(),
            period = period.value,
            plannedIncome = 100000.0,
            carryoverAmount = 0.0,
            isClosed = true
        )
        monthlyBudgetDao.savePlan(plan)

        closeMonthPlanUseCase.reopenMonth(period)

        val reopenedPlan = monthlyBudgetDao.findPlanByPeriod(period.value)
        reopenedPlan?.isClosed shouldBe false
    }
}

