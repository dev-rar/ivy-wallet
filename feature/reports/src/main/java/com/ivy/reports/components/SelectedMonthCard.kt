package com.ivy.reports.components

import androidx.compose.foundation.background
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
import com.ivy.wallet.ui.theme.components.IvyIcon

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
                    text = "${monthData.income.format(baseCurrency)} $baseCurrency",
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
                    text = "${monthData.expenses.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b2.style(color = Red, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Budget Performance Section (if category budgets exist)
        if (monthData.categoryBudgets.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(UI.colors.medium)
            )

            Spacer(Modifier.height(14.dp))

            // Budget Savings Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IvyIcon(
                        icon = R.drawable.ic_buffer_ok,
                        tint = Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Budget Savings",
                        style = UI.typo.b2.style(
                            fontWeight = FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                }

                val hasSavings = monthData.totalBudgetSavings > 0
                Text(
                    text = if (hasSavings) {
                        "+${monthData.totalBudgetSavings.format(baseCurrency)} $baseCurrency saved"
                    } else {
                        "0 $baseCurrency saved"
                    },
                    style = UI.typo.c.style(
                        color = if (hasSavings) Green else Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // Category breakdown rows
            for (cat in monthData.categoryBudgets) {
                CategoryBudgetRow(
                    item = cat,
                    baseCurrency = baseCurrency
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun CategoryBudgetRow(
    item: CategoryBudgetReportItem,
    baseCurrency: String
) {
    val progress = if (item.budgetAmount > 0) {
        (item.spentAmount / item.budgetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f
    val barColor = if (item.isOverspent) Red else Green

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(UI.colors.medium)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(item.categoryColor)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.categoryName,
                    style = UI.typo.b2.style(
                        fontWeight = FontWeight.Bold,
                        color = UI.colors.pureInverse
                    )
                )
            }

            Text(
                text = if (item.isOverspent) {
                    "Overspent ${item.spentAmount.format(baseCurrency)}"
                } else {
                    "+${item.savedAmount.format(baseCurrency)} saved"
                },
                style = UI.typo.c.style(
                    color = if (item.isOverspent) Red else Green,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(UI.colors.pure)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spent: ${item.spentAmount.format(baseCurrency)} $baseCurrency",
                style = UI.typo.c.style(color = Gray)
            )
            Text(
                text = "Budget: ${item.budgetAmount.format(baseCurrency)} $baseCurrency",
                style = UI.typo.c.style(color = Gray)
            )
        }
    }
}
