package com.ivy.budgets

import com.ivy.budgets.model.BudgetTab
import com.ivy.budgets.model.DisplayBudget
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.data.model.Category
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.model.cashflow.CashFlowProjection
import com.ivy.legacy.data.model.FromToTimeRange
import com.ivy.legacy.datamodel.Account
import kotlinx.collections.immutable.ImmutableList
import javax.annotation.concurrent.Immutable

@Immutable
data class BudgetScreenState(
    val baseCurrency: String,
    val budgets: ImmutableList<DisplayBudget>,
    val categories: ImmutableList<Category>,
    val accounts: ImmutableList<Account>,
    val categoryBudgetsTotal: Double,
    val appBudgetMax: Double,
    val totalRemainingBudgetText: String?,
    val timeRange: FromToTimeRange?,
    val reorderModalVisible: Boolean,
    val budgetModalData: BudgetModalData?,
    val selectedTab: BudgetTab = BudgetTab.MONTHLY_PLAN,
    val monthlyPeriod: BudgetPeriod = BudgetPeriod.current(),
    val monthlyPlan: MonthlyPlanSummary? = null,
    val editItemModalData: EditBudgetItemModalData? = null,
    val editIncomeModalVisible: Boolean = false,
    val cashFlowProjection: CashFlowProjection? = null
)
