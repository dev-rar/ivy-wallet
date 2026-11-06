package com.ivy.budgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewModelScope
import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.budgets.model.BudgetTab
import com.ivy.budgets.model.DisplayBudget
import com.ivy.budgets.model.EditBudgetItemModalData
import com.ivy.data.db.dao.write.WriteBudgetDao
import com.ivy.data.db.entity.BudgetGroupEntity
import com.ivy.data.db.entity.BudgetItemEntity
import com.ivy.data.model.Category
import com.ivy.data.model.Expense
import com.ivy.data.model.Income
import com.ivy.data.model.Transaction
import com.ivy.data.model.Transfer
import com.ivy.data.model.budget.BudgetGroupType
import com.ivy.data.model.budget.BudgetItemType
import com.ivy.data.model.budget.BudgetPeriod
import com.ivy.data.model.budget.SinkingFund
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.MonthlyBudgetRepository
import com.ivy.data.repository.SinkingFundRepository
import com.ivy.data.temp.migration.getAccountId
import com.ivy.data.temp.migration.getValue
import com.ivy.domain.model.budget.MonthCloseSummary
import com.ivy.domain.model.budget.MonthlyPlanSummary
import com.ivy.domain.model.cashflow.CashFlowProjection
import com.ivy.domain.usecase.budget.CloseMonthPlanUseCase
import com.ivy.domain.usecase.budget.CopyPreviousMonthPlanUseCase
import com.ivy.domain.usecase.budget.GetMonthlyPlanSummaryUseCase
import com.ivy.domain.usecase.budget.SaveBudgetItemUseCase
import com.ivy.domain.usecase.cashflow.ProjectCashFlowUseCase
import com.ivy.frp.sumOfSuspend
import com.ivy.legacy.data.model.FromToTimeRange
import com.ivy.legacy.data.model.toCloseTimeRange
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.datamodel.Budget
import com.ivy.legacy.domain.deprecated.logic.BudgetCreator
import com.ivy.legacy.utils.format
import com.ivy.legacy.utils.isNotNullOrBlank
import com.ivy.ui.ComposeViewModel
import com.ivy.ui.R
import com.ivy.wallet.domain.action.account.AccountsAct
import com.ivy.wallet.domain.action.budget.BudgetsAct
import com.ivy.wallet.domain.action.exchange.ExchangeAct
import com.ivy.wallet.domain.action.settings.BaseCurrencyAct
import com.ivy.wallet.domain.action.transaction.HistoryTrnsAct
import com.ivy.wallet.domain.deprecated.logic.model.CreateBudgetData
import com.ivy.wallet.domain.pure.exchange.ExchangeData
import com.ivy.wallet.domain.pure.transaction.trnCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

