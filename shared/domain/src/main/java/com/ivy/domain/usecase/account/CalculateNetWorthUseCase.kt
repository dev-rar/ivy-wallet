package com.ivy.domain.usecase.account

import com.ivy.base.threading.DispatchersProvider
import com.ivy.data.db.dao.read.AccountDao
import com.ivy.data.db.dao.read.LoanDao
import com.ivy.data.model.AccountId
import com.ivy.data.model.AccountType
import com.ivy.data.repository.CurrencyRepository
import com.ivy.data.repository.InvestmentRepository
import com.ivy.domain.model.networth.AccountBalanceItem
import com.ivy.domain.model.networth.NetWorthSummary
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

class CalculateNetWorthUseCase @Inject constructor(
    private val accountDao: AccountDao,
    private val loanDao: LoanDao,
    private val accountBalanceUseCase: AccountBalanceUseCase,
    private val investmentRepository: InvestmentRepository,
    private val currencyRepository: CurrencyRepository,
    private val dispatchersProvider: DispatchersProvider,
) {
    suspend operator fun invoke(): NetWorthSummary = withContext(dispatchersProvider.io) {
        val baseCurrency = currencyRepository.getBaseCurrency().code
        val accounts = accountDao.findAll().filter { !it.isDeleted }

        var liquidAssets = 0.0
        var investmentAssets = 0.0
        var accountLiabilities = 0.0
        val balanceItems = mutableListOf<AccountBalanceItem>()

        for (acc in accounts) {
            val accountId = AccountId(acc.id)
            val parsedType = runCatching { AccountType.valueOf(acc.accountType) }
                .getOrDefault(AccountType.BANK)

            val balances = accountBalanceUseCase.calculate(accountId)
            val ledgerBalance = balances.values.sumOf { it.value }

            val effectiveBalance = if (parsedType == AccountType.INVESTMENT) {
                val latestValuation = investmentRepository.getLatestValuation(accountId)
                latestValuation?.valuationAmount ?: ledgerBalance
            } else {
                ledgerBalance
            }

            balanceItems.add(
                AccountBalanceItem(
                    accountId = acc.id,
                    name = acc.name,
                    currency = acc.currency ?: baseCurrency,
                    balance = effectiveBalance,
                    accountType = parsedType,
                    color = acc.color,
                    icon = acc.icon
                )
            )

            when (parsedType) {
                AccountType.REGULAR, AccountType.CASH, AccountType.BANK -> {
                    if (effectiveBalance >= 0) {
                        liquidAssets += effectiveBalance
                    } else {
                        accountLiabilities += abs(effectiveBalance)
                    }
                }
                AccountType.INVESTMENT -> {
                    if (effectiveBalance >= 0) {
                        investmentAssets += effectiveBalance
                    } else {
                        accountLiabilities += abs(effectiveBalance)
                    }
                }
                AccountType.LIABILITY -> {
                    accountLiabilities += abs(effectiveBalance)
                }
            }
        }

        val loans = loanDao.findAll()
        val outstandingLoans = loans.filter { !it.isDeleted }.sumOf { it.amount }
        val totalLiabilities = accountLiabilities + outstandingLoans

        val totalAssets = liquidAssets + investmentAssets
        val netWorth = totalAssets - totalLiabilities

        NetWorthSummary(
            liquidAssets = liquidAssets,
            investmentAssets = investmentAssets,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            baseCurrency = baseCurrency,
            accounts = balanceItems
        )
    }
}

