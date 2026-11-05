package com.ivy.data.model

import com.ivy.data.model.sync.Identifiable
import com.ivy.data.model.sync.UniqueId
import java.time.Instant
import java.util.UUID

@JvmInline
value class InvestmentValuationId(override val value: UUID) : UniqueId

data class InvestmentValuation(
    override val id: InvestmentValuationId,
    val accountId: AccountId,
    val valuationDate: Instant,
    val valuationAmount: Double,
    val notes: String? = null,
) : Identifiable<InvestmentValuationId>

