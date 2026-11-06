package com.ivy.data.model.budget

import java.time.Instant
import java.util.UUID

data class SinkingFund(
    val id: UUID = UUID.randomUUID(),
    val budgetItemId: UUID,
    val targetAmount: Double,
    val targetDate: Instant? = null,
    val openingBalance: Double = 0.0,
    val notes: String? = null
)

