package com.ivy.accounts

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.domain.model.networth.NetWorthSummary

@Composable
fun NetWorthCard(
    summary: NetWorthSummary,
    onValuateClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(UI.shapes.r4)
            .background(UI.colors.medium, UI.shapes.r4)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TOTAL NET WORTH",
                    style = UI.typo.c.style(
                        color = UI.colors.pureInverse,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${summary.baseCurrency} ${String.format("%,.0f", summary.netWorth)}",
                    style = UI.typo.h2.style(
                        color = if (summary.netWorth >= 0) UI.colors.primary else UI.colors.red,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(UI.shapes.r2)
                    .background(UI.colors.pure)
                    .clickable(onClick = onValuateClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "+ Valuate",
                    style = UI.typo.c.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(UI.shapes.r2)
                .background(UI.colors.pure)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Assets",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
                Text(
                    text = "${summary.baseCurrency} ${String.format("%,.0f", summary.totalAssets)}",
                    style = UI.typo.b1.style(
                        color = UI.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Liquid: ${String.format("%,.0f", summary.liquidAssets)} • Invest: ${String.format("%,.0f", summary.investmentAssets)}",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Debts & Liabilities",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
                Text(
                    text = "${summary.baseCurrency} ${String.format("%,.0f", summary.totalLiabilities)}",
                    style = UI.typo.b1.style(
                        color = UI.colors.red,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Loans & Cards",
                    style = UI.typo.c.style(color = UI.colors.pureInverse)
                )
            }
        }
    }
}

