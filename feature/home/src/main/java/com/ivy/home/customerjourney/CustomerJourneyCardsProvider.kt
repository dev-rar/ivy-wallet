package com.ivy.home.customerjourney

import com.ivy.base.legacy.SharedPrefs
import com.ivy.base.legacy.stringRes
import com.ivy.base.model.TransactionType
import com.ivy.base.time.TimeProvider
import com.ivy.data.db.dao.read.PlannedPaymentRuleDao
import com.ivy.data.repository.TransactionRepository
import com.ivy.design.l0_system.Gradient
import com.ivy.design.l0_system.Green
import com.ivy.design.l0_system.GreenLight
import com.ivy.design.l0_system.Ivy
import com.ivy.design.l0_system.Orange
import com.ivy.design.l0_system.Red
import com.ivy.legacy.IvyWalletCtx
import com.ivy.legacy.data.model.MainTab
import com.ivy.navigation.BudgetScreen
import com.ivy.navigation.EditPlannedScreen
import com.ivy.wallet.domain.action.budget.CheckBudgetAlertsAct
import com.ivy.navigation.Navigation
import com.ivy.navigation.PieChartStatisticScreen
import com.ivy.navigation.PollScreen
import com.ivy.poll.data.PollRepository
import com.ivy.poll.data.model.PollId
import com.ivy.ui.R
import com.ivy.widget.transaction.AddTransactionWidgetCompact
import java.time.LocalDate
import javax.inject.Inject

