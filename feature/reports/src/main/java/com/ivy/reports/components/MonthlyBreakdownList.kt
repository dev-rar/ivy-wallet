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
import com.ivy.reports.model.MonthlyReportData
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.IvyIcon
import com.ivy.ui.R

@Composable
fun MonthlyBreakdownList(
    monthlyData: List<MonthlyReportData>,
    selectedMonthIndex: Int,
    baseCurrency: String,
    onSelectMonth: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.pure)
            .padding(18.dp)
    ) {
        Text(
            text = "Monthly Overview",
            style = UI.typo.b1.style(
                fontWeight = FontWeight.ExtraBold,
                color = UI.colors.pureInverse
            )
        )

        Spacer(Modifier.height(14.dp))

        // Display in descending order (Dec down to Jan)
        val reversedIndices = monthlyData.indices.reversed()

        for (index in reversedIndices) {
            val month = monthlyData[index]
            val isSelected = index == selectedMonthIndex
            val isNetPositive = month.netSavings >= 0

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(UI.shapes.r4)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Green else UI.colors.medium,
                        shape = UI.shapes.r4
                    )
                    .background(if (isSelected) UI.colors.medium.copy(alpha = 0.45f) else UI.colors.pure)
                    .clickable { onSelectMonth(index) }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Calendar Month Icon/Badge + Month Name & In/Out breakdown
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Green else UI.colors.medium),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = month.monthAbbrev.take(3).uppercase(),
                                style = UI.typo.c.style(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) UI.colors.pure else UI.colors.pureInverse
                                )
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = month.monthName,
                                style = UI.typo.b2.style(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = UI.colors.pureInverse
                                )
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${month.income.format(baseCurrency)}",
                                    style = UI.typo.c.style(
                                        color = Green,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "-${month.expenses.format(baseCurrency)}",
                                    style = UI.typo.c.style(
                                        color = Red,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Right: Net Cash Flow + Savings Pill
                    Column(horizontalAlignment = Alignment.End) {
                        val prefix = if (isNetPositive) "+" else ""
                        Text(
                            text = "$prefix${month.netSavings.format(baseCurrency)} $baseCurrency",
                            style = UI.typo.b2.style(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isNetPositive) Green else Red
                            )
                        )

                        if (month.totalBudgetSavings > 0) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "+${month.totalBudgetSavings.format(baseCurrency)} saved",
                                style = UI.typo.c.style(
                                    color = Green,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else if (month.totalBudgeted > 0 && month.totalBudgetSavings < 0) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${month.totalBudgetSavings.format(baseCurrency)} over",
                                style = UI.typo.c.style(
                                    color = Red,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            if (index != reversedIndices.last()) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
