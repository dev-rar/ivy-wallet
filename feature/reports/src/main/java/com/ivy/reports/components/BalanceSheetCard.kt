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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.reports.model.BalanceSheetSummary
import com.ivy.wallet.ui.theme.Gray
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red

@Composable
fun BalanceSheetCard(
    balanceSheet: BalanceSheetSummary,
    baseCurrency: String,
    modifier: Modifier = Modifier
) {
    val isPositiveWorth = balanceSheet.netWorth >= 0
    val prefix = if (isPositiveWorth) "+" else ""

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .background(UI.colors.medium)
            .border(1.dp, UI.colors.pureInverse.copy(alpha = 0.08f), UI.shapes.r4)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BALANCE SHEET & NET WORTH",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$prefix${balanceSheet.netWorth.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.b1.style(
                        color = if (isPositiveWorth) Green else Red,
                        fontWeight = FontWeight.ExtraBold
                    ).copy(fontSize = 22.sp)
                )
            }

            Box(
                modifier = Modifier
                    .clip(UI.shapes.rFull)
                    .background(UI.colors.pure.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Debt ${String.format("%.1f", balanceSheet.debtToAssetRatio)}%",
                    style = UI.typo.nC.style(
                        color = if (balanceSheet.debtToAssetRatio <= 40.0) Green else Red,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(UI.shapes.r3)
                .background(UI.colors.pure.copy(alpha = 0.55f))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Assets Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOTAL ASSETS",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${balanceSheet.totalAssets.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.nB2.style(
                        color = Green,
                        fontWeight = FontWeight.ExtraBold
                    ).copy(fontSize = 14.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Cash/Bank: ${balanceSheet.cashAndBankAssets.format(baseCurrency)}",
                    style = UI.typo.nC.style(color = Gray, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Receivables: ${balanceSheet.accountsReceivable.format(baseCurrency)}",
                    style = UI.typo.nC.style(color = Gray, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(54.dp)
                    .background(UI.colors.pureInverse.copy(alpha = 0.12f))
            )

            Spacer(Modifier.width(12.dp))

            // Liabilities Column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LIABILITIES",
                    style = UI.typo.c.style(
                        color = Gray,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${balanceSheet.totalLiabilities.format(baseCurrency)} $baseCurrency",
                    style = UI.typo.nB2.style(
                        color = Red,
                        fontWeight = FontWeight.ExtraBold
                    ).copy(fontSize = 14.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Credit/OD: ${balanceSheet.creditAndOverdraftLiabilities.format(baseCurrency)}",
                    style = UI.typo.nC.style(color = Gray, fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Payables: ${balanceSheet.accountsPayable.format(baseCurrency)}",
                    style = UI.typo.nC.style(color = Gray, fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

