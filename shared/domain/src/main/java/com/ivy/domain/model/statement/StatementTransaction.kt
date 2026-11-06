package com.ivy.domain.model.statement

import com.ivy.base.model.TransactionType
import java.time.Instant
import java.util.UUID

data class StatementTransaction(
    val id: UUID = UUID.randomUUID(),
    val date: Instant,
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val isDuplicate: Boolean = false,
    val suggestedCategoryId: UUID? = null,
    val suggestedBudgetItemId: UUID? = null,
    val rawRow: Map<String, String> = emptyMap()
)

