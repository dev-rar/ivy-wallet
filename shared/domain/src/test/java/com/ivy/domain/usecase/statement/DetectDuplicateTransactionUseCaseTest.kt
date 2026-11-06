package com.ivy.domain.usecase.statement

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.fake.FakeTransactionDao
import com.ivy.data.db.entity.TransactionEntity
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class DetectDuplicateTransactionUseCaseTest {
    private lateinit var transactionDao: FakeTransactionDao
    private lateinit var useCase: DetectDuplicateTransactionUseCase

    private val dispatchers = object : DispatchersProvider {
        override val main = Dispatchers.Unconfined
        override val default = Dispatchers.Unconfined
        override val io = Dispatchers.Unconfined
    }

    @Before
    fun setup() {
        transactionDao = FakeTransactionDao()
        useCase = DetectDuplicateTransactionUseCase(
            transactionDao = transactionDao,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `detects duplicate transaction when account, amount, and date match within tolerance`() = runTest {
        val accountId = UUID.randomUUID()
        val now = Instant.now()

        val existing = TransactionEntity(
            id = UUID.randomUUID(),
            accountId = accountId,
            type = TransactionType.EXPENSE,
            amount = 1500.0,
            dateTime = now.minus(5, ChronoUnit.HOURS)
        )
        transactionDao.save(existing)

        val isDup = useCase.isDuplicate(
            accountId = accountId,
            amount = 1500.0,
            date = now
        )

        isDup shouldBe true
    }

    @Test
    fun `returns false when amount differs`() = runTest {
        val accountId = UUID.randomUUID()
        val now = Instant.now()

        val existing = TransactionEntity(
            id = UUID.randomUUID(),
            accountId = accountId,
            type = TransactionType.EXPENSE,
            amount = 1500.0,
            dateTime = now
        )
        transactionDao.save(existing)

        val isDup = useCase.isDuplicate(
            accountId = accountId,
            amount = 2500.0,
            date = now
        )

        isDup shouldBe false
    }
}

