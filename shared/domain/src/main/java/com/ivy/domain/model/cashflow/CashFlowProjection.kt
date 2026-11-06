package com.ivy.domain.model.cashflow

import java.time.LocalDate

data class CashFlowEvent(
    val date: LocalDate,
    val title: String,
    val amount: Double,
    val isIncome: Boolean,
    val projectedBalance: Double
)

data class CashFlowProjection(
    val startingCash: Double,
    val endingCash: Double,
    val minimumBalance: Double,
    val minimumBalanceDate: LocalDate?,
    val hasShortfall: Boolean,
    val shortfallDate: LocalDate?,
    val safetyBuffer: Double = 0.0,
    val events: List<CashFlowEvent>
)

