package com.ivy.domain.model.networth

import com.ivy.data.model.AccountType
import java.util.UUID

data class AccountBalanceItem(
    val accountId: UUID,
    val name: String,
    val currency: String,
    val balance: Double,
    val accountType: AccountType,
    val color: Int,
    val icon: String?,
)

data class NetWorthSummary(
    val liquidAssets: Double,
    val investmentAssets: Double,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val baseCurrency: String,
    val accounts: List<AccountBalanceItem> = emptyList(),
)

