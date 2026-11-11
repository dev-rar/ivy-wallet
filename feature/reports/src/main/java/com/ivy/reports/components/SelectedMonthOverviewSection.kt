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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.reports.model.CategoryBudgetReportItem
import com.ivy.reports.model.MonthlyReportData
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.ItemIconSDefaultIcon
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.wallet.ui.theme.findContrastTextColor
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
            // Header: Top Left (Month Icon + Name) | Top Right (Savings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UI.colors.medium.copy(alpha = 0.4f), UI.shapes.r4Top)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Left: Month Icon + Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(UI.colors.pure),
                        contentAlignment = Alignment.Center
                    ) {
                        IvyIcon(
                            icon = R.drawable.ic_calendar,
                            tint = UI.colors.pureInverse,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = monthData.monthName,
                            style = UI.typo.b1.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = UI.colors.pureInverse
                            )
                        )
                        Text(
                            text = "$year",
                            style = UI.typo.c.style(
                                color = Gray,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                // Top Right: Savings
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isNetPositive) "Saved" else "Net Loss",
                        style = UI.typo.c.style(
                            color = Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    val prefix = if (isNetPositive) "+" else ""
                    Text(
                        text = "$prefix${monthData.netSavings.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.b1.style(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isNetPositive) Green else Red
                        )
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Center: Budget
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Monthly Budget",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (hasBudget) {
                        "${monthData.totalBudgeted.format(baseCurrency)} $baseCurrency"
                    } else {
                        "No budget set"
                    },
                    style = UI.typo.h2.style(
                        fontWeight = FontWeight.ExtraBold,
                        color = UI.colors.pureInverse
                    )
                )

                if (hasBudget) {
                    Spacer(Modifier.height(4.dp))
                    val isOver = monthData.totalBudgetSpent > monthData.totalBudgeted
                    Text(
                        text = if (isOver) {
                            "Spent: ${monthData.totalBudgetSpent.format(baseCurrency)} (Over budget)"
                        } else {
                            "Spent: ${monthData.totalBudgetSpent.format(baseCurrency)} • Left: ${(monthData.totalBudgeted - monthData.totalBudgetSpent).format(baseCurrency)}"
                        },
                        style = UI.typo.c.style(
                            color = if (isOver) Red else Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
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
                        style = UI.typo.b2.style(
                            color = Green,
                            fontWeight = FontWeight.ExtraBold
                        )
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
                        style = UI.typo.b2.style(
                            color = Red,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- 2. CATEGORIES BREAKDOWN (ALL CATEGORIES) ---
        Text(
            text = "${monthData.monthName} Categories Breakdown",
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
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Category Card matching the Category page widget styling (Ivy borders, rounded shape, icon badge, progress bar)
 * displaying Category Name, Left to Spend, Budget, and Spent. Clickable to view trends.
 */
@Composable
private fun CategoryBudgetDetailCard(
    item: CategoryBudgetReportItem,
    baseCurrency: String,
    onClick: () -> Unit
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
            .clickable(onClick = onClick)
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
