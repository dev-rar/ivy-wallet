package com.ivy.domain

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeAccountDao
import com.ivy.data.db.dao.fake.FakeInvestmentValuationDao
import com.ivy.data.db.dao.fake.FakeLoanDao
import com.ivy.data.db.dao.fake.FakeMonthlyBudgetDao
import com.ivy.data.db.dao.fake.FakeSinkingFundDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.dao.fake.FakeTransactionSplitDao
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.db.entity.AccountEntity
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.MonthlyBudgetPlanEntity
import com.ivy.data.db.entity.PlannedPaymentRuleEntity
import com.ivy.data.db.entity.SinkingFundEntity
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.model.CategoryId
import com.ivy.data.model.IntervalType
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.model.TransactionSplitId
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.model.primitive.AssetCode
import com.ivy.data.model.primitive.NonZeroDouble
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.InvestmentRepository
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.data.repository.SinkingFundRepository
import com.ivy.data.repository.TransactionSplitRepository
import com.ivy.domain.model.budget.MonthlyGroupSummary
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.model.statement.BankStatementProfile
import com.ivy.domain.usecase.account.AccountBalanceUseCase
import com.ivy.domain.usecase.account.CalculateNetWorthUseCase
import com.ivy.domain.usecase.budget.CloseMonthPlanUseCase
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import com.ivy.domain.usecase.budget.GetSinkingFundProgressUseCase
import com.ivy.domain.usecase.cashflow.ProjectCashFlowUseCase
import com.ivy.domain.usecase.csv.ExportBudgetPlanCsvUseCase
import com.ivy.domain.usecase.report.GetPlannedVsActualReportUseCase
import com.ivy.domain.usecase.statement.DetectDuplicateTransactionUseCase
import com.ivy.domain.usecase.statement.ImportStatementTransactionsUseCase
import com.ivy.domain.usecase.statement.ParseBankStatementUseCase
import com.ivy.domain.usecase.statement.SuggestBudgetItemUseCase
import com.ivy.domain.usecase.transaction.SaveTransactionSplitsUseCase
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class DailyUseWorkflowVerificationTest {

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    private lateinit var fakeMonthlyBudgetDao: FakeMonthlyBudgetDao
    private lateinit var fakeTransactionDao: FakeTransactionDao
    private lateinit var fakeTransactionSplitDao: FakeTransactionSplitDao
    private lateinit var fakeAccountDao: FakeAccountDao
    private lateinit var fakeLoanDao: FakeLoanDao
    private lateinit var fakeValuationDao: FakeInvestmentValuationDao
    private lateinit var fakeSinkingFundDao: FakeSinkingFundDao

    private lateinit var monthlyBudgetRepository: MonthlyBudgetRepository
    private lateinit var splitRepository: TransactionSplitRepository
    private lateinit var investmentRepository: InvestmentRepository
    private lateinit var sinkingFundRepository: SinkingFundRepository

    private val period = BudgetPeriod.of(2026, 11)

    @Before
    fun setup() {
        fakeMonthlyBudgetDao = FakeMonthlyBudgetDao()
        fakeTransactionDao = FakeTransactionDao()
        fakeTransactionSplitDao = FakeTransactionSplitDao()
        fakeAccountDao = FakeAccountDao()
        fakeLoanDao = FakeLoanDao()
        fakeValuationDao = FakeInvestmentValuationDao()
        fakeSinkingFundDao = FakeSinkingFundDao()

        monthlyBudgetRepository = MonthlyBudgetRepository(
            monthlyBudgetDao = fakeMonthlyBudgetDao,
            writeMonthlyBudgetDao = fakeMonthlyBudgetDao,
            dispatchersProvider = dispatchers
        )
        splitRepository = TransactionSplitRepository(
            splitDao = fakeTransactionSplitDao,
            writeSplitDao = fakeTransactionSplitDao,
            dispatchersProvider = dispatchers
        )
        investmentRepository = InvestmentRepository(
            valuationDao = fakeValuationDao,
            writeValuationDao = fakeValuationDao,
            dispatchersProvider = dispatchers
        )
        sinkingFundRepository = SinkingFundRepository(
            sinkingFundDao = fakeSinkingFundDao,
            writeSinkingFundDao = fakeSinkingFundDao,
            dispatchersProvider = dispatchers
        )
    }

    @Test
    fun `complete zero-based budgeting 12-step user lifecycle executes seamlessly`() = runTest {
        // STEP 1: Plan Creation with Planned Income
        val plan = MonthlyBudgetPlanEntity(
            id = UUID.randomUUID(),
            period = period.value,
            plannedIncome = 250000.0,
            carryoverAmount = 0.0,
            isClosed = false,
            createdAt = System.currentTimeMillis()
        )
        fakeMonthlyBudgetDao.savePlan(plan)

        // STEP 2: Creating Budget Groups (Needs, Savings)
        val needsGroup = BudgetGroupEntity(
            id = UUID.randomUUID(),
            planPeriod = period.value,
            name = "Needs",
            groupType = "CUSTOM",
            orderNum = 0.0
        )
        val savingsGroup = BudgetGroupEntity(
            id = UUID.randomUUID(),
            planPeriod = period.value,
            name = "Savings & Investments",
            groupType = "SAVINGS",
            orderNum = 1.0
        )
        fakeMonthlyBudgetDao.saveGroup(needsGroup)
        fakeMonthlyBudgetDao.saveGroup(savingsGroup)

        // STEP 3: Assigning Budget Items to achieve Zero-Based Budget (Left to Assign = 0)
        val rentCatId = UUID.randomUUID()
        val groceriesCatId = UUID.randomUUID()
        val carFundCatId = UUID.randomUUID()

        val rentItem = BudgetItemEntity(
            id = UUID.randomUUID(),
            groupId = needsGroup.id,
            planPeriod = period.value,
            name = "Rent",
            plannedAmount = 100000.0,
            itemType = "STANDARD",
            categoryId = rentCatId,
            orderNum = 0.0
        )
        val groceriesItem = BudgetItemEntity(
            id = UUID.randomUUID(),
            groupId = needsGroup.id,
            planPeriod = period.value,
            name = "Groceries",
            plannedAmount = 70000.0,
            itemType = "STANDARD",
            categoryId = groceriesCatId,
            orderNum = 1.0
        )
        val carFundItem = BudgetItemEntity(
            id = UUID.randomUUID(),
            groupId = savingsGroup.id,
            planPeriod = period.value,
            name = "Emergency Car Fund",
            plannedAmount = 80000.0,
            itemType = "SINKING_FUND",
            categoryId = carFundCatId,
            orderNum = 0.0
        )
        fakeMonthlyBudgetDao.saveItem(rentItem)
        fakeMonthlyBudgetDao.saveItem(groceriesItem)
        fakeMonthlyBudgetDao.saveItem(carFundItem)

        // Verify Zero-Based Math: 250,000 - (100k + 70k + 80k) = 0 Left to Assign
        val totalAllocated = rentItem.plannedAmount + groceriesItem.plannedAmount + carFundItem.plannedAmount
        val leftToAssign = plan.plannedIncome + plan.carryoverAmount - totalAllocated
        leftToAssign shouldBe 0.0

        // STEP 4: Sinking Fund Setup and Progress Calculation
        fakeSinkingFundDao.save(
            SinkingFundEntity(
                id = UUID.randomUUID(),
                budgetItemId = carFundItem.id,
                targetAmount = 400000.0,
                targetDate = Instant.parse("2027-03-31T00:00:00Z"),
                openingBalance = 160000.0,
                notes = "Annual Car Maintenance & Emergency"
            )
        )
        val accountBalanceUseCaseMock = mockk<AccountBalanceUseCase>()
        val sinkingFundUseCase = GetSinkingFundProgressUseCase(
            sinkingFundRepository = sinkingFundRepository,
            monthlyBudgetRepository = monthlyBudgetRepository,
            transactionDao = fakeTransactionDao,
            accountDao = fakeAccountDao,
            accountBalanceUseCase = accountBalanceUseCaseMock,
            dispatchersProvider = dispatchers
        )
        val fundProgress = sinkingFundUseCase(carFundItem.id, period)
        fundProgress shouldNotBe null
        fundProgress!!.currentSavedAmount shouldBe (240000.0 plusOrMinus 0.001) // 160k + 80k planned
        fundProgress.isFullyFunded shouldBe false

        // STEP 5: Split Transaction Validation
        val saveSplitsUseCase = SaveTransactionSplitsUseCase(splitRepository)
        val parentTxId = UUID.randomUUID()
        val split1 = TransactionSplit(
            id = TransactionSplitId(UUID.randomUUID()),
            transactionId = TransactionId(parentTxId),
            budgetItemId = groceriesItem.id,
            categoryId = CategoryId(groceriesCatId),
            amount = 45000.0
        )
        val split2 = TransactionSplit(
            id = TransactionSplitId(UUID.randomUUID()),
            transactionId = TransactionId(parentTxId),
            budgetItemId = rentItem.id,
            categoryId = CategoryId(rentCatId),
            amount = 15000.0
        )
        val saveResult = saveSplitsUseCase(
            transactionId = TransactionId(parentTxId),
            parentAmount = 60000.0,
            splits = listOf(split1, split2)
        )
        saveResult.isSuccess shouldBe true
        splitRepository.findByTransactionId(TransactionId(parentTxId)) shouldHaveSize 2

        // STEP 6: Net Worth Calculation Across Multi-Account Types (Liquid, Investment, Liability)
        val bankAccountId = UUID.randomUUID()
        val investAccountId = UUID.randomUUID()
        val creditAccountId = UUID.randomUUID()

        fakeAccountDao.save(AccountEntity(name = "Meezan Current", currency = "PKR", color = 0, accountType = "BANK", id = bankAccountId))
        fakeAccountDao.save(AccountEntity(name = "Al-Meezan Mutual Fund", currency = "PKR", color = 0, accountType = "INVESTMENT", id = investAccountId))
        fakeAccountDao.save(AccountEntity(name = "Credit Card", currency = "PKR", color = 0, accountType = "LIABILITY", id = creditAccountId))

        val currencyRepositoryMock = mockk<CurrencyRepository> {
            coEvery { getBaseCurrency() } returns AssetCode.from("PKR").getOrNull()!!
        }
        coEvery { accountBalanceUseCaseMock.calculate(AccountId(bankAccountId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(200000.0).getOrNull()!!
        )
        coEvery { accountBalanceUseCaseMock.calculate(AccountId(investAccountId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(500000.0).getOrNull()!!
        )
        coEvery { accountBalanceUseCaseMock.calculate(AccountId(creditAccountId)) } returns mapOf(
            AssetCode.from("PKR").getOrNull()!! to NonZeroDouble.from(150000.0).getOrNull()!!
        )

        val netWorthUseCase = CalculateNetWorthUseCase(
            accountDao = fakeAccountDao,
            loanDao = fakeLoanDao,
            accountBalanceUseCase = accountBalanceUseCaseMock,
            investmentRepository = investmentRepository,
            currencyRepository = currencyRepositoryMock,
            dispatchersProvider = dispatchers
        )
        val netWorthSummary = netWorthUseCase()
        // Liquid: 200,000; Investments: 500,000; Liabilities: 150,000 -> Net Worth = 550,000
        netWorthSummary.liquidAssets shouldBe (200000.0 plusOrMinus 0.001)
        netWorthSummary.investmentAssets shouldBe (500000.0 plusOrMinus 0.001)
        netWorthSummary.totalLiabilities shouldBe (150000.0 plusOrMinus 0.001)
        netWorthSummary.netWorth shouldBe (550000.0 plusOrMinus 0.001)

        // STEP 7: Cash Flow & Paycheck Forecasting
        val plannedPaymentRuleDaoMock = mockk<PlannedPaymentRuleDao>()
        coEvery { plannedPaymentRuleDaoMock.findAll() } returns listOf(
            PlannedPaymentRuleEntity(
                id = UUID.randomUUID(),
                accountId = bankAccountId,
                type = TransactionType.INCOME,
                amount = 250000.0,
                title = "Monthly Salary Inflow",
                startDate = Instant.parse("2026-11-01T10:00:00Z"),
                intervalN = 1,
                intervalType = IntervalType.MONTH,
                oneTime = false
            ),
            PlannedPaymentRuleEntity(
                id = UUID.randomUUID(),
                accountId = bankAccountId,
                type = TransactionType.EXPENSE,
                amount = 100000.0,
                title = "Apartment Rent",
                startDate = Instant.parse("2026-11-05T10:00:00Z"),
                intervalN = 1,
                intervalType = IntervalType.MONTH,
                oneTime = false
            )
        )

        val projectCashFlowUseCase = ProjectCashFlowUseCase(
            accountDao = fakeAccountDao,
            accountBalanceUseCase = accountBalanceUseCaseMock,
            plannedPaymentRuleDao = plannedPaymentRuleDaoMock,
            dispatchersProvider = dispatchers
        )
        val projection = projectCashFlowUseCase(period = period, safetyBuffer = 10000.0)
        projection.startingCash shouldBe 200000.0
        projection.endingCash shouldBe (200000.0 + 250000.0 - 100000.0) // 350,000
        projection.hasShortfall shouldBe false

        // STEP 8: Statement Import, Duplicate Detection, and Smart Matching
        val duplicateDetector = DetectDuplicateTransactionUseCase(fakeTransactionDao, dispatchers)
        val budgetSuggester = SuggestBudgetItemUseCase(fakeTransactionDao, fakeMonthlyBudgetDao, dispatchers)
        val parseStatementUseCase = ParseBankStatementUseCase(duplicateDetector, budgetSuggester, dispatchers)

        // Seed an existing transaction in Room DB to test duplicate detection
        val existingTx = TransactionEntity(
            id = UUID.randomUUID(),
            accountId = bankAccountId,
            amount = 100000.0,
            type = TransactionType.EXPENSE,
            title = "House Rent Meezan Transfer",
            dateTime = Instant.parse("2026-11-05T10:00:00Z"),
            categoryId = rentCatId
        )
        fakeTransactionDao.save(existingTx)

        val csvStatement = """
            Date,Description,Amount,Type
            05/11/2026,House Rent Meezan Transfer,100000,Debit
            10/11/2026,Carrefour Grocery Mart,18500,Debit
        """.trimIndent()

        val parsedTxs = parseStatementUseCase.parse(
            csvText = csvStatement,
            accountId = bankAccountId,
            period = period,
            forcedProfile = BankStatementProfile.GENERIC
        )

        parsedTxs shouldHaveSize 2
        parsedTxs[0].isDuplicate shouldBe true
        parsedTxs[1].isDuplicate shouldBe false

        // Import non-duplicates
        val importUseCase = ImportStatementTransactionsUseCase(fakeTransactionDao, splitRepository, dispatchers)
        val importedCount = importUseCase.import(
            transactions = parsedTxs,
            accountId = bankAccountId,
            skipDuplicates = true
        )
        importedCount shouldBe 1

        // STEP 9: Planned vs Actual Report Calculation
        val getPlanSummaryMock = mockk<GetMonthlyPlanSummaryUseCase>()
        val planSummary = MonthlyPlanSummary(
            period = period,
            plannedIncome = 250000.0,
            carryoverAmount = 0.0,
            totalIncome = 250000.0,
            totalAllocated = 250000.0,
            leftToAssign = 0.0,
            groups = listOf(
                MonthlyGroupSummary(
                    id = UUID.randomUUID(),
                    name = "Income",
                    groupType = BudgetGroupType.INCOME,
                    orderNum = 0.0,
                    plannedTotal = 250000.0,
                    actualTotal = 250000.0,
                    items = emptyList()
                ),
                MonthlyGroupSummary(
                    id = needsGroup.id,
                    name = "Needs",
                    groupType = BudgetGroupType.CUSTOM,
                    orderNum = 1.0,
                    plannedTotal = 170000.0,
                    actualTotal = 150000.0,
                    items = listOf(
                        MonthlyItemSummary(
                            id = rentItem.id,
                            groupId = needsGroup.id,
                            name = "Rent",
                            plannedAmount = 100000.0,
                            actualAmount = 100000.0,
                            itemType = BudgetItemType.STANDARD,
                            categoryId = CategoryId(rentCatId)
                        ),
                        MonthlyItemSummary(
                            id = groceriesItem.id,
                            groupId = needsGroup.id,
                            name = "Groceries",
                            plannedAmount = 70000.0,
                            actualAmount = 50000.0,
                            itemType = BudgetItemType.STANDARD,
                            categoryId = CategoryId(groceriesCatId)
                        )
                    )
                )
            )
        )
        coEvery { getPlanSummaryMock(period) } returns planSummary

        val plannedVsActualUseCase = GetPlannedVsActualReportUseCase(getPlanSummaryMock, dispatchers)
        val report = plannedVsActualUseCase(period)
        report.netActual shouldBe 100000.0 // 250k income - 150k expenses = 100k positive surplus
        report.groups.flatMap { it.items }.any { it.isOverspent } shouldBe false

        // STEP 10: Export Budget Plan to CSV
        val exportCsvUseCase = ExportBudgetPlanCsvUseCase(getPlanSummaryMock, dispatchers)
        val csvResult = exportCsvUseCase.exportCsv(period)
        csvResult shouldNotBe null
        csvResult.contains("Rent") shouldBe true
        csvResult.contains("Groceries") shouldBe true

        // STEP 11: Month Close Wizard & Surplus Rollover
        val closeMonthUseCase = CloseMonthPlanUseCase(monthlyBudgetRepository, getPlanSummaryMock, dispatchers)
        val closeSummary = closeMonthUseCase.getSummary(period)
        closeSummary.netSurplus shouldBe 100000.0
        closeSummary.isClosed shouldBe false

        // Close month and carry over 100,000 surplus to December 2026
        closeMonthUseCase.closeMonth(
            period = period,
            rolloverSurplus = true
        )

        // Verify November is marked closed
        val closedPlan = fakeMonthlyBudgetDao.findPlanByPeriod(period.value)
        closedPlan?.isClosed shouldBe true

        // Verify December 2026 has the carryover surplus populated
        val nextMonthPlan = fakeMonthlyBudgetDao.findPlanByPeriod(period.next().value)
        nextMonthPlan shouldNotBe null
        nextMonthPlan!!.carryoverAmount shouldBe (100000.0 plusOrMinus 0.001)

        // STEP 12: Verify Idempotent Reopen
        closeMonthUseCase.reopenMonth(period)
        val reopenedPlan = fakeMonthlyBudgetDao.findPlanByPeriod(period.value)
        reopenedPlan?.isClosed shouldBe false
    }
}
