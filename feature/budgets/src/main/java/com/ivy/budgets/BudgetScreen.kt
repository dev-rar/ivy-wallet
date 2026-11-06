package com.ivy.budgets

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ivy.budgets.model.BudgetTab
import com.ivy.budgets.model.DisplayBudget
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.budgets.monthly.CloseMonthModal
import com.ivy.budgets.monthly.EditBudgetItemModal
import com.ivy.budgets.monthly.EditIncomeModal
import com.ivy.budgets.monthly.ImportStatementModal
import com.ivy.budgets.monthly.MonthlyBudgetPlanView
import com.ivy.budgets.monthly.PlannedVsActualModal
import com.ivy.design.api.LocalTimeFormatter
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.statement.BankStatementProfile
import com.ivy.domain.model.statement.StatementTransaction
import java.util.UUID
import com.ivy.legacy.legacy.ui.theme.components.BudgetBattery
import com.ivy.legacy.utils.clickableNoIndication
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.rememberInteractionSource
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.navigation
import com.ivy.navigation.screenScopedViewModel
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.components.ReorderButton
import com.ivy.wallet.ui.theme.components.ReorderModalSingleType
import com.ivy.wallet.ui.theme.wallet.AmountCurrencyB1

@Composable
fun BoxWithConstraintsScope.BudgetScreen(screen: BudgetScreen) {
    val viewModel: BudgetViewModel = screenScopedViewModel()
    val uiState = viewModel.uiState()

    UI(
        state = uiState,
        onEvent = viewModel::onEvent,
        onParseStatement = viewModel::parseStatement
    )
}

