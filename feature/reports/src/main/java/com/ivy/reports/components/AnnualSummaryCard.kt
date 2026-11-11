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
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.components.IvyIcon
import java.time.LocalDate

@Composable
fun AnnualSummaryCard(
    year: Int,
    baseCurrency: String,
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    savingsRate: Double,
    annualBudgetSavings: Double,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCalendarYear = LocalDate.now().year
    val canGoForward = year < currentCalendarYear
    val isNetPositive = netSavings >= 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(UI.colors.pure)
            .padding(18.dp)
    ) {
        // Year Navigator Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(UI.colors.medium)
                    .clickable { onPreviousYear() },
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_back,
                    tint = UI.colors.pureInverse,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = year.toString(),
                style = UI.typo.h2.style(
                    fontWeight = FontWeight.ExtraBold,
                    color = UI.colors.pureInverse
                )
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (canGoForward) UI.colors.medium else UI.colors.medium.copy(alpha = 0.3f))
                    .clickable(enabled = canGoForward) { onNextYear() },
                contentAlignment = Alignment.Center
            ) {
                IvyIcon(
                    icon = R.drawable.ic_arrow_right,
                    tint = if (canGoForward) UI.colors.pureInverse else Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // Main Cash Flow Cards: Income & Expenses
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Income Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Green.copy(alpha = 0.08f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Total Income",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${totalIncome.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = Green,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            // Expense Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Red.copy(alpha = 0.08f))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Total Expenses",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${totalExpenses.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Net Cash Flow Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(UI.colors.medium)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Net Cash Flow",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(2.dp))
                val prefix = if (isNetPositive) "+" else ""
                Text(
                    text = "$prefix${netSavings.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = if (isNetPositive) Green else Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            if (totalIncome > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNetPositive) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", savingsRate)}% Saved",
                        style = UI.typo.c.style(
                            color = if (isNetPositive) Green else Red,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Annual Budget Savings Banner (if any)
        if (annualBudgetSavings > 0) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B5E20).copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IvyIcon(
                    icon = R.drawable.ic_buffer_ok,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${annualBudgetSavings.format(baseCurrency)} $baseCurrency saved under budget in $year!",
                    style = UI.typo.c.style(
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
