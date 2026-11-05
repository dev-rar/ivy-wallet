package com.ivy.domain.usecase.transaction

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeTransactionSplitDao
import com.ivy.data.model.CategoryId
import com.ivy.data.model.TransactionId
import com.ivy.data.model.TransactionSplit
import com.ivy.data.model.TransactionSplitId
import com.ivy.data.repository.TransactionSplitRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.UUID

class SaveTransactionSplitsUseCaseTest {
    private lateinit var fakeDao: FakeTransactionSplitDao
    private lateinit var repository: TransactionSplitRepository
    private lateinit var useCase: SaveTransactionSplitsUseCase

    @Before
    fun setup() {
        fakeDao = FakeTransactionSplitDao()
        val dispatchersProvider = object : DispatchersProvider {
            override val main = Dispatchers.Unconfined
            override val default = Dispatchers.Unconfined
            override val io = Dispatchers.Unconfined
        }
        repository = TransactionSplitRepository(
            splitDao = fakeDao,
            writeSplitDao = fakeDao,
            dispatchersProvider = dispatchersProvider,
        )
        useCase = SaveTransactionSplitsUseCase(repository)
    }

    @Test
    fun `successful save when split sum matches parent amount`() = runTest {
        val trnId = TransactionId(UUID.randomUUID())
        val splits = listOf(
            TransactionSplit(
                id = TransactionSplitId(UUID.randomUUID()),
                transactionId = trnId,
                amount = 60.0,
                categoryId = CategoryId(UUID.randomUUID())
            ),
            TransactionSplit(
                id = TransactionSplitId(UUID.randomUUID()),
                transactionId = trnId,
                amount = 40.0,
                categoryId = CategoryId(UUID.randomUUID())
            )
        )

        val result = useCase(trnId, 100.0, splits)
        result.isSuccess shouldBe true

        val saved = repository.findByTransactionId(trnId)
        saved.size shouldBe 2
        saved.sumOf { it.amount } shouldBe 100.0
    }

    @Test
    fun `fails when split sum does not match parent amount`() = runTest {
        val trnId = TransactionId(UUID.randomUUID())
        val splits = listOf(
            TransactionSplit(
                id = TransactionSplitId(UUID.randomUUID()),
                transactionId = trnId,
                amount = 50.0
            )
        )

        val result = useCase(trnId, 100.0, splits)
        result.isFailure shouldBe true
    }

    @Test
    fun `fails when split amount is negative or zero`() = runTest {
        val trnId = TransactionId(UUID.randomUUID())
        val splits = listOf(
            TransactionSplit(
                id = TransactionSplitId(UUID.randomUUID()),
                transactionId = trnId,
                amount = -10.0
            )
        )

        val result = useCase(trnId, -10.0, splits)
        result.isFailure shouldBe true
    }

    @Test
    fun `clears splits when empty list provided`() = runTest {
        val trnId = TransactionId(UUID.randomUUID())
        val splits = listOf(
            TransactionSplit(
                id = TransactionSplitId(UUID.randomUUID()),
                transactionId = trnId,
                amount = 100.0
            )
        )
        useCase(trnId, 100.0, splits)
        repository.findByTransactionId(trnId).size shouldBe 1

        val clearResult = useCase(trnId, 100.0, emptyList())
        clearResult.isSuccess shouldBe true
        repository.findByTransactionId(trnId).size shouldBe 0
    }
}

