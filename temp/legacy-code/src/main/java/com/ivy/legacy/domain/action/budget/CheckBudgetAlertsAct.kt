package com.ivy.wallet.domain.action.budget

import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeConverter
import com.ivy.base.time.TimeProvider
import com.ivy.data.model.Category
import com.ivy.data.model.CategoryId
import com.ivy.data.model.Expense
import com.ivy.data.repository.CategoryRepository
import com.ivy.data.repository.TransactionRepository
import com.ivy.data.temp.migration.getAccountId
import com.ivy.data.temp.migration.getValue
import com.ivy.frp.action.FPAction
import com.ivy.frp.then
import com.ivy.legacy.datamodel.Account
import com.ivy.legacy.datamodel.Budget
import com.ivy.wallet.domain.pure.transaction.trnCurrency
import com.ivy.wallet.android.notification.IvyNotificationChannel
import com.ivy.wallet.android.notification.NotificationService
import com.ivy.wallet.domain.action.account.AccountsAct
import com.ivy.wallet.domain.action.exchange.ExchangeAct
import com.ivy.wallet.domain.action.settings.BaseCurrencyAct
import com.ivy.wallet.domain.pure.exchange.ExchangeData
import com.ivy.wallet.domain.pure.util.orZero
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class BudgetAlertInfo(
    val categoryId: UUID,
    val categoryName: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val percentage: Int,
    val isOverspent: Boolean,
    val currency: String
)

@Singleton
class CheckBudgetAlertsAct @Inject constructor(
    private val budgetsAct: BudgetsAct,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountsAct: AccountsAct,
    private val baseCurrencyAct: BaseCurrencyAct,
    private val exchangeAct: ExchangeAct,
    private val timeProvider: TimeProvider,
    private val timeConverter: TimeConverter,
    private val notificationService: NotificationService
) : FPAction<Unit, List<BudgetAlertInfo>>() {

    override suspend fun Unit.compose(): suspend () -> List<BudgetAlertInfo> = suspend {
        checkAlerts()
    }

    suspend fun checkAlerts(): List<BudgetAlertInfo> {
        val today = timeProvider.localDateNow()
        val startOfMonth = with(timeConverter) {
            today.withDayOfMonth(1).atStartOfDay().toUTC()
        }
        val endOfMonth = with(timeConverter) {
            today.withDayOfMonth(today.lengthOfMonth()).atTime(LocalTime.MAX).toUTC()
        }

        val allBudgets = budgetsAct(Unit).filter { !it.isDeleted && it.amount > 0 }
        if (allBudgets.isEmpty()) return emptyList()

        val baseCurrency = baseCurrencyAct(Unit)
        val allAccounts = accountsAct(Unit)
        val allTransactions = transactionRepository.findAllBetween(startOfMonth, endOfMonth)
        val allCategories = categoryRepository.findAll().associateBy { it.id.value }

        val alertList = mutableListOf<BudgetAlertInfo>()

        for (budget in allBudgets) {
            val catIds = budget.parseCategoryIds()
            if (catIds.isEmpty()) continue

            val accountsFilter = budget.parseAccountIds()

            // Calculate spent amount for this budget in the current month
            var spent = 0.0
            for (trn in allTransactions) {
                if (trn !is Expense) continue
                if (accountsFilter.isNotEmpty() && !accountsFilter.contains(trn.getAccountId())) continue

                val trnCatId = trn.category?.value
                if (trnCatId != null && catIds.contains(trnCatId)) {
                    val trnCur = trnCurrency(trn, allAccounts, baseCurrency)
                    val converted = exchangeAct(
                        ExchangeAct.Input(
                            data = ExchangeData(
                                baseCurrency = baseCurrency,
                                fromCurrency = trnCur
                            ),
                            amount = trn.getValue()
                        )
                    ).orZero().toDouble()
                    spent += converted
                }
            }

            val ratio = if (budget.amount > 0) spent / budget.amount else 0.0
            val percentage = (ratio * 100).toInt()

            if (percentage >= 80) {
                val primaryCatId = catIds.first()
                val catName = allCategories[primaryCatId]?.name?.value ?: budget.name
                alertList.add(
                    BudgetAlertInfo(
                        categoryId = primaryCatId,
                        categoryName = catName,
                        budgetAmount = budget.amount,
                        spentAmount = spent,
                        percentage = percentage,
                        isOverspent = spent > budget.amount,
                        currency = baseCurrency
                    )
                )
            }
        }

        return alertList
    }

    /**
     * Checks if a newly saved expense transaction pushes any category past 80% or 100%,
     * and fires an instant Android heads-up notification.
     */
    suspend fun checkAndNotifyPostTransaction(targetCategoryId: UUID?) {
        if (targetCategoryId == null) return
        try {
            val alerts = checkAlerts()
            val matchedAlert = alerts.find { it.categoryId == targetCategoryId } ?: return

            val title = if (matchedAlert.isOverspent) {
                "🚨 Budget Exceeded: ${matchedAlert.categoryName}"
            } else {
                "⚠️ ${matchedAlert.percentage}% of Budget Reached: ${matchedAlert.categoryName}"
            }

            val spentStr = String.format("%,.0f", matchedAlert.spentAmount)
            val budgetStr = String.format("%,.0f", matchedAlert.budgetAmount)
            val content = if (matchedAlert.isOverspent) {
                "You've exceeded your budget ($spentStr / $budgetStr ${matchedAlert.currency})."
            } else {
                "Spent: $spentStr / $budgetStr ${matchedAlert.currency} (${matchedAlert.percentage}%)."
            }

            val notification = notificationService.defaultIvyNotification(
                channel = IvyNotificationChannel.BUDGET_ALERTS
            )
            notification.setContentTitle(title)
                .setContentText(content)
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(content))

            val notificationId = ("budget_${matchedAlert.categoryId}".hashCode())
            notificationService.showNotification(notification, notificationId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
