package com.ivy.accounts

import com.ivy.legacy.data.model.AccountData
import java.util.UUID

sealed interface AccountsEvent {
    data class OnReorder(val reorderedList: List<AccountData>) : AccountsEvent
    data class OnReorderModalVisible(val reorderVisible: Boolean) : AccountsEvent
    data class OnRecordValuation(
        val accountId: UUID,
        val amount: Double,
        val notes: String?
    ) : AccountsEvent
}
