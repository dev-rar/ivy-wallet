package com.ivy.widget.balance

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ivy.ui.R

/**
 * Modern Material You Jetpack Glance Balance & Budget Widget.
 *
 * Displays:
 * 1. Current Balance & Currency header
 * 2. Monthly Income & Expense breakdown pills
 * 3. Monthly Budget status pill (Budget Remaining / Exceeded)
 * 4. 1-Tap Quick Action Buttons (+ Income, + Expense, + Transfer)
 */
@Composable
fun WalletBalanceWidgetContent(
    appLocked: Boolean,
    balance: String,
    currency: String,
    income: String,
    expense: String,
    hasBudget: Boolean = false,
    budgetTotal: String = "",
    budgetRemaining: String = "",
    budgetPercent: Float = 0f,
    onIncomeClick: Action,
    onExpenseClick: Action,
    onTransferClick: Action,
    onWidgetClick: Action,
) {
    val resources = LocalContext.current.resources
    Box(
        GlanceModifier
            .background(ImageProvider(R.drawable.shape_widget_background))
            .clickable(onWidgetClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = if (appLocked) Alignment.CenterHorizontally else Alignment.Start
        ) {
            if (appLocked) {
                Text(
                    modifier = GlanceModifier.padding(8.dp),
                    text = resources.getString(R.string.app_locked),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.White),
                        textAlign = TextAlign.Center
                    )
                )
            } else {
                BalanceSection(balance = balance, currency = currency)
                Spacer(GlanceModifier.height(8.dp))
                IncomeExpenseSection(income = income, expense = expense, currency = currency)

                if (hasBudget) {
                    Spacer(GlanceModifier.height(8.dp))
                    BudgetStatusSection(
                        budgetRemaining = budgetRemaining,
                        budgetTotal = budgetTotal,
                        currency = currency,
                        percent = budgetPercent
                    )
                }

                Spacer(GlanceModifier.height(6.dp))
                ButtonsSection(
                    onIncomeClick = onIncomeClick,
                    onExpenseClick = onExpenseClick,
                    onTransferClick = onTransferClick
                )
            }
        }
    }
}

@Composable
fun BalanceSection(
    balance: String,
    currency: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
    ) {
        Text(
            text = currency,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                color = ColorProvider(Color.White.copy(alpha = 0.8f))
            )
        )
        Spacer(GlanceModifier.width(8.dp))
        Text(
            text = balance,
            style = TextStyle(
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.White)
            )
        )
    }
}

@Composable
fun IncomeExpenseSection(
    income: String,
    expense: String,
    currency: String,
) {
    Row(
        GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val resources = LocalContext.current.resources
        // Income Pill
        Row(
            GlanceModifier
                .padding(vertical = 6.dp, horizontal = 10.dp)
                .defaultWeight()
                .background(ImageProvider(R.drawable.income_shape_widget_background)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_income_white),
                contentDescription = resources.getString(R.string.income),
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "+$income $currency",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.White)
                )
            )
        }
        Spacer(GlanceModifier.width(8.dp))
        // Expense Pill
        Row(
            GlanceModifier
                .padding(vertical = 6.dp, horizontal = 10.dp)
                .defaultWeight()
                .background(ImageProvider(R.drawable.expense_shape_widget_background)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_expense),
                contentDescription = resources.getString(R.string.expense),
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = "-$expense $currency",
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color.Black)
                )
            )
        }
    }
}

@Composable
fun BudgetStatusSection(
    budgetRemaining: String,
    budgetTotal: String,
    currency: String,
    percent: Float
) {
    val isOver = percent >= 1.0f
    Row(
        GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 10.dp)
            .background(ImageProvider(R.drawable.budget_shape_widget_background)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_budget_indicator),
            contentDescription = null,
            modifier = GlanceModifier.size(14.dp)
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = if (isOver) {
                "Budget Exceeded • Limit: $budgetTotal $currency"
            } else {
                "$budgetRemaining $currency left • Limit: $budgetTotal $currency"
            },
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ColorProvider(if (isOver) Color(0xFFFF6B6B) else Color(0xFF9E9E9E))
            )
        )
    }
}

@Composable
fun ButtonsSection(
    onIncomeClick: Action,
    onExpenseClick: Action,
    onTransferClick: Action,
) {
    val buttons = listOf(
        R.drawable.ic_widget_income to onIncomeClick,
        R.drawable.ic_widget_expense to onExpenseClick,
        R.drawable.ic_widget_transfer to onTransferClick,
    )
    Row(
        GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        buttons.forEach { (image, onClick) ->
            WidgetClickableItem(
                image = image,
                onClick = onClick,
            )
        }
    }
}

@Composable
fun RowScope.WidgetClickableItem(
    @DrawableRes image: Int,
    onClick: Action,
) {
    Column(
        GlanceModifier
            .defaultWeight()
            .clickable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = GlanceModifier.size(46.dp),
            provider = ImageProvider(image),
            contentDescription = null
        )
    }
}
