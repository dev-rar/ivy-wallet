package com.ivy.domain.model.budget

import com.ivy.data.model.budget.SinkingFund
import java.util.UUID

data class SinkingFundProgress(
    val sinkingFund: SinkingFund,
    val budgetItemId: UUID,
    val targetAmount: Double,
    val currentSavedAmount: Double,
    val remainingAmount: Double,
    val progressPercent: Float,
    val monthsRemaining: Int?,
    val monthlyContributionNeeded: Double?,
    val isSolvent: Boolean,
    val isFullyFunded: Boolean
)

