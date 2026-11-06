package com.ivy.domain.usecase.budget

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.TransactionDao
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.model.budget.SinkingFund
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.data.repository.SinkingFundRepository
import com.ivy.domain.model.budget.SinkingFundProgress
import com.ivy.domain.usecase.account.AccountBalanceUseCase
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

class GetSinkingFundProgressUseCase @Inject constructor(
    private val sinkingFundRepository: SinkingFundRepository,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val accountBalanceUseCase: AccountBalanceUseCase,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(budgetItemId: UUID, period: BudgetPeriod): SinkingFundProgress? =
        withContext(dispatchersProvider.io) {
            val fund = sinkingFundRepository.getSinkingFund(budgetItemId) ?: return@withContext null
            val totalLiquidCash = calculateTotalLiquidCash()
            calculateProgress(fund, period, totalLiquidCash)
        }

    suspend fun getAll(period: BudgetPeriod): List<SinkingFundProgress> =
        withContext(dispatchersProvider.io) {
            val funds = sinkingFundRepository.getAllSinkingFunds()
            val totalLiquidCash = calculateTotalLiquidCash()
            funds.map { fund ->
                calculateProgress(fund, period, totalLiquidCash)
            }
        }

    private suspend fun calculateProgress(
        fund: SinkingFund,
        period: BudgetPeriod,
        totalLiquidCash: Double
    ): SinkingFundProgress {
        val items = monthlyBudgetRepository.findItems(period)
        val currentItem = items.find { it.id == fund.budgetItemId }

        var currentSaved = fund.openingBalance + (currentItem?.plannedAmount ?: 0.0)

        if (currentItem?.categoryId != null) {
            val transactions = transactionDao.findAllBetween(period.startInstant(), period.endInstant())
            val spent = transactions
                .filter { it.categoryId == currentItem.categoryId && it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            currentSaved -= spent
        }
        currentSaved = maxOf(0.0, currentSaved)

        val remainingAmount = maxOf(0.0, fund.targetAmount - currentSaved)
        val now = Instant.now()
        val targetDate = fund.targetDate

        val monthsRemaining: Int? = if (targetDate != null) {
            if (targetDate.isAfter(now)) {
                val zone = ZoneId.systemDefault()
                val nowZdt = now.atZone(zone)
                val targetZdt = targetDate.atZone(zone)
                val months = (targetZdt.year - nowZdt.year) * 12 + (targetZdt.monthValue - nowZdt.monthValue)
                maxOf(1, months)
            } else {
                0
            }
        } else {
            null
        }

        val monthlyContributionNeeded: Double? = when {
            targetDate == null -> null
            monthsRemaining == 0 -> remainingAmount
            monthsRemaining != null && monthsRemaining > 0 -> remainingAmount / monthsRemaining
            else -> null
        }

        val progressPercent = if (fund.targetAmount > 0.0) {
            (currentSaved / fund.targetAmount).toFloat().coerceIn(0f, 1f)
        } else {
            1.0f
        }

        val isSolvent = totalLiquidCash >= currentSaved
        val isFullyFunded = fund.targetAmount > 0.0 && currentSaved >= fund.targetAmount

        return SinkingFundProgress(
            sinkingFund = fund,
            budgetItemId = fund.budgetItemId,
            targetAmount = fund.targetAmount,
            currentSavedAmount = currentSaved,
            remainingAmount = remainingAmount,
            progressPercent = progressPercent,
            monthsRemaining = monthsRemaining,
            monthlyContributionNeeded = monthlyContributionNeeded,
            isSolvent = isSolvent,
            isFullyFunded = isFullyFunded
        )
    }

    private suspend fun calculateTotalLiquidCash(): Double {
        val accounts = accountDao.findAll().filter { !it.isDeleted }
        var total = 0.0
        for (acc in accounts) {
            val parsedType = runCatching { AccountType.valueOf(acc.accountType) }
                .getOrDefault(AccountType.BANK)
            if (parsedType == AccountType.BANK || parsedType == AccountType.CASH || parsedType == AccountType.REGULAR) {
                val balance = accountBalanceUseCase.calculate(AccountId(acc.id)).values.sumOf { it.value }
                if (balance > 0.0) {
                    total += balance
                }
            }
        }
        return total
    }
}

