package com.ivy.data.model

enum class AccountType {
    REGULAR,
    CASH,
    BANK,
    INVESTMENT,
    LIABILITY;

    val isLiquid: Boolean
        get() = this == REGULAR || this == CASH || this == BANK

    val isInvestment: Boolean
        get() = this == INVESTMENT

    val isLiability: Boolean
        get() = this == LIABILITY
}
