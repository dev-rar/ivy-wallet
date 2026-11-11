package com.ivy.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.reports.model.CategoryBudgetReportItem
import com.ivy.reports.model.MonthTransactionItem
import com.ivy.reports.model.MonthlyReportData
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.GradientGreen
import com.ivy.wallet.ui.theme.GradientRed
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.findContrastTextColor

/**
 * Monthly Overview Card styled EXACTLY like the Category page card:
 * - Top header styled like CategoryHeader with contrasting background, month name, icon, and Net Savings/Cash Flow
 * - Bottom section with Income vs Expense columns separated by vertical divider
 * - When selected, highlights and displays the detailed breakdown (Category Budgets + Monthly Transaction List) below
 */
@Composable
fun MonthlyOverviewCategoryCard(
    monthData: MonthlyReportData,
    year: Int,
    isSelected: Boolean,
    baseCurrency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNetPositive = monthData.netSavings >= 0
    val headerColor = if (isSelected) {
        if (isNetPositive) Green else Red
    } else {
        UI.colors.medium
    }
    val headerContrast = if (isSelected) White else UI.colors.pureInverse

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .border(
                width = if (isSelected) 2.dp else 1.5.dp,
                color = if (isSelected) Green else UI.colors.medium,
                shape = UI.shapes.r4
            )
            .background(UI.colors.pure)
            .clickable(onClick = onClick)
    ) {
        // 1. Header (Identical to CategoryHeader in CategoriesScreen)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor, UI.shapes.r4Top)
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) White.copy(alpha = 0.2f) else UI.colors.pure),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = monthData.monthAbbrev.take(3).uppercase(),
                            style = UI.typo.c.style(
                                color = headerContrast,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "${monthData.monthName} $year",
                        style = UI.typo.b1.style(
                            color = headerContrast,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                if (monthData.totalBudgetSavings > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) White.copy(alpha = 0.25f) else Green.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${monthData.totalBudgetSavings.format(baseCurrency)} saved",
                            style = UI.typo.c.style(
                                color = if (isSelected) White else Green,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Net Cash Flow Balance Row
            val prefix = if (isNetPositive) "+" else ""
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "$prefix${monthData.netSavings.format(baseCurrency)} $baseCurrency",
                style = UI.typo.h2.style(
                    color = headerContrast,
                    fontWeight = FontWeight.ExtraBold
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // 2. AddedSpent section (Income vs Expense with vertical divider, matching CategoriesScreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Income",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "+${monthData.income.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(
                        color = Green,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Divider
            Spacer(
                modifier = Modifier
                    .width(2.dp)
                    .height(38.dp)
                    .background(UI.colors.medium, UI.shapes.rFull)
            )

            // Expense Column
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Expenses",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "-${monthData.expenses.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(
                        color = Red,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))
    }
}

/**
 * Detailed Section shown below the selected month:
 * 1. Category Budgets breakdown (All categories: with budget & without budget)
 * 2. Detailed Transaction History list (Income in Green, Expense in Red)
 */
@Composable
fun SelectedMonthDetailsSection(
    monthData: MonthlyReportData,
    baseCurrency: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // --- SECTION A: ALL CATEGORIES (BUDGET & EXPENSES) ---
        Text(
            text = "Categories Breakdown",
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
                    baseCurrency = baseCurrency
                )
                Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- SECTION B: MONTHLY TRANSACTIONS LIST ---
        Text(
            text = "${monthData.monthName} Transactions",
            style = UI.typo.b1.style(
                fontWeight = FontWeight.ExtraBold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.height(12.dp))

        if (monthData.monthTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(UI.shapes.r4)
                    .background(UI.colors.medium.copy(alpha = 0.3f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions recorded for ${monthData.monthName}.",
                    style = UI.typo.b2.style(color = Gray)
                )
            }
        } else {
            for (trn in monthData.monthTransactions) {
                TransactionHistoryItemRow(
                    trn = trn
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Card for each category showing Category Name, Left to Spend, Budget, and Spent.
 * Styled matching the Category page widget.
 */
@Composable
private fun CategoryBudgetDetailCard(
    item: CategoryBudgetReportItem,
    baseCurrency: String
) {
    val contrastColor = findContrastTextColor(item.categoryColor)
    val leftToSpend = item.budgetAmount - item.spentAmount
    val progress = if (item.budgetAmount > 0) {
        (item.spentAmount / item.budgetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val barColor = if (item.isOverspent) Red else Green

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .border(1.5.dp, UI.colors.medium, UI.shapes.r4)
            .background(UI.colors.pure)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Category icon & title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(item.categoryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        ItemIconSDefaultIcon(
                            iconName = item.categoryIcon,
                            defaultIcon = R.drawable.ic_custom_category_s,
                            tint = contrastColor
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = item.categoryName,
                        style = UI.typo.b2.style(
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                }

                // Left to spend or unbudgeted expense
                Column(horizontalAlignment = Alignment.End) {
                    if (item.hasBudget) {
                        Text(
                            text = if (item.isOverspent) "Over budget" else "Left to spend",
                            style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = if (item.isOverspent) {
                                "-${(-leftToSpend).format(baseCurrency)} $baseCurrency"
                            } else {
                                "${leftToSpend.format(baseCurrency)} $baseCurrency"
                            },
                            style = UI.typo.b2.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.isOverspent) Red else Green
                            )
                        )
                    } else {
                        Text(
                            text = "Unbudgeted",
                            style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = "-${item.spentAmount.format(baseCurrency)} $baseCurrency",
                            style = UI.typo.b2.style(
                                fontWeight = FontWeight.Bold,
                                color = if (item.spentAmount > 0) Red else Gray
                            )
                        )
                    }
                }
            }

            if (item.hasBudget) {
                Spacer(Modifier.height(10.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(UI.colors.medium)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spent: ${item.spentAmount.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Budget: ${item.budgetAmount.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

/**
 * Clean transaction item row with Green for Income and Red for Expense
 */
@Composable
private fun TransactionHistoryItemRow(
    trn: MonthTransactionItem
) {
    val amountColor = if (trn.isIncome) Green else Red
    val contrastColor = findContrastTextColor(trn.categoryColor)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .border(1.dp, UI.colors.medium.copy(alpha = 0.6f), UI.shapes.r4)
            .background(UI.colors.pure)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon + Title & Category subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(trn.categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    ItemIconSDefaultIcon(
                        iconName = trn.categoryIcon,
                        defaultIcon = if (trn.isIncome) R.drawable.ic_income else R.drawable.ic_expense,
                        tint = contrastColor
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column {
                    Text(
                        text = trn.title,
                        style = UI.typo.b2.style(
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${trn.categoryName} • ${trn.dateFormatted}",
                        style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                    )
                }
            }

            // Amount formatted in Green or Red
            Text(
                text = trn.amountFormatted,
                style = UI.typo.b2.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
            )
        }
    }
}

