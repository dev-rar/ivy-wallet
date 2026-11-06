package com.ivy.budgets

import com.ivy.budgets.model.BudgetTab
import com.ivy.budgets.model.DisplayBudget
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.legacy.datamodel.Budget
import com.ivy.wallet.domain.deprecated.logic.model.CreateBudgetData
import java.util.UUID

sealed interface BudgetScreenEvent {
    data class OnReorder(val newOrder: List<DisplayBudget>) : BudgetScreenEvent
    data class OnCreateBudget(val budgetData: CreateBudgetData) : BudgetScreenEvent
    data class OnEditBudget(val budget: Budget) : BudgetScreenEvent
    data class OnDeleteBudget(val budget: Budget) : BudgetScreenEvent
    data class OnReorderModalVisible(val visible: Boolean) : BudgetScreenEvent
    data class OnBudgetModalData(val budgetModalData: BudgetModalData?) : BudgetScreenEvent

    // Monthly zero-based budget events
    data class OnSelectTab(val tab: BudgetTab) : BudgetScreenEvent
    data class OnChangePeriod(val period: BudgetPeriod) : BudgetScreenEvent
    data object OnCopyPreviousMonth : BudgetScreenEvent
    data class OnEditPlannedIncome(val income: Double, val carryover: Double) : BudgetScreenEvent
    data class OnOpenEditIncomeModal(val visible: Boolean) : BudgetScreenEvent
    data class OnOpenEditItemModal(val data: EditBudgetItemModalData?) : BudgetScreenEvent
    data class OnSaveBudgetItem(
        val itemId: UUID?,
        val groupId: UUID,
        val period: BudgetPeriod,
        val name: String,
        val plannedAmount: Double,
        val itemType: BudgetItemType,
        val categoryId: UUID?,
        val targetAmount: Double = 0.0,
        val openingBalance: Double = 0.0,
        val notes: String? = null
    ) : BudgetScreenEvent
    data class OnDeleteBudgetItem(val itemId: UUID) : BudgetScreenEvent
    data class OnAddBudgetGroup(val period: BudgetPeriod, val name: String, val groupType: BudgetGroupType) : BudgetScreenEvent
    data class OnDeleteBudgetGroup(val groupId: UUID) : BudgetScreenEvent
    data class OnOpenCloseMonthModal(val visible: Boolean) : BudgetScreenEvent
    data class OnCloseMonth(val rolloverSurplus: Boolean) : BudgetScreenEvent
    data object OnReopenMonth : BudgetScreenEvent
}
