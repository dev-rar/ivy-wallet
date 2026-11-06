package com.ivy.domain.model.budget

import java.time.Instant
import java.util.UUID

enum class BillStatus {
    SCHEDULED,
    DUE_SOON,
    OVERDUE,
    PAID
}

data class BudgetItemBillSummary(
    val ruleId: UUID,
    val budgetItemId: UUID,
    val title: String,
    val amount: Double,
    val dueDate: Instant?,
    val status: BillStatus
)

