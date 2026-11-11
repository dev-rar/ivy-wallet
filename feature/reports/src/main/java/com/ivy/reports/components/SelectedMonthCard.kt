package com.ivy.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.ivy.wallet.ui.theme.findContrastTextColor

@Composable
fun SelectedMonthCard(
    monthData: MonthlyReportData,
    year: Int,
    baseCurrency: String,
    modifier: Modifier = Modifier
) {
    val isNetPositive = monthData.netSavings >= 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.pure)
            .padding(18.dp)
    ) {
        // Month Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${monthData.monthName} $year",
                style = UI.typo.b1.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isNetPositive) Green.copy(alpha = 0.12f) else Red.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                val prefix = if (isNetPositive) "+" else ""
                Text(
                    text = "$prefix${monthData.netSavings.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.c.style(
                        color = if (isNetPositive) Green else Red,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Income vs Expenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Income",
                    style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "+${monthData.income.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = Green, fontWeight = FontWeight.Bold)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Expenses",
                    style = UI.typo.c.style(color = Gray, fontWeight = FontWeight.Medium)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "-${monthData.expenses.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = Red, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Budget Performance Breakdown by Category
        if (monthData.categoryBudgets.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budgets",
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )

                val hasSavings = monthData.totalBudgetSavings > 0
                Text(
                    text = if (hasSavings) {
                        "+${monthData.totalBudgetSavings.format(baseCurrency)} saved"
                    } else {
                        "0 $baseCurrency saved"
                    },
                    style = UI.typo.c.style(
                        color = if (hasSavings) Green else Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            // Category Cards styled exactly like Category Page widgets
            for (cat in monthData.categoryBudgets) {
                CategoryBudgetCard(
                    item = cat,
                    baseCurrency = baseCurrency
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Category Card matching the Category page widget styling (Ivy borders, rounded shape, icon badge, progress bar)
 * displaying Category Name, Left to Spend, and Budget.
 */
@Composable
private fun CategoryBudgetCard(
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
            .border(2.dp, UI.colors.medium, UI.shapes.r4)
            .background(UI.colors.pure)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Category Icon + Name, and Left to Spend on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
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

                // Left to spend badge / label
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.isOverspent) "Over budget" else "Left to spend",
                        style = UI.typo.c.style(
                            color = Gray,
                            fontWeight = FontWeight.Medium
                        )
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
                }
            }

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

            // Bottom Subtext: Spent vs Budget
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spent: ${item.spentAmount.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Budget: ${item.budgetAmount.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}