@Composable
private fun BoxWithConstraintsScope.UI(
    state: BudgetScreenState,
    onEvent: (BudgetScreenEvent) -> Unit = {},
    onParseStatement: suspend (String, BankStatementProfile, UUID?) -> List<StatementTransaction> = { _, _, _ -> emptyList() }
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(32.dp))

        Toolbar(
            timeRange = state.timeRange,
            totalRemainingBudgetText = state.totalRemainingBudgetText,
            baseCurrency = state.baseCurrency,
            appBudgetMax = state.appBudgetMax,
            categoryBudgetsTotal = state.categoryBudgetsTotal,
            selectedTab = state.selectedTab,
            setReorderModalVisible = {
                onEvent(BudgetScreenEvent.OnReorderModalVisible(it))
            }
        )

        Spacer(Modifier.height(16.dp))

        // Tab Selector: Monthly Plan vs Classic
        TabSelector(
            selectedTab = state.selectedTab,
            onSelectTab = { onEvent(BudgetScreenEvent.OnSelectTab(it)) }
        )

        Spacer(Modifier.height(20.dp))

        if (state.selectedTab == BudgetTab.MONTHLY_PLAN) {
            MonthlyBudgetPlanView(
                plan = state.monthlyPlan,
                period = state.monthlyPeriod,
                baseCurrency = state.baseCurrency,
                cashFlowProjection = state.cashFlowProjection,
                onChangePeriod = { onEvent(BudgetScreenEvent.OnChangePeriod(it)) },
                onCopyPreviousMonth = { onEvent(BudgetScreenEvent.OnCopyPreviousMonth) },
                onEditIncome = { onEvent(BudgetScreenEvent.OnOpenEditIncomeModal(true)) },
                onReviewMonth = { onEvent(BudgetScreenEvent.OnOpenCloseMonthModal(true)) },
                onViewReport = { onEvent(BudgetScreenEvent.OnOpenPlannedVsActualModal(true)) },
                onImportStatement = { onEvent(BudgetScreenEvent.OnOpenImportStatementModal(true)) },
                onAddItem = { groupId ->
                    onEvent(
                        BudgetScreenEvent.OnOpenEditItemModal(
                            EditBudgetItemModalData(
                                groupId = groupId,
                                period = state.monthlyPeriod
                            )
                        )
                    )
                },
                onEditItem = { item ->
                    onEvent(
                        BudgetScreenEvent.OnOpenEditItemModal(
                            EditBudgetItemModalData(
                                itemId = item.id,
                                groupId = item.groupId,
                                period = state.monthlyPeriod,
                                initialName = item.name,
                                initialPlannedAmount = item.plannedAmount,
                                initialCategoryId = item.categoryId?.value,
                                initialItemType = item.itemType,
                                initialTargetAmount = item.sinkingFundProgress?.targetAmount ?: 0.0,
                                initialOpeningBalance = item.sinkingFundProgress?.sinkingFund?.openingBalance ?: 0.0,
                                initialNotes = item.sinkingFundProgress?.sinkingFund?.notes
                            )
                        )
                    )
                }
            )
        } else {
            // Classic Ivy Wallet Category Budgets
            for (item in state.budgets) {
                Spacer(Modifier.height(24.dp))

                BudgetItem(
                    displayBudget = item,
                    baseCurrency = state.baseCurrency
                ) {
                    onEvent(
                        BudgetScreenEvent.OnBudgetModalData(
                            BudgetModalData(
                                budget = item.budget,
                                baseCurrency = state.baseCurrency,
                                categories = state.categories,
                                accounts = state.accounts,
                                autoFocusKeyboard = false
                            )
                        )
                    )
                }
            }

            if (state.budgets.isEmpty()) {
                Spacer(Modifier.weight(1f))

                NoBudgetsEmptyState(
                    emptyStateTitle = stringResource(R.string.no_budgets),
                    emptyStateText = stringResource(R.string.no_budgets_text)
                )

                Spacer(Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(150.dp)) // scroll hack
    }

    val nav = navigation()
    BudgetBottomBar(
        onAdd = {
            if (state.selectedTab == BudgetTab.MONTHLY_PLAN) {
                val firstGroupId = state.monthlyPlan?.groups?.firstOrNull()?.id
                if (firstGroupId != null) {
                    onEvent(
                        BudgetScreenEvent.OnOpenEditItemModal(
                            EditBudgetItemModalData(
                                groupId = firstGroupId,
                                period = state.monthlyPeriod
                            )
                        )
                    )
                }
            } else {
                onEvent(
                    BudgetScreenEvent.OnBudgetModalData(
                        BudgetModalData(
                            budget = null,
                            baseCurrency = state.baseCurrency,
                            categories = state.categories,
                            accounts = state.accounts
                        )
                    )
                )
            }
        },
        onClose = {
            nav.back()
        },
    )

    ReorderModalSingleType(
        visible = state.reorderModalVisible,
        initialItems = state.budgets,
        dismiss = {
            onEvent(BudgetScreenEvent.OnReorderModalVisible(false))
        },
        onReordered = { onEvent(BudgetScreenEvent.OnReorder(it)) }
    ) { _, item ->
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
                .padding(vertical = 8.dp),
            text = item.budget.name,
            style = UI.typo.b1.style(
                color = UI.colors.pureInverse,
                fontWeight = FontWeight.Bold
            )
        )
    }

    BudgetModal(
        modal = state.budgetModalData,
        onCreate = { onEvent(BudgetScreenEvent.OnCreateBudget(it)) },
        onEdit = { onEvent(BudgetScreenEvent.OnEditBudget(it)) },
        onDelete = { onEvent(BudgetScreenEvent.OnDeleteBudget(it)) },
        dismiss = {
            onEvent(BudgetScreenEvent.OnBudgetModalData(null))
        }
    )

    EditBudgetItemModal(
        modal = state.editItemModalData,
        categories = state.categories,
        baseCurrency = state.baseCurrency,
        onSave = { itemId, groupId, period, name, amount, itemType, categoryId, targetAmount, openingBalance, notes ->
            onEvent(
                BudgetScreenEvent.OnSaveBudgetItem(
                    itemId = itemId,
                    groupId = groupId,
                    period = period,
                    name = name,
                    plannedAmount = amount,
                    itemType = itemType,
                    categoryId = categoryId,
                    targetAmount = targetAmount,
                    openingBalance = openingBalance,
                    notes = notes
                )
            )
        },
        onDelete = { itemId ->
            onEvent(BudgetScreenEvent.OnDeleteBudgetItem(itemId))
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnOpenEditItemModal(null))
        }
    )

    EditIncomeModal(
        visible = state.editIncomeModalVisible,
        initialIncome = state.monthlyPlan?.plannedIncome ?: 0.0,
        initialCarryover = state.monthlyPlan?.carryoverAmount ?: 0.0,
        baseCurrency = state.baseCurrency,
        onSave = { income, carryover ->
            onEvent(BudgetScreenEvent.OnEditPlannedIncome(income, carryover))
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnOpenEditIncomeModal(false))
        }
    )

    CloseMonthModal(
        visible = state.closeMonthModalVisible,
        summary = state.monthCloseSummary,
        baseCurrency = state.baseCurrency,
        onCloseMonth = { rollover ->
            onEvent(BudgetScreenEvent.OnCloseMonth(rollover))
        },
        onReopenMonth = {
            onEvent(BudgetScreenEvent.OnReopenMonth)
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnOpenCloseMonthModal(false))
        }
    )

    PlannedVsActualModal(
        visible = state.plannedVsActualModalVisible,
        report = state.plannedVsActualReport,
        baseCurrency = state.baseCurrency,
        onExportCsv = {
            onEvent(BudgetScreenEvent.OnExportBudgetPlanCsv(context))
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnOpenPlannedVsActualModal(false))
        }
    )

    ImportStatementModal(
        visible = state.importStatementModalVisible,
        accounts = state.accounts,
        period = state.monthlyPeriod,
        baseCurrency = state.baseCurrency,
        onParse = onParseStatement,
        onImport = { transactions, accountId, skipDuplicates ->
            onEvent(
                BudgetScreenEvent.OnImportStatementTransactions(
                    transactions = transactions,
                    accountId = accountId,
                    skipDuplicates = skipDuplicates
                )
            )
        },
        dismiss = {
            onEvent(BudgetScreenEvent.OnOpenImportStatementModal(false))
        }
    )
}

