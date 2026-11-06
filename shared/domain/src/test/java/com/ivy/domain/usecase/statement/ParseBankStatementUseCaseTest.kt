package com.ivy.domain.usecase.statement

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeMonthlyBudgetDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.db.entity.TransactionEntity
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.statement.BankStatementProfile
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ParseBankStatementUseCaseTest {
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var monthlyBudgetDao: FakeMonthlyBudgetDao
    private lateinit var detectDuplicateTransactionUseCase: DetectDuplicateTransactionUseCase
    private lateinit var suggestBudgetItemUseCase: SuggestBudgetItemUseCase
    private lateinit var useCase: ParseBankStatementUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    private val period = BudgetPeriod.of(2026, 11)

    @Before
    fun setup() {
        transactionDao = FakeTransactionDao()
        monthlyBudgetDao = FakeMonthlyBudgetDao()

        detectDuplicateTransactionUseCase = DetectDuplicateTransactionUseCase(
            transactionDao = transactionDao,
            dispatchers = dispatchers
        )

        suggestBudgetItemUseCase = SuggestBudgetItemUseCase(
            transactionDao = transactionDao,
            monthlyBudgetDao = monthlyBudgetDao,
            dispatchers = dispatchers
        )

        useCase = ParseBankStatementUseCase(
            detectDuplicateTransactionUseCase = detectDuplicateTransactionUseCase,
            suggestBudgetItemUseCase = suggestBudgetItemUseCase,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `parses Meezan Bank statement with debit and credit columns`() = runTest {
        val csv = """
            Post Date,Description,Instrument No,Debit,Credit,Balance
            01/11/2026,K-Electric Bill Payment,,4500.00,,95500.00
            05/11/2026,Salary Transfer,,,150000.00,245500.00
        """.trimIndent()

        val results = useCase.parse(csv, period = period)

        results.size shouldBe 2

        val expense = results[0]
        expense.description shouldBe "K-Electric Bill Payment"
        expense.amount shouldBe 4500.0
        expense.type shouldBe TransactionType.EXPENSE
        expense.isDuplicate shouldBe false

        val income = results[1]
        income.description shouldBe "Salary Transfer"
        income.amount shouldBe 150000.0
        income.type shouldBe TransactionType.INCOME
    }

    @Test
    fun `parses SadaPay statement correctly and detects duplicates and suggestions`() = runTest {
        val accountId = UUID.randomUUID()
        val catId = UUID.randomUUID()
        val budgetItemId = UUID.randomUUID()

        // Seed existing budget item for "Foodpanda"
        monthlyBudgetDao.saveItem(
            BudgetItemEntity(
                id = budgetItemId,
                groupId = UUID.randomUUID(),
                planPeriod = period.value,
                name = "Foodpanda",
                plannedAmount = 10000.0,
                itemType = BudgetItemType.STANDARD.name,
                categoryId = catId
            )
        )

        // Seed existing transaction to trigger duplicate detection
        transactionDao.save(
            TransactionEntity(
                id = UUID.randomUUID(),
                accountId = accountId,
                type = TransactionType.EXPENSE,
                amount = 1250.0,
                dateTime = Instant.parse("2026-11-03T10:00:00Z")
            )
        )

        val csv = """
            Date,Description,Amount (PKR),Status
            2026-11-03,Foodpanda,-1250.00,SUCCESS
        """.trimIndent()

        val results = useCase.parse(csv, accountId = accountId, period = period)

        results.size shouldBe 1
        val item = results[0]
        item.description shouldBe "Foodpanda"
        item.amount shouldBe 1250.0
        item.type shouldBe TransactionType.EXPENSE
        item.isDuplicate shouldBe true
        item.suggestedCategoryId shouldBe catId
        item.suggestedBudgetItemId shouldBe budgetItemId
    }

    @Test
    fun `auto detects HBL profile`() {
        val header = listOf("Transaction Date", "Narration", "Withdrawal", "Deposit", "Balance")
        val profile = BankStatementProfile.detectProfile(header)
        profile shouldBe BankStatementProfile.HBL
    }
}
