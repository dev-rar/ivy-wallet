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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ivy.design.l0_system.UI
import com.ivy.design.l0_system.style
import com.ivy.legacy.utils.format
import com.ivy.ui.R
import com.ivy.wallet.ui.theme.Green
import com.ivy.wallet.ui.theme.Red
import com.ivy.wallet.ui.theme.White
import com.ivy.wallet.ui.theme.components.BalanceRow
import com.ivy.wallet.ui.theme.components.IvyIcon

@Suppress("UNUSED_PARAMETER")
@Composable
fun AnnualSummaryCard(
    year: Int,
    baseCurrency: String,
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    savingsRate: Double,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNetPositive = netSavings >= 0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(UI.shapes.r4)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1B1D24),
                        Color(0xFF2A2E39)
                    )
                )
            )
            .border(1.dp, White.copy(alpha = 0.12f), UI.shapes.r4)
    ) {
        // Top Row: EMV Credit Card Chip + "CASH FLOW" label on Left | Savings Rate Pill on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Metallic EMV Chip
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 24.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE6C365),
                                    Color(0xFFC49B38)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFFFFE28A).copy(alpha = 0.6f), RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 18.dp, height = 12.dp)
                            .border(1.dp, Color(0xFF8C6B1F).copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                    )
                }

                Spacer(Modifier.width(10.dp))

                Text(
                    text = "CASH FLOW",
                    style = UI.typo.c.style(
                        color = White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            if (totalIncome > 0) {
                Box(
                    modifier = Modifier
                        .clip(UI.shapes.rFull)
                        .background(
                            if (isNetPositive) Green.copy(alpha = 0.18f) else Red.copy(alpha = 0.18f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${String.format("%.1f", savingsRate)}% Saved",
                        style = UI.typo.nC.style(
                            color = if (isNetPositive) Green else Red,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Center: Net Cash Flow Row (identical to Home screen card)
        BalanceRow(
            modifier = Modifier.padding(horizontal = 20.dp),
            textColor = White,
            currency = baseCurrency,
            balance = netSavings,
            balanceFontSize = 24.sp,
            currencyFontSize = 20.sp,
            shortenBigNumbers = false,
            hiddenMode = false
        )

        Spacer(Modifier.height(20.dp))

        // Bottom Strip: Unified Income & Expenses Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(White.copy(alpha = 0.06f))
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Income Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Green.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_income_white,
                        tint = Green,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Column {
                    Text(
                        text = stringResource(R.string.income),
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${totalIncome.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = Green,
                            fontWeight = FontWeight.ExtraBold
                        ).copy(fontSize = 13.sp),
                        maxLines = 1
                    )
                }
            }

            // Vertical Divider
            Spacer(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(White.copy(alpha = 0.14f))
            )

            Spacer(Modifier.width(6.dp))

            // Expense Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Red.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    IvyIcon(
                        icon = R.drawable.ic_expense,
                        tint = Red,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Column {
                    Text(
                        text = stringResource(R.string.expenses),
                        style = UI.typo.c.style(
                            color = White.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${totalExpenses.format(baseCurrency)} $baseCurrency",
                        style = UI.typo.nB2.style(
                            color = White,
                            fontWeight = FontWeight.ExtraBold
                        ).copy(fontSize = 13.sp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
