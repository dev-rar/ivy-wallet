package com.ivy.domain.usecase.budget

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakePlannedPaymentDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.IntervalType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.BillStatus
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class BudgetItemBillsTest {
    private lateinit var plannedPaymentDao: FakePlannedPaymentDao
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var useCase: GetBudgetItemBillsUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    @Before
    fun setup() {
        plannedPaymentDao = FakePlannedPaymentDao()
        transactionDao = FakeTransactionDao()
        useCase = GetBudgetItemBillsUseCase(
            plannedPaymentRuleDao = plannedPaymentDao,
            transactionDao = transactionDao,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `overdue bill detected when past due date without transaction`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.current()

        // Bill due 5 days ago
        val pastDate = Instant.now().minus(5, ChronoUnit.DAYS)
        val rule = PlannedPaymentRuleEntity(
            id = UUID.randomUUID(),
            budgetItemId = budgetItemId,
            startDate = pastDate,
            intervalN = 1,
            intervalType = IntervalType.MONTH,
            oneTime = false,
            type = TransactionType.EXPENSE,
            accountId = UUID.randomUUID(),
            amount = 15000.0,
            title = "Electricity Bill"
        )
        plannedPaymentDao.save(rule)

        val billsMap = useCase.getBillsForPeriod(period)
        val bills = billsMap[budgetItemId]
        bills shouldBe bills
        bills!!.size shouldBe 1
        bills[0].status shouldBe BillStatus.OVERDUE
        bills[0].amount shouldBe 15000.0
    }

    @Test
    fun `paid bill detected when transaction exists for recurring rule`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.current()
        val ruleId = UUID.randomUUID()

        val rule = PlannedPaymentRuleEntity(
            id = ruleId,
            budgetItemId = budgetItemId,
            startDate = Instant.now().minus(2, ChronoUnit.DAYS),
            intervalN = 1,
            intervalType = IntervalType.MONTH,
            oneTime = false,
            type = TransactionType.EXPENSE,
            accountId = UUID.randomUUID(),
            amount = 4500.0,
            title = "Internet Bill"
        )
        plannedPaymentDao.save(rule)

        // Add matching paid transaction
        val trn = TransactionEntity(
            id = UUID.randomUUID(),
            accountId = UUID.randomUUID(),
            amount = 4500.0,
            type = TransactionType.EXPENSE,
            recurringRuleId = ruleId,
            dateTime = Instant.now().minus(1, ChronoUnit.DAYS)
        )
        transactionDao.save(trn)

        val billsMap = useCase.getBillsForPeriod(period)
        val bills = billsMap[budgetItemId]
        bills!!.size shouldBe 1
        bills[0].status shouldBe BillStatus.PAID
    }

    @Test
    fun `due soon bill detected when due within 3 days`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.current()

        // Bill due tomorrow
        val futureDate = Instant.now().plus(1, ChronoUnit.DAYS)
        val rule = PlannedPaymentRuleEntity(
            id = UUID.randomUUID(),
            budgetItemId = budgetItemId,
            startDate = futureDate,
            intervalN = 1,
            intervalType = IntervalType.MONTH,
            oneTime = false,
            type = TransactionType.EXPENSE,
            accountId = UUID.randomUUID(),
            amount = 3000.0,
            title = "Water Utility"
        )
        plannedPaymentDao.save(rule)

        val billsMap = useCase.getBillsForPeriod(period)
        val bills = billsMap[budgetItemId]
        bills!!.size shouldBe 1
        bills[0].status shouldBe BillStatus.DUE_SOON
    }
}