@Deprecated("Legacy code")
class CustomerJourneyCardsProvider @Inject constructor(
  private val transactionRepository: TransactionRepository,
  private val plannedPaymentRuleDao: PlannedPaymentRuleDao,
  private val sharedPrefs: SharedPrefs,
  private val ivyContext: IvyWalletCtx,
  private val pollRepository: PollRepository,
  private val timeProvider: TimeProvider,
  private val checkBudgetAlertsAct: CheckBudgetAlertsAct,
) {

  suspend fun loadCards(): List<CustomerJourneyCardModel> {
    val trnCount = transactionRepository.countHappenedTransactions().value
    val plannedPaymentsCount = plannedPaymentRuleDao.countPlannedPayments()
    val deps = CustomerJourneyDeps(
      pollRepository = pollRepository,
      timeProvider = timeProvider,
    )

    val staticCards = ACTIVE_CARDS
      .filter {
        it.condition(
          trnCount,
          plannedPaymentsCount,
          ivyContext,
          deps
        ) && !isCardDismissed(it)
      }

    val dynamicBudgetCards = loadBudgetAlertCards()
    return dynamicBudgetCards + staticCards
  }

  private suspend fun loadBudgetAlertCards(): List<CustomerJourneyCardModel> {
    val alerts = checkBudgetAlertsAct.checkAlerts()
    val today = timeProvider.localDateNow()
    val monthKey = "${today.year}_${today.monthValue}"

    return alerts.mapNotNull { alert ->
      val severityKey = if (alert.isOverspent) "overspent" else "warning"
      val cardId = "budget_alert_${alert.categoryId}_${monthKey}_$severityKey"

      val cardModel = CustomerJourneyCardModel(
        id = cardId,
        condition = { _, _, _, _ -> true },
        title = if (alert.isOverspent) {
          "🚨 Budget Exceeded: ${alert.categoryName}"
        } else {
          "⚠️ Approaching Limit: ${alert.categoryName}"
        },
        description = if (alert.isOverspent) {
          "You've exceeded your budget (${String.format("%,.0f", alert.spentAmount)} / ${String.format("%,.0f", alert.budgetAmount)} ${alert.currency})."
        } else {
          "You've spent ${alert.percentage}% of your budget (${String.format("%,.0f", alert.spentAmount)} / ${String.format("%,.0f", alert.budgetAmount)} ${alert.currency})."
        },
        cta = "View Budget",
        ctaIcon = R.drawable.ic_custom_bills_s,
        background = if (alert.isOverspent) Gradient.solid(Red) else Gradient.solid(Orange),
        hasDismiss = true,
        onAction = { navigation, _, _ ->
          navigation.navigateTo(BudgetScreen)
        }
      )

      if (isCardDismissed(cardModel)) null else cardModel
    }
  }

  private fun isCardDismissed(cardData: CustomerJourneyCardModel): Boolean {
    return sharedPrefs.getBoolean(sharedPrefsKey(cardData), false)
  }

  fun dismissCard(cardData: CustomerJourneyCardModel) {
    sharedPrefs.putBoolean(sharedPrefsKey(cardData), true)
  }

  private fun sharedPrefsKey(cardData: CustomerJourneyCardModel): String {
    return "${cardData.id}${SharedPrefs._CARD_DISMISSED}"
  }

  companion object {
    val ACTIVE_CARDS = listOf(
      adjustBalanceCard(),
      addPlannedPaymentCard(),
      didYouKnow_pinAddTransactionWidgetCard(),
      didYouKnow_expensesPieChart(),
      voteCard()
    )

    fun adjustBalanceCard() = CustomerJourneyCardModel(
      id = "adjust_balance",
      condition = { trnCount, _, _, _ ->
        trnCount == 0L
      },
      title = stringRes(R.string.adjust_initial_balance),
      description = stringRes(R.string.adjust_initial_balance_description),
      cta = stringRes(R.string.to_accounts),
      ctaIcon = R.drawable.ic_custom_account_s,
      background = Gradient.solid(Ivy),
      hasDismiss = false,
      onAction = { _, ivyContext, _ ->
        ivyContext.selectMainTab(MainTab.ACCOUNTS)
      }
    )

    fun addPlannedPaymentCard() = CustomerJourneyCardModel(
      id = "add_planned_payment",
      condition = { trnCount, plannedPaymentCount, _, _ ->
        trnCount >= 1 && plannedPaymentCount == 0L
      },
      title = stringRes(R.string.create_first_planned_payment),
      description = stringRes(R.string.create_first_planned_payment_description),
      cta = stringRes(R.string.add_planned_payment),
      ctaIcon = R.drawable.ic_planned_payments,
      background = Gradient.solid(Orange),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(
          EditPlannedScreen(
            type = TransactionType.EXPENSE,
            plannedPaymentRuleId = null
          )
        )
      }
    )

    fun didYouKnow_pinAddTransactionWidgetCard() = CustomerJourneyCardModel(
      id = "add_transaction_widget",
      condition = { trnCount, _, _, _ ->
        trnCount >= 3
      },
      title = stringRes(R.string.did_you_know),
      description = stringRes(R.string.widget_description),
      cta = stringRes(R.string.add_widget),
      ctaIcon = R.drawable.ic_custom_atom_s,
      background = Gradient.solid(GreenLight),
      hasDismiss = true,
      onAction = { _, _, ivyActivity ->
        ivyActivity.pinWidget(AddTransactionWidgetCompact::class.java)
      }
    )

    fun didYouKnow_expensesPieChart() = CustomerJourneyCardModel(
      id = "expenses_pie_chart",
      condition = { trnCount, _, _, _ ->
        trnCount >= 7
      },
      title = stringRes(R.string.did_you_know),
      description = stringRes(R.string.you_can_see_a_piechart),
      cta = stringRes(R.string.expenses_piechart),
      ctaIcon = R.drawable.ic_custom_bills_s,
      background = Gradient.solid(Red),
      hasDismiss = true,
      onAction = { navigation, _, _ ->
        navigation.navigateTo(PieChartStatisticScreen(type = TransactionType.EXPENSE))
      }
    )

    fun rateUsCard() = CustomerJourneyCardModel(
      id = "rate_us",
      condition = { trnCount, _, _, _ ->
        trnCount >= 10
      },
      title = stringRes(R.string.review_ivy_wallet),
      description = stringRes(R.string.review_ivy_wallet_description),
      cta = stringRes(R.string.rate_us_on_google_play),
      ctaIcon = R.drawable.ic_custom_star_s,
      background = Gradient.solid(Green),
      hasDismiss = true,
      onAction = { _, _, ivyActivity ->
        ivyActivity.reviewIvyWallet(dismissReviewCard = true)
      }
    )

    @Suppress("MaxLineLength", "NoImplicitFunctionReturnType", "MagicNumber")
    private fun voteCard() = CustomerJourneyCardModel(
      id = "vote_card",
      // to users that haven't voted
      condition = { trnCount, _, _, deps ->
        val expiry = LocalDate.of(2025, 7, 28)
        trnCount > 3 &&
            // set expiration
            deps.timeProvider.localDateNow().isBefore(expiry) &&
            !deps.pollRepository.hasVoted(PollId.PaidIvy)
      },
      title = "How much are you willing to pay for Ivy Wallet?",
      description = "Google Play requires us to update Ivy Wallet to target API level 35 (Android 15). We'd like to know if you will be interested to pay on a subscription basis so we can maintain the app.",
      cta = "Vote",
      ctaIcon = R.drawable.ic_telegram_24dp,
      hasDismiss = false,
      background = Gradient.solid(Ivy),
      // navigate to PollScreen
      onAction = { navigation: Navigation, _, _ ->
        navigation.navigateTo(PollScreen)
      }
    )
  }
}