@Stable
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val sharedPrefs: SharedPrefs,
    private val budgetWriter: WriteBudgetDao,
    private val budgetCreator: BudgetCreator,
    private val ivyContext: com.ivy.legacy.IvyWalletCtx,
    private val accountsAct: AccountsAct,
    private val categoryRepository: CategoryRepository,
    private val budgetsAct: BudgetsAct,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val historyTrnsAct: HistoryTrnsAct,
    private val exchangeAct: ExchangeAct,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
    private val getMonthlyPlanSummaryUseCase: GetMonthlyPlanSummaryUseCase,
    private val copyPreviousMonthPlanUseCase: CopyPreviousMonthPlanUseCase,
    private val saveBudgetItemUseCase: SaveBudgetItemUseCase,
    private val monthlyBudgetRepository: MonthlyBudgetRepository,
    private val sinkingFundRepository: SinkingFundRepository,
    private val projectCashFlowUseCase: ProjectCashFlowUseCase,
    private val closeMonthPlanUseCase: CloseMonthPlanUseCase,
) : ComposeViewModel<BudgetScreenState, BudgetScreenEvent>() {

    private val baseCurrency = mutableStateOf("")
    private val timeRange = mutableStateOf<FromToTimeRange?>(null)
    private val budgets = mutableStateOf<ImmutableList<DisplayBudget>>(persistentListOf())
    private val categories = mutableStateOf<ImmutableList<Category>>(persistentListOf())
    private val accounts = mutableStateOf<ImmutableList<Account>>(persistentListOf())
    private val categoryBudgetsTotal = mutableDoubleStateOf(0.0)
    private val appBudgetMax = mutableDoubleStateOf(0.0)
    private val totalRemainingBudget = mutableDoubleStateOf(0.0)
    private val reorderModalVisible = mutableStateOf(false)
    private val budgetModalData = mutableStateOf<BudgetModalData?>(null)

    // Monthly zero-based budget states
    private val selectedTab = mutableStateOf(BudgetTab.MONTHLY_PLAN)
    private val monthlyPeriod = mutableStateOf(BudgetPeriod.current())
    private val monthlyPlan = mutableStateOf<MonthlyPlanSummary?>(null)
    private val editItemModalData = mutableStateOf<EditBudgetItemModalData?>(null)
    private val editIncomeModalVisible = mutableStateOf(false)
    private val cashFlowProjection = mutableStateOf<CashFlowProjection?>(null)
    private val closeMonthModalVisible = mutableStateOf(false)
    private val monthCloseSummary = mutableStateOf<MonthCloseSummary?>(null)

    @Composable
    override fun uiState(): BudgetScreenState {
        LaunchedEffect(Unit) {
            start()
            loadMonthlyPlan(monthlyPeriod.value)
        }

        return BudgetScreenState(
            baseCurrency = getBaseCurrency(),
            categories = getCategories(),
            accounts = getAccounts(),
            budgets = getBudgets(),
            categoryBudgetsTotal = getCategoryBudgetsTotal(),
            appBudgetMax = getAppBudgetMax(),
            totalRemainingBudgetText = getTotalRemainingBudgetText(),
            timeRange = getTimeRange(),
            reorderModalVisible = getReorderModalVisible(),
            budgetModalData = getBudgetModalData(),
            selectedTab = selectedTab.value,
            monthlyPeriod = monthlyPeriod.value,
            monthlyPlan = monthlyPlan.value,
            editItemModalData = editItemModalData.value,
            editIncomeModalVisible = editIncomeModalVisible.value,
            cashFlowProjection = cashFlowProjection.value,
            closeMonthModalVisible = closeMonthModalVisible.value,
            monthCloseSummary = monthCloseSummary.value
        )
    }

    @Composable
    private fun getBaseCurrency(): String {
        return baseCurrency.value
    }

    @Composable
    private fun getTimeRange(): FromToTimeRange? {
        return timeRange.value
    }

    @Composable
    private fun getCategories(): ImmutableList<Category> {
        return categories.value
    }

    @Composable
    private fun getAccounts(): ImmutableList<Account> {
        return accounts.value
    }

    @Composable
    private fun getBudgets(): ImmutableList<DisplayBudget> {
        return budgets.value
    }

    @Composable
    private fun getReorderModalVisible(): Boolean {
        return reorderModalVisible.value
    }

    @Composable
    private fun getCategoryBudgetsTotal(): Double {
        return categoryBudgetsTotal.doubleValue
    }

    @Composable
    private fun getAppBudgetMax(): Double {
        return appBudgetMax.doubleValue
    }

    @Composable
    private fun getTotalRemainingBudgetText(): String? {
        val budgetExceeded = totalRemainingBudget.doubleValue < 0
        return when {
            categoryBudgetsTotal.doubleValue > 0 -> stringResource(
                if (budgetExceeded) R.string.budget_exceeded_info else R.string.total_budget_info,
                abs(totalRemainingBudget.doubleValue).format(baseCurrency.value),
                baseCurrency.value
            )

            else -> null
        }
    }

    @Composable
    private fun getBudgetModalData(): BudgetModalData? {
        return budgetModalData.value
    }

    override fun onEvent(event: BudgetScreenEvent) {
        when (event) {
            is BudgetScreenEvent.OnCreateBudget -> {
                createBudget(event.budgetData)
            }

            is BudgetScreenEvent.OnEditBudget -> {
                editBudget(event.budget)
            }

            is BudgetScreenEvent.OnDeleteBudget -> {
                deleteBudget(event.budget)
            }

            is BudgetScreenEvent.OnReorder -> {
                reorder(event.newOrder)
            }

            is BudgetScreenEvent.OnReorderModalVisible -> {
                reorderModalVisible.value = event.visible
            }

            is BudgetScreenEvent.OnBudgetModalData -> {
                budgetModalData.value = event.budgetModalData
            }

            is BudgetScreenEvent.OnSelectTab -> {
                selectedTab.value = event.tab
            }

            is BudgetScreenEvent.OnChangePeriod -> {
                monthlyPeriod.value = event.period
                loadMonthlyPlan(event.period)
            }

            is BudgetScreenEvent.OnCopyPreviousMonth -> {
                viewModelScope.launch {
                    copyPreviousMonthPlanUseCase(monthlyPeriod.value)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }

            is BudgetScreenEvent.OnEditPlannedIncome -> {
                viewModelScope.launch {
                    monthlyBudgetRepository.updatePlannedIncome(monthlyPeriod.value, event.income)
                    monthlyBudgetRepository.updateCarryover(monthlyPeriod.value, event.carryover)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }

            is BudgetScreenEvent.OnOpenEditIncomeModal -> {
                editIncomeModalVisible.value = event.visible
            }

            is BudgetScreenEvent.OnOpenEditItemModal -> {
                editItemModalData.value = event.data
            }

            is BudgetScreenEvent.OnSaveBudgetItem -> {
                viewModelScope.launch {
                    val itemId = event.itemId ?: UUID.randomUUID()
                    val entity = BudgetItemEntity(
                        id = itemId,
                        groupId = event.groupId,
                        planPeriod = event.period.value,
                        name = event.name,
                        plannedAmount = event.plannedAmount,
                        itemType = event.itemType.name,
                        categoryId = event.categoryId
                    )
                    saveBudgetItemUseCase.save(entity)

                    if (event.itemType == BudgetItemType.SINKING_FUND || event.targetAmount > 0.0) {
                        val existingFund = sinkingFundRepository.getSinkingFund(itemId)
                        val fund = SinkingFund(
                            id = existingFund?.id ?: UUID.randomUUID(),
                            budgetItemId = itemId,
                            targetAmount = event.targetAmount,
                            targetDate = existingFund?.targetDate,
                            openingBalance = event.openingBalance,
                            notes = event.notes ?: existingFund?.notes
                        )
                        sinkingFundRepository.saveSinkingFund(fund)
                    }

                    loadMonthlyPlan(event.period)
                }
            }

            is BudgetScreenEvent.OnDeleteBudgetItem -> {
                viewModelScope.launch {
                    sinkingFundRepository.deleteByBudgetItemId(event.itemId)
                    saveBudgetItemUseCase.delete(event.itemId)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }

            is BudgetScreenEvent.OnAddBudgetGroup -> {
                viewModelScope.launch {
                    val group = BudgetGroupEntity(
                        id = UUID.randomUUID(),
                        planPeriod = event.period.value,
                        name = event.name,
                        groupType = event.groupType.name
                    )
                    monthlyBudgetRepository.saveGroup(group)
                    loadMonthlyPlan(event.period)
                }
            }

            is BudgetScreenEvent.OnDeleteBudgetGroup -> {
                viewModelScope.launch {
                    monthlyBudgetRepository.deleteGroup(event.groupId)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }

            is BudgetScreenEvent.OnOpenCloseMonthModal -> {
                viewModelScope.launch {
                    if (event.visible) {
                        monthCloseSummary.value = runCatching {
                            closeMonthPlanUseCase.getSummary(monthlyPeriod.value)
                        }.getOrNull()
                    }
                    closeMonthModalVisible.value = event.visible
                }
            }

            is BudgetScreenEvent.OnCloseMonth -> {
                viewModelScope.launch {
                    closeMonthPlanUseCase.closeMonth(monthlyPeriod.value, event.rolloverSurplus)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }

            is BudgetScreenEvent.OnReopenMonth -> {
                viewModelScope.launch {
                    closeMonthPlanUseCase.reopenMonth(monthlyPeriod.value)
                    loadMonthlyPlan(monthlyPeriod.value)
                }
            }
        }
    }

    private fun loadMonthlyPlan(period: BudgetPeriod) {
        viewModelScope.launch {
            monthlyPlan.value = getMonthlyPlanSummaryUseCase(period)
            cashFlowProjection.value = runCatching { projectCashFlowUseCase(period) }.getOrNull()
        }
    }

    private fun start() {
        viewModelScope.launch {
            categories.value = categoryRepository.findAll().toImmutableList()
            val accounts = accountsAct(Unit)
            val baseCurrency = baseCurrencyAct(Unit)
            val startDateOfMonth = ivyContext.initStartDayOfMonthInMemory(sharedPrefs = sharedPrefs)
            val timeRange = com.ivy.legacy.data.model.TimePeriod.currentMonth(
                startDayOfMonth = startDateOfMonth
            ).toRange(startDateOfMonth = startDateOfMonth, timeConverter, timeProvider)
            val budgets = budgetsAct(Unit)

            appBudgetMax.doubleValue = budgets
                .filter { it.categoryIdsSerialized.isNullOrBlank() }
                .maxOfOrNull { it.amount } ?: 0.0

            categoryBudgetsTotal.doubleValue = budgets
                .filter { it.categoryIdsSerialized.isNotNullOrBlank() }
                .sumOf { it.amount }

            this@BudgetViewModel.budgets.value = com.ivy.legacy.utils.ioThread {
                budgets.map {
                    DisplayBudget(
                        budget = it,
                        spentAmount = calculateSpentAmount(
                            budget = it,
                            transactions = historyTrnsAct(timeRange.toCloseTimeRange()),
                            accounts = accounts,
                            baseCurrencyCode = baseCurrency
                        )
                    )
                }.toImmutableList()
            }
            totalRemainingBudget.doubleValue = calculateTotalRemainingBudget(
                budgets = this@BudgetViewModel.budgets.value,
                categoryBudgetsTotal = categoryBudgetsTotal.doubleValue
            )
            this@BudgetViewModel.accounts.value = accounts
            this@BudgetViewModel.baseCurrency.value = baseCurrency
            this@BudgetViewModel.timeRange.value = timeRange
        }
    }

    private suspend fun calculateSpentAmount(
        budget: Budget,
        transactions: List<Transaction>,
        baseCurrencyCode: String,
        accounts: List<Account>
    ): Double {
        val accountsFilter = budget.parseAccountIds()
        val categoryFilter = budget.parseCategoryIds()

        return transactions
            .filter { accountsFilter.isEmpty() || accountsFilter.contains(it.getAccountId()) }
            .filter { categoryFilter.isEmpty() || categoryFilter.contains(it.category?.value) }
            .sumOfSuspend {
                when (it) {
                    is Income -> {
                        0.0 // ignore income
                    }

                    is Expense -> {
                        // increment spent amount
                        exchangeAct(
                            ExchangeAct.Input(
                                data = ExchangeData(
                                    baseCurrency = baseCurrencyCode,
                                    fromCurrency = trnCurrency(it, accounts, baseCurrencyCode)
                                ),
                                amount = it.getValue()
                            )
                        ).orNull()?.toDouble() ?: 0.0
                    }

                    is Transfer -> {
                        // ignore transfers for simplicity
                        0.0
                    }
                }
            }
    }

    private fun createBudget(data: CreateBudgetData) {
        viewModelScope.launch {
            budgetCreator.createBudget(data) {
                start()
            }
        }
    }

    private fun editBudget(budget: Budget) {
        viewModelScope.launch {
            budgetCreator.editBudget(budget) {
                start()
            }
        }
    }

    private fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetCreator.deleteBudget(budget) {
                start()
            }
        }
    }

    private fun reorder(newOrder: List<DisplayBudget>) {
        this@BudgetViewModel.budgets.value = newOrder.toImmutableList()
        viewModelScope.launch {
            com.ivy.legacy.utils.ioThread {
                newOrder.forEachIndexed { index, item ->
                    budgetWriter.save(
                        item.budget.toEntity().copy(
                            orderId = index.toDouble(),
                            isSynced = false
                        )
                    )
                }
            }
            start()
        }
    }
}

fun calculateTotalRemainingBudget(
    budgets: ImmutableList<DisplayBudget>,
    categoryBudgetsTotal: Double
): Double {
    return categoryBudgetsTotal - budgets
        .filter { it.budget.categoryIdsSerialized.isNotNullOrBlank() }
        .sumOf { it.spentAmount }
}
