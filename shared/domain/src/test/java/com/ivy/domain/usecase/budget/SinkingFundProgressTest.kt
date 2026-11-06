package com.ivy.domain.usecase.budget

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakeMonthlyBudgetDao
import com.ivy.data.db.dao.fake.FakeSinkingFundDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.model.budget.SinkingFund
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonZeroDouble
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.data.repository.SinkingFundRepository
import com.ivy.domain.usecase.account.AccountBalanceUseCase
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class SinkingFundProgressTest {
    private lateinit var sinkingFundDao: FakeSinkingFundDao
    private lateinit var monthlyBudgetDao: FakeMonthlyBudgetDao
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var accountDao: FakeAccountDao
    private lateinit var accountBalanceUseCase: AccountBalanceUseCase
    private lateinit var sinkingFundRepository: SinkingFundRepository
    private lateinit var monthlyBudgetRepository: MonthlyBudgetRepository
    private lateinit var useCase: GetSinkingFundProgressUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    @Before
    fun setup() {
        sinkingFundDao = FakeSinkingFundDao()
        monthlyBudgetDao = FakeMonthlyBudgetDao()
        transactionDao = FakeTransactionDao()
        accountDao = FakeAccountDao()
        accountBalanceUseCase = mockk()

        sinkingFundRepository = SinkingFundRepository(
            sinkingFundDao = sinkingFundDao,
            writeSinkingFundDao = sinkingFundDao,
            dispatchersProvider = dispatchers
        )

        monthlyBudgetRepository = MonthlyBudgetRepository(
            monthlyBudgetDao = monthlyBudgetDao,
            writeMonthlyBudgetDao = monthlyBudgetDao,
            dispatchersProvider = dispatchers
        )

        useCase = GetSinkingFundProgressUseCase(
            sinkingFundRepository = sinkingFundRepository,
            monthlyBudgetRepository = monthlyBudgetRepository,
            transactionDao = transactionDao,
            accountDao = accountDao,
            accountBalanceUseCase = accountBalanceUseCase,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `calculates target progress and monthly needed contribution`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.of(2026, 11)

        // Target 120,000 in 6 months (~180 days in the future)
        val targetDate = Instant.now().plus(180, ChronoUnit.DAYS)
        val fund = SinkingFund(
            id = UUID.randomUUID(),
            budgetItemId = budgetItemId,
            targetAmount = 120000.0,
            targetDate = targetDate,
            openingBalance = 30000.0,
            notes = "Annual Car Insurance"
        )
        sinkingFundRepository.saveSinkingFund(fund)

        // Current month allocates 10,000 planned
        monthlyBudgetDao.saveItems(
            listOf(
                BudgetItemEntity(
                    id = budgetItemId,
                    groupId = UUID.randomUUID(),
                    planPeriod = period.toString(),
                    name = "Car Insurance",
                    plannedAmount = 10000.0,
                    itemType = "SINKING_FUND"
                )
            )
        )

        // Mock bank account with 100,000 liquid cash
        val bankAccount = AccountEntity(
            id = UUID.randomUUID(),
            name = "Meezan Bank",
            currency = "PKR",
            accountType = "BANK",
            color = 0
        )
        accountDao.save(bankAccount)
        val pkrCode = AssetCode.from("PKR").getOrNull()!!
        coEvery { accountBalanceUseCase.calculate(AccountId(bankAccount.id)) } returns mapOf(
            pkrCode to NonZeroDouble.from(100000.0).getOrNull()!!
        )

        val progress = useCase(budgetItemId, period)
        progress shouldBe progress // not null
        progress!!.targetAmount shouldBe 120000.0
        // current saved: 30,000 opening + 10,000 planned = 40,000
        progress.currentSavedAmount shouldBe 40000.0
        progress.remainingAmount shouldBe 80000.0
        progress.progressPercent.toDouble() shouldBe (0.3333 plusOrMinus 0.01)
        progress.isSolvent shouldBe true // 100,000 liquid >= 40,000 saved
        progress.isFullyFunded shouldBe false
        progress.monthlyContributionNeeded shouldBe (80000.0 / progress.monthsRemaining!!)
    }

    @Test
    fun `flags insolvent when liquid cash is less than saved amount`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.of(2026, 11)

        val fund = SinkingFund(
            id = UUID.randomUUID(),
            budgetItemId = budgetItemId,
            targetAmount = 50000.0,
            openingBalance = 40000.0,
            notes = "Emergency Fund"
        )
        sinkingFundRepository.saveSinkingFund(fund)

        // Mock bank account with only 20,000 liquid cash (less than 40,000 saved)
        val bankAccount = AccountEntity(
            id = UUID.randomUUID(),
            name = "Cash",
            currency = "PKR",
            accountType = "CASH",
            color = 0
        )
        accountDao.save(bankAccount)
        val pkrCode = AssetCode.from("PKR").getOrNull()!!
        coEvery { accountBalanceUseCase.calculate(AccountId(bankAccount.id)) } returns mapOf(
            pkrCode to NonZeroDouble.from(20000.0).getOrNull()!!
        )

        val progress = useCase(budgetItemId, period)
        progress!!.isSolvent shouldBe false
        progress.isFullyFunded shouldBe false
    }

    @Test
    fun `marks fully funded when saved reaches target`() = runTest {
        val budgetItemId = UUID.randomUUID()
        val period = BudgetPeriod.of(2026, 11)

        val fund = SinkingFund(
            id = UUID.randomUUID(),
            budgetItemId = budgetItemId,
            targetAmount = 50000.0,
            openingBalance = 50000.0
        )
        sinkingFundRepository.saveSinkingFund(fund)

        val bankAccount = AccountEntity(
            id = UUID.randomUUID(),
            name = "Bank",
            currency = "PKR",
            accountType = "BANK",
            color = 0
        )
        accountDao.save(bankAccount)
        val pkrCode = AssetCode.from("PKR").getOrNull()!!
        coEvery { accountBalanceUseCase.calculate(AccountId(bankAccount.id)) } returns mapOf(
            pkrCode to NonZeroDouble.from(60000.0).getOrNull()!!
        )

        val progress = useCase(budgetItemId, period)
        progress!!.isFullyFunded shouldBe true
        progress.remainingAmount shouldBe 0.0
        progress.progressPercent shouldBe 1.0f
    }
}

