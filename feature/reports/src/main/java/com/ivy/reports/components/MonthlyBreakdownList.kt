package com.ivy.reports.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) UI.colors.medium else UI.colors.pure)
                    .clickable { onSelectMonth(index) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = month.monthName,
                        style = UI.typo.b2.style(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            color = UI.colors.pureInverse
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Row {
                        Text(
                            text = "In: +${month.income.format(baseCurrency)}",
                            style = UI.typo.c.style(color = Green)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Out: -${month.expenses.format(baseCurrency)}",
                            style = UI.typo.c.style(color = Red)
                        )
                    }
                }

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
                    }
                }
            }

            if (index != reversedIndices.last()) {
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}
