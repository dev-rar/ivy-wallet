package com.ivy.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.balancePrefix
import com.ivy.legacy.utils.format
import com.ivy.reports.model.CategoryBudgetReportItem
import com.ivy.reports.model.MonthlyReportData
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
import com.ivy.wallet.ui.theme.wallet.AmountCurrencyB1
import java.util.UUID

/**
 * Selected Month Overview Card designed to user's exact specification:
 * - Top Left: Month calendar icon badge + Month Name & Year
 * - Top Right: Savings amount
 * - Center: Budget amount
 * - Footer: Income and Expense separated cleanly
 *
 * Below it:
 * - Full Categories breakdown (All categories: with budget, spent, left to spend, and unbudgeted)
 * - Tapping any category opens its historical spending trend bottom sheet
 */
@Composable
fun SelectedMonthOverviewSection(
    monthData: MonthlyReportData,
    year: Int,
    baseCurrency: String,
    onCategoryClick: (UUID?) -> Unit,
    modifier: Modifier = Modifier
) {
    val isNetPositive = monthData.netSavings >= 0
    val hasBudget = monthData.totalBudgeted > 0

    Column(modifier = modifier.fillMaxWidth()) {
        // --- 1. SELECTED MONTH HERO CARD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(UI.shapes.r4)
                .border(2.dp, UI.colors.medium, UI.shapes.r4)
                .background(UI.colors.pure)
        ) {
            // Header: Top Left (Month Name + Budget below it) | Top Right (Budget status if set)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UI.colors.medium.copy(alpha = 0.4f), UI.shapes.r4Top)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Left: Month Name & Budget label below it
                Column {
                    Text(
                        text = monthData.monthName,
                        style = UI.typo.b1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = UI.colors.pureInverse
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (hasBudget) {
                            "Budget: ${monthData.totalBudgeted.format(baseCurrency)} $baseCurrency"
                        } else {
                            "Budget: Not set"
                        },
                        style = UI.typo.c.style(
                            color = Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Top Right: Left to spend / Over budget when budget is set
                if (hasBudget) {
                    val isOver = monthData.totalBudgetSpent > monthData.totalBudgeted
                    val leftAmount = monthData.totalBudgeted - monthData.totalBudgetSpent
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOver) "Over budget" else "Left to spend",
                            style = UI.typo.c.style(
                                color = Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = if (isOver) {
                                "-${(-leftAmount).format(baseCurrency)} $baseCurrency"
                            } else {
                                "${leftAmount.format(baseCurrency)} $baseCurrency"
                            },
                            style = UI.typo.nB2.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOver) Red else UI.colors.pureInverse
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Center: Saved Money
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNetPositive) "Saved" else "Net Loss",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                val prefix = if (isNetPositive) "+" else ""
                Text(
                    text = "$prefix${monthData.netSavings.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isNetPositive) Green else Red
                    ).copy(fontSize = 24.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(18.dp))

            // Footer: Income and Expense
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UI.colors.medium.copy(alpha = 0.25f), UI.shapes.r4Bot)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Income",
                        style = UI.typo.c.style(
                            color = Gray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "+${monthData.income.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = Green,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Vertical Divider
                Spacer(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                        .background(UI.colors.medium, UI.shapes.rFull)
                )

                // Expense
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expenses",
                        style = UI.typo.c.style(
                            color = Gray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "-${monthData.expenses.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = Red,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 2. CATEGORY BREAKDOWN (ALL CATEGORIES) ---
        Text(
            text = "Category Breakdown",
            style = UI.typo.b1.style(
                fontWeight = FontWeight.ExtraBold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.height(12.dp))

        if (monthData.categoryBudgets.isEmpty()) {
            Text(
                text = "No categories found.",
                style = UI.typo.b2.style(color = Gray)
            )
        } else {
            for (cat in monthData.categoryBudgets) {
                CategoryBudgetDetailCard(
                    item = cat,
                    baseCurrency = baseCurrency,
                    onClick = { onCategoryClick(cat.categoryId) }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Category Card matching the exact Categories page card design (DefaultCategoryCard),
 * showing colored header with net monthly balance and footer with monthly expenses vs income.
 * Clickable to view historical category trends.
 */
@Composable
private fun CategoryBudgetDetailCard(
    item: CategoryBudgetReportItem,
    baseCurrency: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .clickable(onClick = onClick)
    ) {
        CategoryHeader(
            item = item,
            currency = baseCurrency,
            contrastColor = findContrastTextColor(item.categoryColor)
        )

        Spacer(Modifier.height(12.dp))

        CategoryAddedSpent(
            currency = baseCurrency,
            monthlyIncome = item.incomeAmount,
            monthlyExpenses = item.spentAmount
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CategoryHeader(
    item: CategoryBudgetReportItem,
    currency: String,
    contrastColor: Color,
) {
    val balancePrefixValue = balancePrefix(
        income = item.incomeAmount,
        expenses = item.spentAmount
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(item.categoryColor, UI.shapes.r4Top)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(20.dp))

            ItemIconSDefaultIcon(
                iconName = item.categoryIcon,
                defaultIcon = R.drawable.ic_custom_category_s,
                tint = contrastColor
            )

            Spacer(Modifier.width(8.dp))

            Text(
                text = item.categoryName,
                style = UI.typo.b1.style(
                    color = contrastColor,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.height(4.dp))

        BalanceRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            textColor = contrastColor,
            currency = currency,
            balance = item.monthlyBalance,
            balanceFontSize = 24.sp,
            currencyFontSize = 24.sp,
            currencyUpfront = false,
            balanceAmountPrefix = balancePrefixValue
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CategoryAddedSpent(
    monthlyIncome: Double,
    monthlyExpenses: Double,
    currency: String,
    modifier: Modifier = Modifier,
    textColor: Color = UI.colors.pureInverse,
    dividerColor: Color = UI.colors.medium,
    center: Boolean = true,
    dividerSpacer: Dp? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (center) {
            Spacer(Modifier.weight(1f))
        }

        CategoryLabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_expenses),
            amount = monthlyExpenses,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        // Divider
        Spacer(
            modifier = Modifier
                .width(2.dp)
                .height(48.dp)
                .background(dividerColor, UI.shapes.rFull)
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }

        if (dividerSpacer != null) {
            Spacer(modifier = Modifier.width(dividerSpacer))
        }

        CategoryLabelAmount(
            textColor = textColor,
            label = stringResource(R.string.month_income),
            amount = monthlyIncome,
            currency = currency,
            center = center
        )

        if (center) {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryLabelAmount(
    label: String,
    amount: Double,
    currency: String,
    textColor: Color,
    center: Boolean
) {
    Column(
        horizontalAlignment = if (center) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = label,
            style = UI.typo.c.style(
                color = textColor,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AmountCurrencyB1(
                textColor = textColor,
                amount = amount,
                currency = currency
            )
        }
    }
}