@Composable
private fun TabSelector(
    selectedTab: BudgetTab,
    onSelectTab: (BudgetTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(UI.colors.medium.copy(alpha = 0.08f))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selectedTab == BudgetTab.MONTHLY_PLAN) UI.colors.primary else Color.Transparent)
                .clickable { onSelectTab(BudgetTab.MONTHLY_PLAN) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Monthly Plan",
                style = UI.typo.b2.style(
                    color = if (selectedTab == BudgetTab.MONTHLY_PLAN) UI.colors.pure else UI.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (selectedTab == BudgetTab.CLASSIC) UI.colors.primary else Color.Transparent)
                .clickable { onSelectTab(BudgetTab.CLASSIC) }
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Classic",
                style = UI.typo.b2.style(
                    color = if (selectedTab == BudgetTab.CLASSIC) UI.colors.pure else UI.colors.pureInverse,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun Toolbar(
    timeRange: com.ivy.legacy.data.model.FromToTimeRange?,
    totalRemainingBudgetText: String?,
    baseCurrency: String,
    appBudgetMax: Double,
    categoryBudgetsTotal: Double,
    selectedTab: BudgetTab,
    setReorderModalVisible: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 24.dp, end = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.budgets),
                style = UI.typo.h2.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            if (selectedTab == BudgetTab.CLASSIC && timeRange != null) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = timeRange.toDisplay(LocalTimeFormatter.current),
                    style = UI.typo.b2.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (selectedTab == BudgetTab.CLASSIC && (categoryBudgetsTotal > 0 || appBudgetMax > 0)) {
                Spacer(Modifier.height(4.dp))

                val categoryBudgetText = if (categoryBudgetsTotal > 0) {
                    stringResource(
                        R.string.for_categories,
                        categoryBudgetsTotal.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val appBudgetMaxText = if (appBudgetMax > 0) {
                    stringResource(
                        R.string.app_budget,
                        appBudgetMax.format(baseCurrency),
                        baseCurrency
                    )
                } else {
                    ""
                }

                val hasBothBudgetTypes =
                    categoryBudgetText.isNotBlank() && appBudgetMaxText.isNotBlank()
                Text(
                    modifier = Modifier.testTag("budgets_info_text"),
                    text = if (hasBothBudgetTypes) {
                        stringResource(
                            R.string.budget_info_both,
                            categoryBudgetText,
                            appBudgetMaxText
                        )
                    } else {
                        stringResource(R.string.budget_info, categoryBudgetText, appBudgetMaxText)
                    },
                    style = UI.typo.nC.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                if (totalRemainingBudgetText != null) {
                    Text(
                        text = totalRemainingBudgetText,
                        style = UI.typo.nC.style(
                            color = Gray,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        if (selectedTab == BudgetTab.CLASSIC) {
            ReorderButton {
                setReorderModalVisible(true)
            }
            Spacer(Modifier.width(24.dp))
        }
    }
}

@SuppressLint("ComposeContentEmitterReturningValues", "ComposeMultipleContentEmitters")
@Composable
private fun BudgetItem(
    displayBudget: DisplayBudget,
    baseCurrency: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickableNoIndication(rememberInteractionSource()) {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = displayBudget.budget.name,
                style = UI.typo.b1.style(
                    color = UI.colors.pureInverse,
                    fontWeight = FontWeight.ExtraBold
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = determineBudgetType(displayBudget.budget.parseCategoryIds().size),
                style = UI.typo.c.style(
                    color = Gray
                )
            )
        }

        AmountCurrencyB1(
            amount = displayBudget.budget.amount,
            currency = baseCurrency,
            amountFontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.width(32.dp))
    }

    Spacer(Modifier.height(12.dp))

    BudgetBattery(
        modifier = Modifier.padding(horizontal = 16.dp),
        currency = baseCurrency,
        expenses = displayBudget.spentAmount,
        budget = displayBudget.budget.amount,
        backgroundNotFilled = UI.colors.medium
    ) {
        onClick()
    }
}


@Composable
private fun NoBudgetsEmptyState(
    emptyStateTitle: String,
    emptyStateText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        IvyIcon(
            icon = R.drawable.ic_budget_xl,
            tint = Gray
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = emptyStateTitle,
            style = UI.typo.b1.style(
                color = Gray,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = emptyStateText,
            style = UI.typo.b2.style(
                color = Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )

        Spacer(Modifier.height(96.dp))
    }
}
