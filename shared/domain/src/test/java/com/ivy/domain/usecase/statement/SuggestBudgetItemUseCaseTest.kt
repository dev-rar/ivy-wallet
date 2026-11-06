package com.ivy.domain.usecase.statement

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeMonthlyBudgetDao
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SuggestBudgetItemUseCaseTest {
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var monthlyBudgetDao: FakeMonthlyBudgetDao
    private lateinit var useCase: SuggestBudgetItemUseCase

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
        useCase = SuggestBudgetItemUseCase(
            transactionDao = transactionDao,
            monthlyBudgetDao = monthlyBudgetDao,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `suggests budget item based on direct name match in period`() = runTest {
        val categoryId = UUID.randomUUID()
        val budgetItem = BudgetItemEntity(
            id = UUID.randomUUID(),
            groupId = UUID.randomUUID(),
            planPeriod = period.value,
            name = "Groceries",
            plannedAmount = 30000.0,
            itemType = BudgetItemType.STANDARD.name,
            categoryId = categoryId
        )
        monthlyBudgetDao.saveItem(budgetItem)

        val suggestion = useCase.suggest("Imtiaz Supermarket Groceries Purchase", period)

        suggestion.budgetItemId shouldBe budgetItem.id
        suggestion.categoryId shouldBe categoryId
        suggestion.confidence shouldBeGreaterThan 0.9
    }
}

