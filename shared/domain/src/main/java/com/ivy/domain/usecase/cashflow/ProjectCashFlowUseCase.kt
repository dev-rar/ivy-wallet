package com.ivy.domain.usecase.cashflow

import com.ivy.base.model.TransactionType
import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.cashflow.CashFlowEvent
import com.ivy.domain.model.cashflow.CashFlowProjection
import com.ivy.domain.usecase.account.AccountBalanceUseCase
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ProjectCashFlowUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val accountBalanceUseCase: AccountBalanceUseCase,
    private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(
        period: BudgetPeriod = BudgetPeriod.current(),
        safetyBuffer: Double = 0.0
    ): CashFlowProjection = withContext(dispatchersProvider.io) {
        val startingCash = calculateLiquidCash()
        val yearMonth = period.toYearMonth()
        val zone = ZoneId.systemDefault()

        val rules = plannedPaymentRuleDao.findAll()
        val rawEvents = mutableListOf<Pair<LocalDate, Pair<String, Double>>>() // date, title, signed amount (+/-)

        for (rule in rules) {
            val startDate = rule.startDate ?: continue
            val startZdt = startDate.atZone(zone)
            val dayOfMonth = minOf(startZdt.dayOfMonth, yearMonth.lengthOfMonth())
            val eventDate = yearMonth.atDay(dayOfMonth)

            val isIncome = rule.type == TransactionType.INCOME
            val signedAmount = if (isIncome) rule.amount else -rule.amount
            val title = rule.title ?: if (isIncome) "Scheduled Income" else "Scheduled Bill"

            rawEvents.add(eventDate to (title to signedAmount))
        }

        // Sort events chronologically
        rawEvents.sortBy { it.first }

        var runningBalance = startingCash
        var minBalance = startingCash
        var minBalanceDate: LocalDate? = if (rawEvents.isNotEmpty()) yearMonth.atDay(1) else null
        var hasShortfall = startingCash < safetyBuffer
        var shortfallDate: LocalDate? = if (hasShortfall) yearMonth.atDay(1) else null

        val projectedEvents = mutableListOf<CashFlowEvent>()

        for ((date, details) in rawEvents) {
            val (title, signedAmount) = details
            val isIncome = signedAmount >= 0
            val amount = kotlin.math.abs(signedAmount)
            runningBalance += signedAmount

            if (runningBalance < minBalance) {
                minBalance = runningBalance
                minBalanceDate = date
            }

            if (runningBalance < safetyBuffer && !hasShortfall) {
                hasShortfall = true
                shortfallDate = date
            }

            projectedEvents.add(
                CashFlowEvent(
                    date = date,
                    title = title,
                    amount = amount,
                    isIncome = isIncome,
                    projectedBalance = runningBalance
                )
            )
        }

        CashFlowProjection(
            startingCash = startingCash,
            endingCash = runningBalance,
            minimumBalance = minBalance,
            minimumBalanceDate = minBalanceDate,
            hasShortfall = hasShortfall,
            shortfallDate = shortfallDate,
            safetyBuffer = safetyBuffer,
            events = projectedEvents
        )
    }

    private suspend fun calculateLiquidCash(): Double {
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

