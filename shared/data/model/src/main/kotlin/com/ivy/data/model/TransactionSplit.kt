package com.ivy.data.model

import com.ivy.data.model.sync.Identifiable
import com.ivy.data.model.sync.UniqueId
import java.util.UUID

@JvmInline
value class TransactionSplitId(override val value: UUID) : UniqueId

data class TransactionSplit(
    override val id: TransactionSplitId,
    val transactionId: TransactionId,
    val budgetItemId: UUID? = null,
    val categoryId: CategoryId? = null,
    val amount: Double,
    val note: String? = null,
    val orderNum: Double = 0.0,
) : Identifiable<TransactionSplitId>
