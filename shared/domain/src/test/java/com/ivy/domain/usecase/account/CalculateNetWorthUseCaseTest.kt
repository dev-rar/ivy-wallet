package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakeInvestmentValuationDao
import com.ivy.data.db.dao.fake.FakeLoanDao
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.db.entity.LoanEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.model.LoanType
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonZeroDouble
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.InvestmentRepository
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class CalculateNetWorthUseCaseTest {
    private lateinit var accountDao: FakeAccountDao
    private lateinit var loanDao: FakeLoanDao
    private lateinit var fakeValuationDao: FakeInvestmentValuationDao
    private lateinit var investmentRepository: InvestmentRepository
    private lateinit var currencyRepository: CurrencyRepository
    private lateinit var accountBalanceUseCase: AccountBalanceUseCase
    private lateinit var useCase: CalculateNetWorthUseCase

    @Before
    fun setup() {
        accountDao = FakeAccountDao()
        loanDao = FakeLoanDao()
        fakeValuationDao = FakeInvestmentValuationDao()

        val dispatchersProvider = object : DispatchersProvider {
            override val main = Dispatchers.Unconfined
            override val default = Dispatchers.Unconfined
            override val io = Dispatchers.Unconfined
        }

        investmentRepository = InvestmentRepository(
            valuationDao = fakeValuationDao,
            writeValuationDao = fakeValuationDao,
            dispatchersProvider = dispatchersProvider
        )

        currencyRepository = mockk {
            coEvery { getBaseCurrency() } returns AssetCode.from("PKR").getOrNull()!!
        }

        accountBalanceUseCase = mockk()

        useCase = CalculateNetWorthUseCase(
            accountDao = accountDao,
            loanDao = loanDao,
            accountBalanceUseCase = accountBalanceUseCase,
            investmentRepository = investmentRepository,
            currencyRepository = currencyRepository,
            dispatchersProvider = dispatchersProvider
        )
    }

    @Test
    fun `calculates net worth with liquid, investment, and debt accounts`() = runTest {
        val cashAccId = UUID.randomUUID()
        val bankAccId = UUID.randomUUID()
        val investAccId = UUID.randomUUID()
        val creditAccId = UUID.randomUUID()

        accountDao.save(AccountEntity(name = "Wallet", currency = "PKR", color = 0, accountType = "CASH", id = cashAccId))
        accountDao.save(AccountEntity(name = "Meezan Bank", currency = "PKR", color = 0, accountType = "BANK", id = bankAccId))
        accountDao.save(AccountEntity(name = "Mutual Funds", currency = "PKR", color = 0, accountType = "INVESTMENT", id = investAccId))
        accountDao.save(AccountEntity(name = "Credit Card", currency = "PKR", color = 0, accountType = "LIABILITY", id = creditAccId))

        coEvery { accountBalanceUseCase.calculate(AccountId(cashAccId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(10000.0).getOrNull()!!
        )
        coEvery { accountBalanceUseCase.calculate(AccountId(bankAccId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(50000.0).getOrNull()!!
        )
        // Investment ledger shows initial cost of 200,000
        coEvery { accountBalanceUseCase.calculate(AccountId(investAccId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(200000.0).getOrNull()!!
        )
        coEvery { accountBalanceUseCase.calculate(AccountId(creditAccId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(-20000.0).getOrNull()!!
        )

        // Record a manual valuation update for mutual funds growing to 250,000
        investmentRepository.recordValuation(
            accountId = AccountId(investAccId),
            amount = 250000.0,
            date = Instant.now(),
            notes = "Q4 NAV statement"
        )

        // Also add an outstanding personal loan of 30,000
        loanDao.save(
            LoanEntity(
                name = "Car Loan",
                amount = 30000.0,
                type = LoanType.BORROW,
                color = 0,
                orderNum = 0.0,
                isSynced = false,
                isDeleted = false
            )
        )

        val summary = useCase()

        summary.liquidAssets shouldBe 60000.0 // 10,000 + 50,000
        summary.investmentAssets shouldBe 250000.0 // updated valuation
        summary.totalAssets shouldBe 310000.0 // 60,000 + 250,000
        summary.totalLiabilities shouldBe 50000.0 // 20,000 (credit card) + 30,000 (loan)
        summary.netWorth shouldBe 260000.0 // 310,000 - 50,000
    }
}

