package com.ivy.domain.usecase.statement

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.dao.fake.FakeTransactionSplitDao
import com.ivy.data.model.TransactionId
import com.ivy.data.repository.TransactionSplitRepository
import com.ivy.domain.model.statement.StatementTransaction
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

class ImportStatementTransactionsUseCaseTest {
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var splitDao: FakeTransactionSplitDao
    private lateinit var splitRepository: TransactionSplitRepository
    private lateinit var useCase: ImportStatementTransactionsUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    @Before
    fun setup() {
        transactionDao = FakeTransactionDao()
        splitDao = FakeTransactionSplitDao()
        splitRepository = TransactionSplitRepository(
            splitDao = splitDao,
            writeSplitDao = splitDao,
            dispatchersProvider = dispatchers
        )
        useCase = ImportStatementTransactionsUseCase(
            writeTransactionDao = transactionDao,
            splitRepository = splitRepository,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `imports non-duplicate transactions and skips duplicates`() = runTest {
        val accountId = UUID.randomUUID()
        val budgetItemId = UUID.randomUUID()

        val trn1 = StatementTransaction(
            id = UUID.randomUUID(),
            date = Instant.now(),
            description = "Electricity Bill",
            amount = 5500.0,
            type = TransactionType.EXPENSE,
            isDuplicate = false,
            suggestedBudgetItemId = budgetItemId
        )

        val trn2 = StatementTransaction(
            id = UUID.randomUUID(),
            date = Instant.now(),
            description = "Coffee",
            amount = 500.0,
            type = TransactionType.EXPENSE,
            isDuplicate = true
        )

        val count = useCase.import(
            transactions = listOf(trn1, trn2),
            accountId = accountId,
            skipDuplicates = true
        )

        count shouldBe 1
        val savedTrn = transactionDao.findById(trn1.id)
        savedTrn shouldBe transactionDao.findAll().first()
        savedTrn?.amount shouldBe 5500.0

        val splits = splitRepository.findByTransactionId(TransactionId(trn1.id))
        splits.size shouldBe 1
        splits[0].budgetItemId shouldBe budgetItemId
    }
}

