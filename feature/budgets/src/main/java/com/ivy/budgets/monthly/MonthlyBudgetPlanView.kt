package com.ivy.budgets.monthly

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.domain.model.budget.MonthlyItemSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.model.cashflow.CashFlowProjection
import java.util.UUID

@Composable
fun MonthlyBudgetPlanView(
    plan: MonthlyPlanSummary?,
    period: BudgetPeriod,
    baseCurrency: String,
    cashFlowProjection: CashFlowProjection? = null,
    onChangePeriod: (BudgetPeriod) -> Unit,
    onCopyPreviousMonth: () -> Unit,
    onEditIncome: () -> Unit,
    onReviewMonth: () -> Unit = {},
    onAddItem: (groupId: UUID) -> Unit,
    onEditItem: (item: MonthlyItemSummary) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        val hasItems = plan?.groups?.any { it.items.isNotEmpty() } == true

        MonthlyPeriodHeader(
            period = period,
            onChangePeriod = onChangePeriod,
            onCopyPreviousMonth = onCopyPreviousMonth,
            hasItems = hasItems
        )

        Spacer(Modifier.height(16.dp))

        if (plan != null) {
            ZeroBasedSummaryBanner(
                plan = plan,
                baseCurrency = baseCurrency,
                onEditIncome = onEditIncome,
                onReviewMonth = onReviewMonth
            )

            if (cashFlowProjection != null) {
                Spacer(Modifier.height(16.dp))
                CashFlowProjectionCard(
                    projection = cashFlowProjection,
                    baseCurrency = baseCurrency,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            for (group in plan.groups) {
                BudgetGroupSection(
                    group = group,
                    baseCurrency = baseCurrency,
                    onAddItem = onAddItem,
                    onEditItem = onEditItem
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

