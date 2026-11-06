package com.ivy.domain.usecase.cashflow

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakePlannedPaymentDao
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.IntervalType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonZeroDouble
import com.ivy.domain.usecase.account.AccountBalanceUseCase
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class ProjectCashFlowUseCaseTest {
    private lateinit var accountDao: FakeAccountDao
    private lateinit var plannedPaymentDao: FakePlannedPaymentDao
    private lateinit var accountBalanceUseCase: AccountBalanceUseCase
    private lateinit var useCase: ProjectCashFlowUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    @Before
    fun setup() {
        accountDao = FakeAccountDao()
        plannedPaymentDao = FakePlannedPaymentDao()
        accountBalanceUseCase = mockk()

        useCase = ProjectCashFlowUseCase(
            accountDao = accountDao,
            accountBalanceUseCase = accountBalanceUseCase,
            plannedPaymentRuleDao = plannedPaymentDao,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `projects cash trajectory across scheduled paychecks and bills`() = runTest {
        val period = BudgetPeriod.of(2026, 11)
        val zone = ZoneId.systemDefault()

        // Bank account starts with 50,000 PKR
        val bank = AccountEntity(
            id = UUID.randomUUID(),
            name = "Meezan Bank",
            currency = "PKR",
            accountType = "BANK",
            color = 0
        )
        accountDao.save(bank)
        val pkrCode = AssetCode.from("PKR").getOrNull()!!
        coEvery { accountBalanceUseCase.calculate(AccountId(bank.id)) } returns mapOf(
            pkrCode to NonZeroDouble.from(50000.0).getOrNull()!!
        )

        // Rent bill on the 5th of November (-30,000)
        val date5 = LocalDate.of(2026, 11, 5).atStartOfDay(zone).toInstant()
        plannedPaymentDao.save(
            PlannedPaymentRuleEntity(
                id = UUID.randomUUID(),
                startDate = date5,
                intervalN = 1,
                intervalType = IntervalType.MONTH,
                oneTime = false,
                type = TransactionType.EXPENSE,
                accountId = bank.id,
                amount = 30000.0,
                title = "House Rent"
            )
        )

        // Paycheck on the 10th of November (+100,000)
        val date10 = LocalDate.of(2026, 11, 10).atStartOfDay(zone).toInstant()
        plannedPaymentDao.save(
            PlannedPaymentRuleEntity(
                id = UUID.randomUUID(),
                startDate = date10,
                intervalN = 1,
                intervalType = IntervalType.MONTH,
                oneTime = false,
                type = TransactionType.INCOME,
                accountId = bank.id,
                amount = 100000.0,
                title = "Salary Paycheck"
            )
        )

        val projection = useCase(period, safetyBuffer = 10000.0)
        projection.startingCash shouldBe 50000.0
        projection.events.size shouldBe 2
        // On 5th: 50,000 - 30,000 = 20,000
        projection.events[0].date shouldBe LocalDate.of(2026, 11, 5)
        projection.events[0].projectedBalance shouldBe 20000.0
        // On 10th: 20,000 + 100,000 = 120,000
        projection.events[1].date shouldBe LocalDate.of(2026, 11, 10)
        projection.events[1].projectedBalance shouldBe 120000.0

        projection.endingCash shouldBe 120000.0
        projection.minimumBalance shouldBe 20000.0
        projection.hasShortfall shouldBe false // 20,000 >= 10,000 buffer
    }

    @Test
    fun `detects shortfall when bill exceeds available buffer before paycheck arrives`() = runTest {
        val period = BudgetPeriod.of(2026, 11)
        val zone = ZoneId.systemDefault()

        // Starting with only 15,000 PKR
        val bank = AccountEntity(
            id = UUID.randomUUID(),
            name = "Bank",
            currency = "PKR",
            accountType = "BANK",
            color = 0
        )
        accountDao.save(bank)
        val pkrCode = AssetCode.from("PKR").getOrNull()!!
        coEvery { accountBalanceUseCase.calculate(AccountId(bank.id)) } returns mapOf(
            pkrCode to NonZeroDouble.from(15000.0).getOrNull()!!
        )

        // Big bill due on the 3rd (-25,000) -> dips to -10,000!
        val date3 = LocalDate.of(2026, 11, 3).atStartOfDay(zone).toInstant()
        plannedPaymentDao.save(
            PlannedPaymentRuleEntity(
                id = UUID.randomUUID(),
                startDate = date3,
                intervalN = 1,
                intervalType = IntervalType.MONTH,
                oneTime = false,
                type = TransactionType.EXPENSE,
                accountId = bank.id,
                amount = 25000.0,
                title = "Annual Insurance"
            )
        )

        val projection = useCase(period, safetyBuffer = 0.0)
        projection.startingCash shouldBe 15000.0
        projection.minimumBalance shouldBe -10000.0
        projection.hasShortfall shouldBe true
        projection.shortfallDate shouldBe LocalDate.of(2026, 11, 3)
    }
}

